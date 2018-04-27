package cern.ais.gridwars.web.controller.admin;

import cern.ais.gridwars.web.config.GridWarsProperties;
import cern.ais.gridwars.web.util.ModelAndViewBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Controller
public class SystemInfoAdminController extends BaseAdminController {

    private static final double MEGA_BYTE_FACTOR = 1024 * 1024;

    private final GridWarsProperties gridWarsProperties;

    public SystemInfoAdminController(GridWarsProperties gridWarsProperties) {
        this.gridWarsProperties = Objects.requireNonNull(gridWarsProperties);
    }

    @GetMapping("system")
    public ModelAndView systemInfo() {
        return ModelAndViewBuilder.forPage("admin/system")
            .addAttribute("systemInfo", createSystemInfo())
            .toModelAndView();
    }

    private SystemInfo createSystemInfo() {
        SystemInfo info = new SystemInfo();
        populateSystemHeapInfo(info);
        populateDiskUsageInfo(info);
        return info;
    }

    private void populateSystemHeapInfo(SystemInfo info) {
        final Runtime runtime = Runtime.getRuntime();
        info.setUsedHeapMb(toMegaBytes(runtime.totalMemory() - runtime.freeMemory()));
        info.setMaxHeapMb(toMegaBytes(runtime.maxMemory()));
        info.setServerLogFileNames(getServerLogFileNames());
    }

    private void populateDiskUsageInfo(SystemInfo info) {
        final String workDir = gridWarsProperties.getDirectories().getBaseWorkDir();
        final File workDirFile = new File(workDir);

        info.setWorkDir(workDir);
        info.setUsedDiskSpaceMb(toMegaBytes(workDirFile.getUsableSpace()));
        info.setMaxDiskSpaceMb(toMegaBytes(workDirFile.getTotalSpace()));
        info.setBotWorkDirSizeMb(toMegaBytes(getFolderSize(Paths.get(workDir, "bots"))));
        info.setMatchesWorkDirSizeMb(toMegaBytes(getFolderSize(Paths.get(workDir, "matches"))));
        info.setDbWorkDirSizeMb(toMegaBytes(getFolderSize(Paths.get(workDir, "db"))));
        info.setServerWorkDirSizeMb(toMegaBytes(getFolderSize(Paths.get(workDir, "server"))));
    }

    private double toMegaBytes(long value) {
        return ((double) value) / MEGA_BYTE_FACTOR;
    }

    private long getFolderSize(Path folderPath) {
        final AtomicLong size = new AtomicLong();

        try {
            Files.walkFileTree(folderPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    size.addAndGet(attrs.size());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ignored) {
           // can't happen because the visitor above doesn't throw IOException
        }

        return size.get();
    }

    private List<String> getServerLogFileNames() {
        final File logDir = Paths.get(gridWarsProperties.getDirectories().getBaseWorkDir(), "logs").toFile();
        if (!logDir.exists() || !logDir.canRead()) {
            return Collections.emptyList();
        }

        File[] logFiles = logDir.listFiles();
        if ((logFiles == null) || (logFiles.length == 0)) {
            return Collections.emptyList();
        }

        return Stream.of(logFiles)
            .filter(file -> file.length() > 0)
            .sorted((file1, file2) -> file2.getName().compareToIgnoreCase(file1.getName()))
            .map(File::getName)
            .collect(Collectors.toList());
    }

    public static final class SystemInfo {

        private String workDir;
        private double usedHeapMb;
        private double maxHeapMb;
        private double usedDiskSpaceMb;
        private double maxDiskSpaceMb;
        private double botWorkDirSizeMb;
        private double matchesWorkDirSizeMb;
        private double dbWorkDirSizeMb;
        private double serverWorkDirSizeMb;
        private List<String> serverLogFileNames;

        public String getWorkDir() {
            return workDir;
        }

        public SystemInfo setWorkDir(String workDir) {
            this.workDir = workDir;
            return this;
        }

        public double getUsedHeapMb() {
            return usedHeapMb;
        }

        public SystemInfo setUsedHeapMb(double usedHeapMb) {
            this.usedHeapMb = usedHeapMb;
            return this;
        }

        public double getMaxHeapMb() {
            return maxHeapMb;
        }

        public SystemInfo setMaxHeapMb(double maxHeapMb) {
            this.maxHeapMb = maxHeapMb;
            return this;
        }

        public double getHeapUsage() {
            return usedHeapMb / maxHeapMb * 100;
        }

        public double getUsedDiskSpaceMb() {
            return usedDiskSpaceMb;
        }

        public SystemInfo setUsedDiskSpaceMb(double usedDiskSpaceMb) {
            this.usedDiskSpaceMb = usedDiskSpaceMb;
            return this;
        }

        public double getMaxDiskSpaceMb() {
            return maxDiskSpaceMb;
        }

        public SystemInfo setMaxDiskSpaceMb(double maxDiskSpaceMb) {
            this.maxDiskSpaceMb = maxDiskSpaceMb;
            return this;
        }

        public double getDiskSpaceUsage() {
            return usedDiskSpaceMb / maxDiskSpaceMb * 100;
        }

        public double getBotWorkDirSizeMb() {
            return botWorkDirSizeMb;
        }

        public SystemInfo setBotWorkDirSizeMb(double botWorkDirSizeMb) {
            this.botWorkDirSizeMb = botWorkDirSizeMb;
            return this;
        }

        public double getMatchesWorkDirSizeMb() {
            return matchesWorkDirSizeMb;
        }

        public SystemInfo setMatchesWorkDirSizeMb(double matchesWorkDirSizeMb) {
            this.matchesWorkDirSizeMb = matchesWorkDirSizeMb;
            return this;
        }

        public double getDbWorkDirSizeMb() {
            return dbWorkDirSizeMb;
        }

        public SystemInfo setDbWorkDirSizeMb(double dbWorkDirSizeMb) {
            this.dbWorkDirSizeMb = dbWorkDirSizeMb;
            return this;
        }

        public double getServerWorkDirSizeMb() {
            return serverWorkDirSizeMb;
        }

        public SystemInfo setServerWorkDirSizeMb(double serverWorkDirSizeMb) {
            this.serverWorkDirSizeMb = serverWorkDirSizeMb;
            return this;
        }

        public double getWorkDirSizeMb() {
            return botWorkDirSizeMb + matchesWorkDirSizeMb + dbWorkDirSizeMb + serverWorkDirSizeMb;
        }

        public List<String> getServerLogFileNames() {
            return serverLogFileNames;
        }

        public SystemInfo setServerLogFileNames(List<String> serverLogFileNames) {
            this.serverLogFileNames = serverLogFileNames;
            return this;
        }
    }
}
