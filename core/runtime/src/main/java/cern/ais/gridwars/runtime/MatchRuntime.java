package cern.ais.gridwars.runtime;

import java.nio.file.FileSystems;
import java.util.stream.Stream;

public class MatchRuntime {

    public static void main(String[] args) {
        Stream.of(
            "Started match runtime...",

            FileSystems.getDefault().getPath("").toAbsolutePath().toString(),
//            getSysProp("Bot 1 jar path", "gridwars.runtime.bot1JarPath"),
//            getSysProp("Bot 2 jar path", "gridwars.runtime.bot2JarPath"),
            getSysProp("Bot 1 class name", "gridwars.runtime.bot1ClassName"),
            getSysProp("Bot 2 class name", "gridwars.runtime.bot2ClassName"),

            "...finished match runtime"
        ).forEach(System.out::println);
    }

    private static String getSysProp(String desc, String key) {
        return "    " + desc + ": " + System.getProperty(key);
    }
}
