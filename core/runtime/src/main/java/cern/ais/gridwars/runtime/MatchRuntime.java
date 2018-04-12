package cern.ais.gridwars.runtime;

import cern.ais.gridwars.bot.PlayerBot;

import java.nio.file.FileSystems;
import java.util.Random;
import java.util.stream.Stream;

import static cern.ais.gridwars.runtime.LogUtils.*;


public class MatchRuntime {

    public static void main(String[] args) {
        new MatchRuntime().executeMatch();
    }

    public void executeMatch() {
        info("Start executing match ...");

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

        info("Trying to load bot classes ...");

        Class bot1Class = loadClass(bot1ClassName);
        info("Bot 1 class loaded: " + bot1Class);

        Class bot2Class = loadClass(bot2ClassName);
        info("Bot 2 class loaded: " + bot2Class);

        info("Trying to instantiate bot classes ...");

        PlayerBot bot1 = instantiateBotClass(bot1Class);
        info("Bot 1 class instantiated: " + bot1);

        PlayerBot bot2 = instantiateBotClass(bot2Class);
        info("Bot 2 class instantiated: " + bot2);

        info("Create mock match result...");
        createMockMatchResult();

        info("... finished executing match");
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

    private void createMockMatchResult() {
        Random random = new Random();
        MatchResult result = new MatchResult();
        result.setOutcome(MatchResult.Outcome.values()[random.nextInt(MatchResult.Outcome.values().length)]);
        result.setTurns(random.nextInt(2000));

        if (MatchResult.Outcome.ERROR == result.getOutcome()) {
            result.setErrorMessage("Some comprehensive error message...");
        }

        result.storeToFile(MatchRuntimeConstants.MATCH_RESULT_FILE_NAME);

        info("Generated match result: " + result.getOutcome().name());
    }
}
