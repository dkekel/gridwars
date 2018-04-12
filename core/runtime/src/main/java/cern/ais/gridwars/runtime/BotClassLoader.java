package cern.ais.gridwars.runtime;

import cern.ais.gridwars.bot.PlayerBot;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;


/**
 * Helper class to load bot classes from jar files
 *
 * For loading the bot classes we use isolated class loaders that work directly on the
 * jar file path. This allows to load different bot classes even if they share the same
 * fully qualified class name.
 */
class BotClassLoader {

    PlayerBot loadAndInstantiateBot(String botJarPath, String botClassName) {
        ClassLoader botClassLoader = createBotClassLoader(botJarPath);
        Class botClass = loadBotClass(botClassLoader, botClassName);
        return instantiateBotClass(botClass);
    }

    private ClassLoader createBotClassLoader(String botJarPath) {
        try {
            URL botJarUrl = new File(botJarPath).toURI().toURL();
            return new URLClassLoader(new URL[] {botJarUrl});
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

    private PlayerBot instantiateBotClass(Class botClass) {
        // TODO instantiate the class in a worker thread in order to enforce the 5 seconds init timeout...
        try {
            return (PlayerBot) botClass.newInstance();
        } catch (Exception e) {
            throw new BotClassLoaderException("Failed to instantiate bot class: " + botClass.getName(), e);
        }
    }

    public static class BotClassLoaderException extends RuntimeException {

        BotClassLoaderException(String message) {
            super(message);
        }

        BotClassLoaderException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
