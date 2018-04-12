package cern.ais.gridwars.runtime;

import cern.ais.gridwars.bot.PlayerBot;

public class BotClassLoader {

    public PlayerBot loadAndInstantiateBot(String botClassName) {
        Class botClass = loadBotClass(botClassName);
        return instantiateBotClass(botClass);
    }

    private Class loadBotClass(String botClassName) {
        try {
            return getClass().getClassLoader().loadClass(botClassName);
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

        public BotClassLoaderException(String message) {
            super(message);
        }

        public BotClassLoaderException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
