package cern.ais.gridwars;

import cern.ais.gridwars.api.bot.PlayerBot;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * Helper class to load bot classes from jar files
 *
 * For loading the bot classes we use isolated class loaders that work directly on the
 * jar file path. This allows to load different bot classes even if they share the same
 * fully qualified class name.
 *
 * When instantiating a bot class, a timeout is enforced in order to prevent the bot
 * to block the execution of the match process for too long. If the bot fails to
 * initialise in time, an idle bot implementation will be return, which will (probably)
 * lose the match.
 */
final class BotClassLoader {

    PlayerBot loadAndInstantiateBot(String botJarPath, String botClassName) {
        ClassLoader botClassLoader = createBotClassLoader(botJarPath);
        Class botClass = loadBotClass(botClassLoader, botClassName);
        return instantiateBotClass(botClass);
    }

    private ClassLoader createBotClassLoader(String botJarPath) {
        try {
            URL botJarUrl = new File(botJarPath).toURI().toURL();
            return new URLClassLoader(new URL[] { botJarUrl });
        } catch (Exception e) {
            throw new BotClassLoaderException("Failed to create bot classloader for jar: " + botJarPath);
        }
    }

    private Class loadBotClass(ClassLoader botClassLoader, String botClassName) {
        try {
            return botClassLoader.loadClass(botClassName);
        } catch (ClassNotFoundException e) {
            throw new BotClassLoaderException("Failed to load bot class: " + botClassName, e);
        }
    }

    /**
     * Instantiates (and thus initialises) an instance of the given bot class. The instantiation in done
     * in an asynchronous task in order to enforce the instantiation timeout. If the bot fails to
     * instantiate and initialise in the allowed time, an idle bot implementation will be returned.
     */
    private PlayerBot instantiateBotClass(final Class botClass) {
        final ExecutorService executorService = Executors.newCachedThreadPool();
        final CompletableFuture<PlayerBot> instantiatorFuture = new CompletableFuture<>();
        PlayerBot botInstance;

        try {
            StdOutputSwitcher.INSTANCE.switchToDevNullPrintStream();

            executorService.submit(() -> {
                try {
                    instantiatorFuture.complete((PlayerBot) botClass.newInstance());
                } catch (Exception e) {
                    instantiatorFuture.completeExceptionally(e);
                }
            });

            botInstance = instantiatorFuture.get(GameConstants.BOT_INSTANTIATION_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            StdOutputSwitcher.INSTANCE.restoreOriginal();
        } catch (TimeoutException te) {
            StdOutputSwitcher.INSTANCE.restoreOriginal();
            LogUtils.error("Bot failed to initialise within the allowed timeout of " +
                GameConstants.BOT_INSTANTIATION_TIMEOUT_MS + " ms: " + botClass.getName());
            botInstance = new InitTimeoutIdleBot(botClass.getName(), GameConstants.BOT_INSTANTIATION_TIMEOUT_MS);
        } catch (ExecutionException | InterruptedException e) {
            StdOutputSwitcher.INSTANCE.restoreOriginal();
            throw new BotClassLoaderException("Failed to instantiate bot class: " + botClass.getName(), e.getCause());
        } finally {
            StdOutputSwitcher.INSTANCE.restoreOriginal();
            executorService.shutdownNow();
        }

        return botInstance;
    }

    static class BotClassLoaderException extends RuntimeException {

        BotClassLoaderException(String message) {
            super(message);
        }

        BotClassLoaderException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
