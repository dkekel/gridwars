package cern.ais.gridwars.runtime;

import cern.ais.gridwars.bot.PlayerBot;

import java.nio.file.FileSystems;
import java.util.stream.Stream;

public class MatchRuntime {

    public static void main(String[] args) {
        new MatchRuntime().executeMatch();
    }

    public void executeMatch() {
        System.out.println("Started match runtime ...");

        String workDir = FileSystems.getDefault().getPath("").toAbsolutePath().toString();
        String classPath = System.getProperty("java.class.path");
        String bot1ClassName = System.getProperty("gridwars.runtime.bot1ClassName");
        String bot2ClassName = System.getProperty("gridwars.runtime.bot2ClassName");

        Stream.of(
            "Class path: " + classPath,
            "Work dir: " + workDir,
            "Bot 1 class name: " + bot1ClassName,
            "Bot 2 class name: " + bot2ClassName
        ).forEach(System.out::println);

        System.out.println("Trying to load bot classes ...");

        Class bot1Class = loadClass(bot1ClassName);
        System.out.println("Bot 1 class loaded: " + bot1Class);

        Class bot2Class = loadClass(bot2ClassName);
        System.out.println("Bot 2 class loaded: " + bot2Class);

        System.out.println("Trying to instantiate bot classes ...");

        PlayerBot bot1 = instantiateBotClass(bot1Class);
        System.out.println("Bot 1 class instantiated: " + bot1);

        PlayerBot bot2 = instantiateBotClass(bot2Class);
        System.out.println("Bot 2 class instantiated: " + bot2);

        System.out.println("... finished match runtime");
    }

    private Class loadClass(String className) {
        try {
            return getClass().getClassLoader().loadClass(className);
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException(cnfe);
        }
    }

    private PlayerBot instantiateBotClass(Class botClass) {
        try {
            return (PlayerBot) botClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
