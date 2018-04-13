package cern.ais.gridwars.runtime;

public final class MatchRuntimeConstants {

    public static final String BOT_1_JAR_PATH_SYS_PROP_KEY = "gridwars.runtime.bot1JarPath";
    public static final String BOT_2_JAR_PATH_SYS_PROP_KEY = "gridwars.runtime.bot2JarPath";
    public static final String BOT_1_CLASS_NAME_SYS_PROP_KEY = "gridwars.runtime.bot1ClassName";
    public static final String BOT_2_CLASS_NAME_SYS_PROP_KEY = "gridwars.runtime.bot2ClassName";
    public static final String BOT_1_OUTPUT_FILE_NAME = "bot_1_output.txt";
    public static final String BOT_2_OUTPUT_FILE_NAME = "bot_2_output.txt";
    public static final String MATCH_RESULT_FILE_NAME = "result.properties";
    public static final String MATCH_TURNS_PAYLOAD_FILE_NAME = "turns.gz";
    public static final String MATCH_RUNTIME_MAIN_CLASS_NAME = MatchRuntime.class.getName();

    private MatchRuntimeConstants() {
    }
}
