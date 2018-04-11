package cern.ais.gridwars.web.worker;


import cern.ais.gridwars.runtime.MatchRuntimeResult;
import cern.ais.gridwars.web.config.GridWarsProperties;
import cern.ais.gridwars.web.domain.Bot;
import cern.ais.gridwars.web.domain.Match;
import cern.ais.gridwars.web.service.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MatchExecutor {

    // Must correspond to the main class in the "core/runtime" sub-project
    private static final String MATCH_RUNTIME_MAIN_CLASS = "cern.ais.gridwars.runtime.MatchRuntime";

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final GridWarsProperties gridWarsProperties;
    private File matchDir;
    private File stdOutFile;
    private File stdErrFile;

    public MatchExecutor(GridWarsProperties gridWarsProperties) {
        this.gridWarsProperties = Objects.requireNonNull(gridWarsProperties);
    }

    public Match executeMatch(Match match) {
        try {
           createMatchWorkDir(match);
           createOutputFiles();
           return doExecuteMatch(match);
        } catch (Exception e) {
            // TODO catch and handle exception, mark match as failed... no exception should escape here...
            throw e;
        } finally {
            cleanUp();
        }
    }

    private void createMatchWorkDir(Match match) {
        String matchDirPath = FileUtils.joinFilePaths(gridWarsProperties.getDirectories().getMatchesDir(),
                match.getId());
        matchDir = new File(matchDirPath);

        if (!matchDir.exists()) {
            if (!matchDir.mkdir()) {
                throw new MatchExecutionException("Could not create match dir: " + matchDirPath);
            }
        } else {
            if (!matchDir.isDirectory()) {
                throw new MatchExecutionException("Match dir file exist, but is not a directory: " + matchDirPath);
            }

            clearDirectory(matchDir);
        }
    }

    private void clearDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                FileUtils.deleteFile(file);
            }
        }
    }

    private void createOutputFiles() {
        stdOutFile = createOutputFile("stdout");
        stdErrFile = createOutputFile("stderr");
    }

    private File createOutputFile(String namePrefix) {
        String outputFilePath = FileUtils.joinFilePaths(matchDir.getAbsolutePath(), namePrefix + ".txt");
        File outputFile = new File(outputFilePath);

        try {
            if (!outputFile.createNewFile()) {
                throw new MatchExecutionException("Could not create output file: " + outputFilePath);
            }
        } catch (IOException e) {
            throw new MatchExecutionException("Could not create output file: " + outputFilePath, e);
        }

        return outputFile;
    }

    private Match doExecuteMatch(Match match) {
        match.setStarted(Instant.now());

        MatchRuntimeResult result = executeRemoteMatchProcess(match);

        Random random = new Random();
        match.setEnded(Instant.now());
        match.setStatus(Match.Status.FINISHED);
        match.setOutcome(Match.Outcome.values()[random.nextInt(Match.Outcome.values().length)]);
        match.setTurns(random.nextInt(3000));
        return match;
    }

    private MatchRuntimeResult executeRemoteMatchProcess(Match match) {
        List<String> jvmProcessArguments = createJvmProcessArguments(match);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Match process working folder: {}", matchDir.getAbsolutePath());
            LOG.debug("Match process args: {}", jvmProcessArguments.stream().collect(Collectors.joining(" ")));
        }

        try {
            Process matchProcess = new ProcessBuilder(jvmProcessArguments)
                .directory(matchDir)
                .redirectOutput(stdOutFile)
                .redirectError(stdErrFile)
                .start();

            boolean exitedNormally = matchProcess.waitFor(gridWarsProperties.getMatches().getExecutionTimeoutSeconds(), TimeUnit.SECONDS);

            if (exitedNormally) {
                LOG.debug("Match process exited normally with code: {}", matchProcess.exitValue());
                return MatchRuntimeResult.fromReturnCode(matchProcess.exitValue());
            } else {
                LOG.debug("Match process timed out and will be killed");
                // TODO make sure the process is really really killed...
                matchProcess.destroyForcibly();
                return MatchRuntimeResult.TIMEOUT;
            }
        } catch (IOException | InterruptedException e) {
            LOG.error("Starting match JVM process failed", e);
            return MatchRuntimeResult.ERROR;
        }
    }

    private List<String> createJvmProcessArguments(Match match) {
        List<String> args = new LinkedList<>();
        args.add(createJavaExecutablePath());
        args.addAll(createMemoryAndGcArguments());
        args.addAll(createClassPathArguments(match));
        args.addAll(createSysPropArguments(match));
        args.add(MATCH_RUNTIME_MAIN_CLASS);
        return args;
    }

    private String createJavaExecutablePath() {
        File javaBinDir = new File(System.getProperty("java.home"), "bin");
        File javaExe = new File(javaBinDir, "java.exe"); // Windows
        File javaBin = new File(javaBinDir, "java"); // Unixoid
        return javaExe.exists() ? javaExe.getAbsolutePath() : javaBin.getAbsolutePath();
    }

    private List<String> createClassPathArguments(Match match) {
        String classPathArgument = Stream.of(
                createMatchRuntimeClassPathPart(),
                determineBotJarPath(match.getPlayer1()),
                determineBotJarPath(match.getPlayer2())
            ).collect(Collectors.joining(";"));

        return Stream.of(
                "-cp",
                "\"" + classPathArgument + "\""
            ).collect(Collectors.toList());
    }

    private String createMatchRuntimeClassPathPart() {
        File[] runtimeDirFile = new File(gridWarsProperties.getDirectories().getRuntimeDir()).listFiles();
        if (runtimeDirFile == null) {
            return "";
        }

        return Stream.of(runtimeDirFile)
            .filter(file -> file.getName().toLowerCase().endsWith(".jar"))
            .sorted()
            .map(File::getAbsolutePath)
            .collect(Collectors.joining(";"));
    }

    private String determineBotJarPath(Bot bot) {
        return FileUtils.joinFilePaths(gridWarsProperties.getDirectories().getBotJarDir(), bot.getJarFileName());
    }

    private List<String> createMemoryAndGcArguments() {
        return Stream.of(
            "-Xms256m",
            "-Xmx256m"
            // TODO add some more GC optimisation flags
        ).collect(Collectors.toList());
    }

    private List<String> createSysPropArguments(Match match) {
        return Stream.of(
//            createSysPropArgument("gridwars.runtime.matchWorkDir", matchDir.getAbsolutePath()),
            createSysPropArgument("gridwars.runtime.bot1ClassName", match.getPlayer1().getBotClassName()),
            createSysPropArgument("gridwars.runtime.bot2ClassName", match.getPlayer2().getBotClassName())
        ).collect(Collectors.toList());
    }

    private String createSysPropArgument(String key, String value) {
        return "-D" + key + "=\"" + value + "\"";
    }

    private void cleanUp() {
        matchDir = null;
        stdOutFile = null;
        stdErrFile = null;
    }

    public static final class MatchExecutionException extends RuntimeException {

        public MatchExecutionException(String message) {
            super(message);
        }

        public MatchExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
