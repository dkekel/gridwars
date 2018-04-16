package cern.ais.gridwars.web.worker;

import cern.ais.gridwars.runtime.MatchFile;
import cern.ais.gridwars.runtime.MatchResult;
import cern.ais.gridwars.runtime.MatchRuntimeConstants;
import cern.ais.gridwars.web.config.GridWarsProperties;
import cern.ais.gridwars.web.domain.Bot;
import cern.ais.gridwars.web.domain.Match;
import cern.ais.gridwars.web.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;


class MatchExecutor {

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final GridWarsProperties gridWarsProperties;
    private File matchDir;
    private File stdOutFile;
    private File stdErrFile;

    MatchExecutor(GridWarsProperties gridWarsProperties) {
        this.gridWarsProperties = Objects.requireNonNull(gridWarsProperties);
    }

    Match executeMatch(Match match) {
        try {
           createMatchWorkDir(match);
           createOutputFiles();
           return doExecuteMatch(match);
        } catch (Exception e) {
            LOG.error("Execution of match {} failed before process could be started: {}", match.getId(), e.getMessage(), e);
            return markMatchAsFailed(match, "Execution of match failed before process could be started");
        } finally {
            cleanUp();
        }
    }

    private void createMatchWorkDir(Match match) {
        matchDir = Paths.get(gridWarsProperties.getDirectories().getMatchesDir(), match.getId()).toFile();

        if (!matchDir.exists()) {
            if (!matchDir.mkdirs()) {
                throw new MatchExecutionException("Could not create match dir: " + matchDir.getAbsolutePath());
            }
        } else {
            if (!matchDir.isDirectory()) {
                throw new MatchExecutionException("Match dir file exist, but is not a directory: " + matchDir.getAbsolutePath());
            }

            FileUtils.clearDirectory(matchDir);
        }
    }

    private void createOutputFiles() {
        stdOutFile = createOutputFile(MatchFile.STDOUT);
        stdErrFile = createOutputFile(MatchFile.STDERR);
    }

    private File createOutputFile(MatchFile matchFile) {
        File outputFile = matchFile.toFile(matchDir);

        try {
            if (!outputFile.createNewFile()) {
                throw new MatchExecutionException("Failed to create output file: " + outputFile.getAbsolutePath());
            }
        } catch (IOException e) {
            throw new MatchExecutionException("Failed to create output file: " + outputFile.getAbsolutePath(), e);
        }

        return outputFile;
    }

    private Match doExecuteMatch(Match match) {
        match.setStarted(Instant.now());
        MatchResult result = executeMatchProcess(match);
        match.setEnded(Instant.now());

        applyResultToMatch(result, match);
        return match;
    }

