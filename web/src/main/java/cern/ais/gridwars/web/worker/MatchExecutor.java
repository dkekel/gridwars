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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class MatchExecutor {

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
            LOG.error("Execution of match failed before process could be started: {}", match.getId(), e);
            return markMatchAsFailed(match);
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
        MatchRuntimeResult result = executeMatchProcess(match);
        match.setEnded(Instant.now());

        applyResultToMatch(result, match);
        return match;
    }

    private MatchRuntimeResult executeMatchProcess(Match match) {
        List<String> jvmProcessArguments = createJvmProcessArguments(match);
        int processTimeoutSeconds = gridWarsProperties.getMatches().getExecutionTimeoutSeconds();

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                "Starting match process for match: {}" +
                "\n\tprocess working folder: {}" +
                "\n\tprocess args: {}",
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
                MatchRuntimeResult result = MatchRuntimeResult.fromReturnCode(exitValue);

                LOG.debug("... match process exited normally with code {}: {}", exitValue, result.name());
                return result;
            } else {
                LOG.debug("...match process timed out and will be killed");
                // TODO make sure the process is really really killed...
                matchProcess.destroyForcibly();
                return MatchRuntimeResult.TIMEOUT;
            }
        } catch (IOException | InterruptedException e) {
            LOG.error("Execution of match process failed for match id: {}", match.getId(), e);
            return MatchRuntimeResult.ERROR;
        }
    }

    private List<String> createJvmProcessArguments(Match match) {
        List<String> args = new LinkedList<>();
        args.add(createJavaExecutablePath());
        args.addAll(createJvmMemoryAndGcArguments());
        args.addAll(createClassPathArguments(match));
        args.addAll(createSysPropArguments(match));
        args.add(gridWarsProperties.getMatches().getMatchRuntimeMainClassName());
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
                determineBotJarPath(match.getBot1()),
                determineBotJarPath(match.getBot2())
            ).collect(Collectors.joining(File.pathSeparator));

        return Stream.of(
                "-cp",
                "\"" + classPathArgument + "\""
            ).collect(Collectors.toList());
    }

    private String createMatchRuntimeClassPathPart() {
        String runtimeDirPath = gridWarsProperties.getDirectories().getRuntimeDir();
        File[] runtimeDirFile = new File(runtimeDirPath).listFiles();
        if ((runtimeDirFile == null) || (runtimeDirFile.length == 0)) {
            LOG.warn("Match process runtime folder is be empty or does not exist, the match runtime process will " +
                "very likely with ClassNotFound exceptions: {}", runtimeDirPath);
            return "";
        }

        return Stream.of(runtimeDirFile)
            .filter(this::isJarFile)
            // It may be relevant that "-api.jar" comes before "-impl.jar" and "-runtime.jar", but not sure. To
            // avoid potential issues, we sort the jars by name to have a safe order: api, impl, runtime
            .sorted()
            .map(File::getAbsolutePath)
            .collect(Collectors.joining(File.pathSeparator));
    }

    private boolean isJarFile(File file) {
        return file.getName().toLowerCase().endsWith(".jar");
    }

    private String determineBotJarPath(Bot bot) {
        return FileUtils.joinFilePaths(gridWarsProperties.getDirectories().getBotJarDir(), bot.getJarFileName());
    }

    private List<String> createJvmMemoryAndGcArguments() {
        return Stream.of(
            "-Xms256m",
            "-Xmx256m"
            // TODO add some more GC optimisation flags
        ).collect(Collectors.toList());
    }

    private List<String> createSysPropArguments(Match match) {
        return Stream.of(
            createSysPropArgument("gridwars.runtime.bot1ClassName", match.getBot1().getBotClassName()),
            createSysPropArgument("gridwars.runtime.bot2ClassName", match.getBot2().getBotClassName())
        ).collect(Collectors.toList());
    }

    private String createSysPropArgument(String key, String value) {
        return "-D" + key + "=\"" + value + "\"";
    }

    private void applyResultToMatch(MatchRuntimeResult result, Match match) {
        switch (result) {
            case DRAW:
                match.setStatus(Match.Status.FINISHED);
                match.setOutcome(Match.Outcome.DRAW);
                break;
            case BOT1_WINNER:
                match.setStatus(Match.Status.FINISHED);
                match.setOutcome(Match.Outcome.WIN);
                break;
            case BOT2_WINNER:
                match.setStatus(Match.Status.FINISHED);
                match.setOutcome(Match.Outcome.LOSS);
                break;
            default: // We consider all other results as failures
                markMatchAsFailed(match);
        }
    }

    private Match markMatchAsFailed(Match match) {
        if (match.getEnded() == null) {
            match.setEnded(Instant.now());
        }

        match.setStatus(Match.Status.FAILED);
        match.setOutcome(Match.Outcome.DNF);
        return match;
    }

    private void cleanUp() {
        matchDir = null;
        stdOutFile = null;
        stdErrFile = null;
    }

    public static class MatchExecutionException extends RuntimeException {

        public MatchExecutionException(String message) {
            super(message);
        }

        public MatchExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