    private MatchResult executeMatchProcess(Match match) {
        List<String> jvmProcessArguments = createJvmProcessArguments(match);
        int processTimeoutSeconds = gridWarsProperties.getMatches().getExecutionTimeoutSeconds();

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                "Starting match process for match: {}" +
                "\n\tProcess working folder: {}" +
                "\n\tProcess args: {}",
                match.getId(),
                matchDir.getAbsolutePath(),
                jvmProcessArguments.stream().collect(Collectors.joining(" "))
            );
        }

        try {
            Process matchProcess = new ProcessBuilder(jvmProcessArguments)
                .directory(matchDir)
                .redirectOutput(stdOutFile)
                .redirectError(stdErrFile)
                .start();

            LOG.debug("Started match process with, waiting for match to finish within {} seconds ...", processTimeoutSeconds);
            boolean exitedNormally = matchProcess.waitFor(processTimeoutSeconds, TimeUnit.SECONDS);

            if (exitedNormally) {
                int exitValue = matchProcess.exitValue();

                if (0 == exitValue) {
                    LOG.debug("... match process exited normally with code: {}", exitValue);
                    return createSuccessfulExecutionMatchResult();
                } else {
                    LOG.debug("... match process exited abnormally with error code: {}", exitValue);
                    return createAbnormalExitCodeMatchResult(exitValue);
                }
            } else {
                LOG.debug("...match process timed out and will be killed");
                matchProcess.destroyForcibly();
                return createTimeoutMatchResult();
            }
        } catch (IOException | InterruptedException e) {
            LOG.error("Execution of match process failed for match id: {}", match.getId(), e);
            return createProcessExceptionMatchResult(e.getMessage());
        }
    }

    private List<String> createJvmProcessArguments(Match match) {
        List<String> args = new LinkedList<>();
        args.add(createJavaExecutablePath());
        args.addAll(createJvmMemoryArguments());
        args.addAll(createMatchRuntimeClassPathArguments());
        args.addAll(createSysPropArguments(match));
        args.add(MatchRuntimeConstants.MATCH_RUNTIME_MAIN_CLASS_NAME);
        return args;
    }

    private String createJavaExecutablePath() {
        File javaBinDir = new File(System.getProperty("java.home"), "bin");
        File javaExe = new File(javaBinDir, "java.exe"); // Windows
        File javaBin = new File(javaBinDir, "java"); // Unixoid
        return javaExe.exists() ? javaExe.getAbsolutePath() : javaBin.getAbsolutePath();
    }

    private List<String> createJvmMemoryArguments() {
        String workerHeapSize = gridWarsProperties.getMatches().getWorkerHeapSize();
        return Arrays.asList("-Xms" + workerHeapSize, "-Xmx" + workerHeapSize);
    }

    private List<String> createMatchRuntimeClassPathArguments() {
        String runtimeDirPath = gridWarsProperties.getDirectories().getRuntimeDir();
        File[] runtimeDirFile = new File(runtimeDirPath).listFiles();
        if ((runtimeDirFile == null) || (runtimeDirFile.length == 0)) {
            LOG.warn("Match process runtime folder is be empty or does not exist, the match runtime process will " +
                "very likely with ClassNotFound exceptions: {}", runtimeDirPath);
            return Collections.emptyList();
        }

        String runtimeJarFilePaths = Stream.of(runtimeDirFile)
            .filter(this::isJarFile)
            // It may be relevant that "-api.jar" comes before "-impl.jar" and "-runtime.jar", but not sure. To
            // avoid potential issues, we sort the jars by name to have a safe order: api, impl, runtime
            .sorted()
            .map(File::getAbsolutePath)
            .collect(Collectors.joining(File.pathSeparator));

        // we might have to check that happens if the "classPathArgument" string contains whitespaces. Maybe it
        // is necessary to wrap it in double quotes then?
        return Arrays.asList("-cp", runtimeJarFilePaths);
    }

    private boolean isJarFile(File file) {
        return file.getName().toLowerCase().endsWith(".jar");
    }

    private List<String> createSysPropArguments(Match match) {
        return Arrays.asList(
            createSysPropArgument(MatchRuntimeConstants.BOT_1_JAR_PATH_SYS_PROP_KEY, determineBotJarPath(match.getBot1())),
            createSysPropArgument(MatchRuntimeConstants.BOT_2_JAR_PATH_SYS_PROP_KEY, determineBotJarPath(match.getBot2())),
            createSysPropArgument(MatchRuntimeConstants.BOT_1_CLASS_NAME_SYS_PROP_KEY, match.getBot1().getBotClassName()),
            createSysPropArgument(MatchRuntimeConstants.BOT_2_CLASS_NAME_SYS_PROP_KEY, match.getBot2().getBotClassName())
        );
    }

    private String createSysPropArgument(String key, String value) {
        return "-D" + key + "=" + value;
    }

    private String determineBotJarPath(Bot bot) {
        return Paths.get(gridWarsProperties.getDirectories().getBotJarDir(), bot.getJarFileName()).toString();
    }

    private MatchResult createSuccessfulExecutionMatchResult() {
        String matchResultFilePath = MatchFile.RESULT.toAbsolutePath(matchDir);
        try {
            return MatchResult.loadFromFile(matchResultFilePath);
        } catch (Exception e) {
            LOG.error("Could not load match result file: {}", matchResultFilePath, e);
            return createErrorMatchResult("Failed to load match result file: " + e.getMessage());
        }
    }

    private MatchResult createAbnormalExitCodeMatchResult(int exitCode) {
        return createErrorMatchResult("Match process exited with abnormal code: " + exitCode);
    }

    private MatchResult createTimeoutMatchResult() {
        return createErrorMatchResult("Match execution process timed out");
    }

    private MatchResult createProcessExceptionMatchResult(String exceptionMessage) {
        return createErrorMatchResult("Match execution process failed with exception: " + exceptionMessage);
    }

    private MatchResult createErrorMatchResult(String errorMessage) {
        return new MatchResult()
            .setOutcome(MatchResult.Outcome.ERROR)
            .setErrorMessage(errorMessage);
    }

    private void applyResultToMatch(MatchResult result, Match match) {
        match.setTurnCount(result.getTurns());

        switch (result.getOutcome()) {
            case BOT_1_WON:
                match.setStatus(Match.Status.FINISHED);
                match.setOutcome(Match.Outcome.WIN);
                break;
            case BOT_2_WON:
                match.setStatus(Match.Status.FINISHED);
                match.setOutcome(Match.Outcome.LOSS);
                break;
            case DRAW:
                match.setStatus(Match.Status.FINISHED);
                match.setOutcome(Match.Outcome.DRAW);
                break;
            case ERROR:
                markMatchAsFailed(match, result.getErrorMessage());
                break;
            default:
                markMatchAsFailed(match, "Unknown match outcome: " + result.getOutcome());
        }
    }

    private Match markMatchAsFailed(Match match, String errorMessage) {
        if (match.getEnded() == null) {
            match.setEnded(Instant.now());
        }

        match.setStatus(Match.Status.FAILED);
        match.setOutcome(Match.Outcome.DNF);
        match.setFailReason(errorMessage);
        return match;
    }

    private void cleanUp() {
        matchDir = null;
        stdOutFile = null;
        stdErrFile = null;
    }

    public static class MatchExecutionException extends RuntimeException {

        MatchExecutionException(String message) {
            super(message);
        }

        MatchExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
