package org.taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io;

import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.WinPerfAsmProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.spf4j.stackmonitor.JmhFlightRecorderProfiler;
import org.spf4j.stackmonitor.JmhNewFlightRecorderProfiler;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class FileUtilsBenchmarkTest {
    //
    // Test tiny improvement to FileUtils::opyDirectory in
    // commons-io:commons-io:2.9.0-SNAPSHOT.
    //
    // I just removed duplicate call to File::getCanonicalPath.
    //
    // Would it show when using jmh for testing?
    //
    // I ran this code on my Dell Latitude E5550 laptop using Windows 10 
    // 64-bit with 16GB memory and an Intel(R) Core(TM) i7-5600 CPU @ 2.60GHz
    // using a LITEONIT LCS-256L9S-11 2.5 7mm 256GB disk drive
    // using JDK:
    // openjdk version "11.0.8" 2020-07-14 LTS
    // OpenJDK Runtime Environment Corretto-11.0.8.10.1 (build 11.0.8+10-LTS)
    // OpenJDK 64-Bit Server VM Corretto-11.0.8.10.1 (build 11.0.8+10-LTS, mixed mode)
    //
    // Results from two test runs:
    //
    //    Benchmark                                                                                  Mode  Cnt        Score       Error  Units
    //    taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.arunBenchmark       avgt  100    11083,590 ±   768,848  us/op
    //    taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.arunBenchmark1by1   avgt  100    51988,380 ± 11676,741  us/op
    //    taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.arunBenchmark2by2   avgt  100   204613,482 ± 37215,888  us/op
    //    taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.arunBenchmark3by3   avgt  100  1174152,673 ± 22541,114  us/op
    //    taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.dedupBenchmark1by1  avgt  100    31088,342 ±  1264,120  us/op
    //    taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.dedupBenchmark2by2  avgt  100   146071,467 ±  3787,178  us/op
    //    taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.dedupBenchmark3by3  avgt  100  1154023,563 ± 15089,337  us/op
    //    taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.deduppedBenchmark   avgt  100    14246,154 ±  2367,264  us/op
    //
    //    Benchmark                                                                                  Mode  Cnt        Score       Error  Units
    //    taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.arunBenchmark       avgt  100    11273,827 ±  2165,753  us/op
    //    taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.arunBenchmark1by1   avgt  100    32702,611 ±  1813,470  us/op
    //    taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.arunBenchmark2by2   avgt  100   152587,381 ±  4642,588  us/op
    //    taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.arunBenchmark3by3   avgt  100  1195478,564 ± 24303,450  us/op
    //    taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.dedupBenchmark1by1  avgt  100    30884,652 ±  1310,080  us/op
    //    taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.dedupBenchmark2by2  avgt  100   147020,471 ±  4613,101  us/op
    //    taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.dedupBenchmark3by3  avgt  100  1185322,846 ± 24429,620  us/op
    //    taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.deduppedBenchmark   avgt  100    11565,818 ±  1937,442  us/op
    //
    // On the whole, the deduppedBenchmarks seem to perform better, but I am definitely not an expert on this.
    //
    // In partcularly I cannot explain why the deduppedBenchmark benchmark seems to be slower than the arunBenchmark.
    //
    // Interesting sources:
    // http://tutorials.jenkov.com/java-performance/jmh.html
    // https://github.com/corretto/corretto-jmc/releases
    // https://docs.oracle.com/cd/E15289_01/JRCLR/optionxx.htm#JRCLR275 (Parameters for -XX:StartFlightRecording)
    // https://stackoverflow.com/questions/36807008/is-it-possible-to-run-a-jmh-benchmark-under-an-external-profiler#answer-37857708
    // https://github.com/zolyfarkas/spf4j
    // https://stackoverflow.com/questions/38926255/maven-annotation-processing-processor-not-found
    

    @Test
    public void testCopyDirectories() throws Exception {

        final String destinationFolder = System.getProperty("basedir",
                org.spf4j.base.Runtime.USER_DIR) + "/target";
        final String profile = System.getProperty("basedir",
                org.spf4j.base.Runtime.USER_DIR) + "/src/main/jfc/profile.jfc";
        //        final String profile = System.getProperty("basedir",
//                org.spf4j.base.Runtime.USER_DIR) + "/src/main/jfc/profile.jfc";
        Options opt = new OptionsBuilder()
                // Specify which benchmarks to run.
                // You can be more specific if you'd like to run only one benchmark per test.
                .include(this.getClass().getName() + ".*")
                .addProfiler(JmhNewFlightRecorderProfiler.class)
                .jvmArgs("-Xmx256m", "-Xms256m", // "-XX:+UnlockCommercialFeatures",
                        "-Djmh.stack.profiles=" + destinationFolder,
                        "-Djmh.executor=FJP",
                        "-Djmh.fr.options=defaultrecording=true,settings=" + profile)
                .result(destinationFolder + "/" + "benchmarkResults.csv")
                .resultFormat(ResultFormatType.CSV)
                // Set the following options as needed
                .mode (Mode.AverageTime)
                .timeUnit(TimeUnit.MICROSECONDS)
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(5)
                .measurementTime(TimeValue.milliseconds(15))
                .measurementIterations(100)
                .threads(1)
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(false)
                .build();

        new Runner(opt).run();
    }

    @State(Scope.Benchmark)
    public static class DirsEmpty {

        Path fromTempDir;
        Path toTempDir;
        File fromTempFile;
        File toTempFile;

        @Setup(Level.Invocation)
        public void setUp() throws IOException {
            fromTempDir = Files.createTempDirectory("benchmark-1");
            toTempDir = Files.createTempDirectory("benchmark-2");

            System.out.println( "Created fromTempDir " + fromTempDir.toString());
            System.out.println( "Created toTempDir " + toTempDir.toString());

            fromTempFile = fromTempDir.toFile();
            toTempFile = toTempDir.toFile();
        }

        @TearDown(Level.Invocation)
        public void tearDown() throws IOException {
            deleteDirectoryRecursively(fromTempDir);
            deleteDirectoryRecursively(toTempDir);
        }
    }

    @State(Scope.Benchmark)
    public static class Dirs1by1 {
        Path fromTempDir;
        Path toTempDir;
        File fromTempFile;
        File toTempFile;

        @Setup(Level.Invocation)
        public void setUp() throws IOException {
            fromTempDir = Files.createTempDirectory("benchmark-1");
            toTempDir = Files.createTempDirectory("benchmark-2");

            System.out.println( "Created fromTempDir " + fromTempDir.toString());
            System.out.println( "Created toTempDir " + toTempDir.toString());

            fill( fromTempDir, 1, 1, 1);

            fromTempFile = fromTempDir.toFile();
            toTempFile = toTempDir.toFile();
        }

        @TearDown(Level.Invocation)
        public void tearDown() throws IOException {
            deleteDirectoryRecursively(fromTempDir);
            deleteDirectoryRecursively(toTempDir);
        }
    }

    @State(Scope.Benchmark)
    public static class Dirs2by2 {
        Path fromTempDir;
        Path toTempDir;
        File fromTempFile;
        File toTempFile;

        @Setup(Level.Invocation)
        public void setUp() throws IOException {
            fromTempDir = Files.createTempDirectory("benchmark-1");
            toTempDir = Files.createTempDirectory("benchmark-2");

            System.out.println( "Created fromTempDir " + fromTempDir.toString());
            System.out.println( "Created toTempDir " + toTempDir.toString());

            fill( fromTempDir, 2, 2, 2);

            fromTempFile = fromTempDir.toFile();
            toTempFile = toTempDir.toFile();
        }

        @TearDown(Level.Invocation)
        public void tearDown() throws IOException {
            deleteDirectoryRecursively(fromTempDir);
            deleteDirectoryRecursively(toTempDir);
        }
    }

    @State(Scope.Benchmark)
    public static class Dirs3by3 {
        Path fromTempDir;
        Path toTempDir;
        File fromTempFile;
        File toTempFile;

        @Setup(Level.Invocation)
        public void setUp() throws IOException {
            fromTempDir = Files.createTempDirectory("benchmark-1");
            toTempDir = Files.createTempDirectory("benchmark-2");

            System.out.println( "Created fromTempDir " + fromTempDir.toString());
            System.out.println( "Created toTempDir " + toTempDir.toString());

            fill( fromTempDir, 3, 3, 3);

            fromTempFile = fromTempDir.toFile();
            toTempFile = toTempDir.toFile();
        }

        @TearDown(Level.Invocation)
        public void tearDown() throws IOException {
            deleteDirectoryRecursively(fromTempDir);
            deleteDirectoryRecursively(toTempDir);
        }
    }

    private static void deleteDirectoryRecursively(Path pathToBeDeleted) {
        System.out.println( "Deleting " + pathToBeDeleted.toString());

        try {
            Files.walk(pathToBeDeleted)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);

            Files.deleteIfExists(pathToBeDeleted);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Benchmark
    public void dedupBenchmark3by3(Dirs3by3 dirs3by3) throws IOException {
        final File fromTempFile = dirs3by3.fromTempFile;
        final File toTempFile = dirs3by3.toTempFile;

        deduppedCopyDirectory(fromTempFile, toTempFile, null);
    }

    @Benchmark
    public void arunBenchmark3by3(Dirs3by3 dirs3by3) throws IOException {
        final File fromTempFile = dirs3by3.fromTempFile;
        final File toTempFile = dirs3by3.toTempFile;

        copyDirectory(fromTempFile, toTempFile, null);
    }

    @Benchmark
    public void dedupBenchmark2by2(Dirs2by2 dirs2by2) throws IOException {
        final File fromTempFile = dirs2by2.fromTempFile;
        final File toTempFile = dirs2by2.toTempFile;

        deduppedCopyDirectory(fromTempFile, toTempFile, null);
    }

    @Benchmark
    public void arunBenchmark2by2(Dirs2by2 dirs2by2) throws IOException {
        final File fromTempFile = dirs2by2.fromTempFile;
        final File toTempFile = dirs2by2.toTempFile;

        copyDirectory(fromTempFile, toTempFile, null);
    }

    @Benchmark
    public void dedupBenchmark1by1(Dirs1by1 dirs1by1) throws IOException {
        final File fromTempFile = dirs1by1.fromTempFile;
        final File toTempFile = dirs1by1.toTempFile;

        deduppedCopyDirectory(fromTempFile, toTempFile, null);
    }

    @Benchmark
    public void arunBenchmark1by1(Dirs1by1 dirs1by1) throws IOException {
        final File fromTempFile = dirs1by1.fromTempFile;
        final File toTempFile = dirs1by1.toTempFile;

        copyDirectory(fromTempFile, toTempFile, null);
    }

    @Benchmark
    public void deduppedBenchmark( DirsEmpty dirsEmpty) throws IOException {
        deduppedCopyDirectory(dirsEmpty.fromTempFile, dirsEmpty.toTempFile, null, true);
    }

    @Benchmark
    public void arunBenchmark( DirsEmpty dirsEmpty) throws IOException {
        copyDirectory(dirsEmpty.fromTempFile, dirsEmpty.toTempFile, null, true);
    }

    private static void fill(Path fromTempDir, int depth, int nrSubDirs, int nrFiles) throws IOException {
        for(int i=0;i<nrFiles;i++) {
            Files.createTempFile( fromTempDir, "benchmark-" + i, ".txt");
        }
        if( depth > 0) {
            for (int j = 0; j < nrSubDirs; j++) {
                Path newTempDir = Files.createTempDirectory(fromTempDir, "benchmark-" + j);
                fill(newTempDir, depth-1, nrSubDirs, nrFiles);
            }
        }
    }

    //
    // dedupped commit 1d59399d6ddb1bf876e67fe3be61b91888ebf00a
    //


    /**
     * Copies a filtered directory to a new location preserving the file dates.
     * <p>
     * This method copies the contents of the specified source directory
     * to within the specified destination directory.
     * </p>
     * <p>
     * The destination directory is created if it does not exist.
     * If the destination directory did exist, then this method merges
     * the source with the destination, with the source taking precedence.
     * </p>
     * <p>
     * <strong>Note:</strong> This method tries to preserve the files' last
     * modified date/times using {@link File#setLastModified(long)}, however
     * it is not guaranteed that those operations will succeed.
     * If the modification operation fails, no indication is provided.
     * </p>
     * <b>Example: Copy directories only</b>
     * <pre>
     *  // only copy the directory structure
     *  FileUtils.copyDirectory(srcDir, destDir, DirectoryFileFilter.DIRECTORY);
     *  </pre>
     *
     * <b>Example: Copy directories and txt files</b>
     * <pre>
     *  // Create a filter for ".txt" files
     *  IOFileFilter txtSuffixFilter = FileFilterUtils.suffixFileFilter(".txt");
     *  IOFileFilter txtFiles = FileFilterUtils.andFileFilter(FileFileFilter.FILE, txtSuffixFilter);
     *
     *  // Create a filter for either directories or ".txt" files
     *  FileFilter filter = FileFilterUtils.orFileFilter(DirectoryFileFilter.DIRECTORY, txtFiles);
     *
     *  // Copy using the filter
     *  FileUtils.copyDirectory(srcDir, destDir, filter);
     *  </pre>
     *
     * @param srcDir  an existing directory to copy, must not be {@code null}
     * @param destDir the new directory, must not be {@code null}
     * @param filter  the filter to apply, null means copy all directories and files
     *                should be the same as the original
     *
     * @throws NullPointerException if source or destination is {@code null}
     * @throws IOException          if source or destination is invalid
     * @throws IOException          if an IO error occurs during copying
     * @since 1.4
     */
    public static void deduppedCopyDirectory(final File srcDir, final File destDir,
                                     final FileFilter filter) throws IOException {
        deduppedCopyDirectory(srcDir, destDir, filter, true);
    }

    /**
     * Copies a filtered directory to a new location.
     * <p>
     * This method copies the contents of the specified source directory
     * to within the specified destination directory.
     * </p>
     * <p>
     *   The destination directory is created if it does not exist.
     * If the destination directory did exist, then this method merges
     * the source with the destination, with the source taking precedence.
     * </p>
     * <p>
     * <strong>Note:</strong> Setting <code>preserveFileDate</code> to
     * {@code true} tries to preserve the files' last modified
     * date/times using {@link File#setLastModified(long)}, however it is
     * not guaranteed that those operations will succeed.
     * If the modification operation fails, no indication is provided.
     * </p>
     * <b>Example: Copy directories only</b>
     * <pre>
     *  // only copy the directory structure
     *  FileUtils.copyDirectory(srcDir, destDir, DirectoryFileFilter.DIRECTORY, false);
     *  </pre>
     *
     * <b>Example: Copy directories and txt files</b>
     * <pre>
     *  // Create a filter for ".txt" files
     *  IOFileFilter txtSuffixFilter = FileFilterUtils.suffixFileFilter(".txt");
     *  IOFileFilter txtFiles = FileFilterUtils.andFileFilter(FileFileFilter.FILE, txtSuffixFilter);
     *
     *  // Create a filter for either directories or ".txt" files
     *  FileFilter filter = FileFilterUtils.orFileFilter(DirectoryFileFilter.DIRECTORY, txtFiles);
     *
     *  // Copy using the filter
     *  FileUtils.copyDirectory(srcDir, destDir, filter, false);
     *  </pre>
     *
     * @param srcDir           an existing directory to copy, must not be {@code null}
     * @param destDir          the new directory, must not be {@code null}
     * @param filter           the filter to apply, null means copy all directories and files
     * @param preserveFileDate true if the file date of the copy
     *                         should be the same as the original
     *
     * @throws NullPointerException if source or destination is {@code null}
     * @throws IOException          if source or destination is invalid
     * @throws IOException          if an IO error occurs during copying
     * @since 1.4
     */
    public static void deduppedCopyDirectory(final File srcDir, final File destDir,
                                     final FileFilter filter, final boolean preserveFileDate) throws IOException {
        checkFileRequirements(srcDir, destDir);
        if (!srcDir.isDirectory()) {
            throw new IOException("Source '" + srcDir + "' exists but is not a directory");
        }
        final String srcDirCanonicalPath = srcDir.getCanonicalPath();
        final String destDirCanonicalPath = destDir.getCanonicalPath();
        if (srcDirCanonicalPath.equals(destDirCanonicalPath)) {
            throw new IOException("Source '" + srcDir + "' and destination '" + destDir + "' are the same");
        }

        // Cater for destination being directory within the source directory (see IO-141)
        List<String> exclusionList = null;
        if (destDirCanonicalPath.startsWith(srcDirCanonicalPath)) {
            final File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
            if (srcFiles != null && srcFiles.length > 0) {
                exclusionList = new ArrayList<>(srcFiles.length);
                for (final File srcFile : srcFiles) {
                    final File copiedFile = new File(destDir, srcFile.getName());
                    exclusionList.add(copiedFile.getCanonicalPath());
                }
            }
        }
        doCopyDirectory(srcDir, destDir, filter, preserveFileDate, exclusionList);
    }

    //
    //commit 1d59399d6ddb1bf876e67fe3be61b91888ebf00a
    //


    /**
     * Copies a filtered directory to a new location preserving the file dates.
     * <p>
     * This method copies the contents of the specified source directory
     * to within the specified destination directory.
     * </p>
     * <p>
     * The destination directory is created if it does not exist.
     * If the destination directory did exist, then this method merges
     * the source with the destination, with the source taking precedence.
     * </p>
     * <p>
     * <strong>Note:</strong> This method tries to preserve the files' last
     * modified date/times using {@link File#setLastModified(long)}, however
     * it is not guaranteed that those operations will succeed.
     * If the modification operation fails, no indication is provided.
     * </p>
     * <b>Example: Copy directories only</b>
     * <pre>
     *  // only copy the directory structure
     *  FileUtils.copyDirectory(srcDir, destDir, DirectoryFileFilter.DIRECTORY);
     *  </pre>
     *
     * <b>Example: Copy directories and txt files</b>
     * <pre>
     *  // Create a filter for ".txt" files
     *  IOFileFilter txtSuffixFilter = FileFilterUtils.suffixFileFilter(".txt");
     *  IOFileFilter txtFiles = FileFilterUtils.andFileFilter(FileFileFilter.FILE, txtSuffixFilter);
     *
     *  // Create a filter for either directories or ".txt" files
     *  FileFilter filter = FileFilterUtils.orFileFilter(DirectoryFileFilter.DIRECTORY, txtFiles);
     *
     *  // Copy using the filter
     *  FileUtils.copyDirectory(srcDir, destDir, filter);
     *  </pre>
     *
     * @param srcDir  an existing directory to copy, must not be {@code null}
     * @param destDir the new directory, must not be {@code null}
     * @param filter  the filter to apply, null means copy all directories and files
     *                should be the same as the original
     *
     * @throws NullPointerException if source or destination is {@code null}
     * @throws IOException          if source or destination is invalid
     * @throws IOException          if an IO error occurs during copying
     * @since 1.4
     */
    public static void copyDirectory(final File srcDir, final File destDir,
                                     final FileFilter filter) throws IOException {
        copyDirectory(srcDir, destDir, filter, true);
    }

    /**
     * Copies a filtered directory to a new location.
     * <p>
     * This method copies the contents of the specified source directory
     * to within the specified destination directory.
     * </p>
     * <p>
     *   The destination directory is created if it does not exist.
     * If the destination directory did exist, then this method merges
     * the source with the destination, with the source taking precedence.
     * </p>
     * <p>
     * <strong>Note:</strong> Setting <code>preserveFileDate</code> to
     * {@code true} tries to preserve the files' last modified
     * date/times using {@link File#setLastModified(long)}, however it is
     * not guaranteed that those operations will succeed.
     * If the modification operation fails, no indication is provided.
     * </p>
     * <b>Example: Copy directories only</b>
     * <pre>
     *  // only copy the directory structure
     *  FileUtils.copyDirectory(srcDir, destDir, DirectoryFileFilter.DIRECTORY, false);
     *  </pre>
     *
     * <b>Example: Copy directories and txt files</b>
     * <pre>
     *  // Create a filter for ".txt" files
     *  IOFileFilter txtSuffixFilter = FileFilterUtils.suffixFileFilter(".txt");
     *  IOFileFilter txtFiles = FileFilterUtils.andFileFilter(FileFileFilter.FILE, txtSuffixFilter);
     *
     *  // Create a filter for either directories or ".txt" files
     *  FileFilter filter = FileFilterUtils.orFileFilter(DirectoryFileFilter.DIRECTORY, txtFiles);
     *
     *  // Copy using the filter
     *  FileUtils.copyDirectory(srcDir, destDir, filter, false);
     *  </pre>
     *
     * @param srcDir           an existing directory to copy, must not be {@code null}
     * @param destDir          the new directory, must not be {@code null}
     * @param filter           the filter to apply, null means copy all directories and files
     * @param preserveFileDate true if the file date of the copy
     *                         should be the same as the original
     *
     * @throws NullPointerException if source or destination is {@code null}
     * @throws IOException          if source or destination is invalid
     * @throws IOException          if an IO error occurs during copying
     * @since 1.4
     */
    public static void copyDirectory(final File srcDir, final File destDir,
                                     final FileFilter filter, final boolean preserveFileDate) throws IOException {
        checkFileRequirements(srcDir, destDir);
        if (!srcDir.isDirectory()) {
            throw new IOException("Source '" + srcDir + "' exists but is not a directory");
        }
        if (srcDir.getCanonicalPath().equals(destDir.getCanonicalPath())) {
            throw new IOException("Source '" + srcDir + "' and destination '" + destDir + "' are the same");
        }

        // Cater for destination being directory within the source directory (see IO-141)
        List<String> exclusionList = null;
        if (destDir.getCanonicalPath().startsWith(srcDir.getCanonicalPath())) {
            final File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
            if (srcFiles != null && srcFiles.length > 0) {
                exclusionList = new ArrayList<>(srcFiles.length);
                for (final File srcFile : srcFiles) {
                    final File copiedFile = new File(destDir, srcFile.getName());
                    exclusionList.add(copiedFile.getCanonicalPath());
                }
            }
        }
        doCopyDirectory(srcDir, destDir, filter, preserveFileDate, exclusionList);
    }

    /**
     * Internal copy directory method.
     *
     * @param srcDir           the validated source directory, must not be {@code null}
     * @param destDir          the validated destination directory, must not be {@code null}
     * @param filter           the filter to apply, null means copy all directories and files
     * @param preserveFileDate whether to preserve the file date
     * @param exclusionList    List of files and directories to exclude from the copy, may be null
     * @throws IOException if an error occurs
     * @since 1.1
     */
    private static void doCopyDirectory(final File srcDir, final File destDir, final FileFilter filter,
                                        final boolean preserveFileDate, final List<String> exclusionList)
            throws IOException {
        // recurse
        final File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
        if (srcFiles == null) {  // null if abstract pathname does not denote a directory, or if an I/O error occurs
            throw new IOException("Failed to list contents of " + srcDir);
        }
        if (destDir.exists()) {
            if (destDir.isDirectory() == false) {
                throw new IOException("Destination '" + destDir + "' exists but is not a directory");
            }
        } else {
            if (!destDir.mkdirs() && !destDir.isDirectory()) {
                throw new IOException("Destination '" + destDir + "' directory cannot be created");
            }
        }
        if (destDir.canWrite() == false) {
            throw new IOException("Destination '" + destDir + "' cannot be written to");
        }
        for (final File srcFile : srcFiles) {
            final File dstFile = new File(destDir, srcFile.getName());
            if (exclusionList == null || !exclusionList.contains(srcFile.getCanonicalPath())) {
                if (srcFile.isDirectory()) {
                    doCopyDirectory(srcFile, dstFile, filter, preserveFileDate, exclusionList);
                } else {
                    doCopyFile(srcFile, dstFile, preserveFileDate);
                }
            }
        }

        // Do this last, as the above has probably affected directory metadata
        if (preserveFileDate) {
            destDir.setLastModified(srcDir.lastModified());
        }
    }

    /**
     * Checks requirements for file copy.
     *
     * @param source the source file
     * @param destination the destination
     * @throws FileNotFoundException if the destination does not exist
     */
    private static void checkFileRequirements(final File source, final File destination) throws FileNotFoundException {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(destination, "target");
        if (!source.exists()) {
            throw new FileNotFoundException("Source '" + source + "' does not exist");
        }
    }


    /**
     * Internal copy file method.
     * This uses the original file length, and throws an IOException
     * if the output file length is different from the current input file length.
     * So it may fail if the file changes size.
     * It may also fail with "IllegalArgumentException: Negative size" if the input file is truncated part way
     * through copying the data and the new file size is less than the current position.
     *
     * @param srcFile          the validated source file, must not be {@code null}
     * @param destFile         the validated destination file, must not be {@code null}
     * @param preserveFileDate whether to preserve the file date
     * @throws IOException              if an error occurs
     * @throws IOException              if the output file length is not the same as the input file length after the
     * copy completes
     * @throws IllegalArgumentException "Negative size" if the file is truncated so that the size is less than the
     * position
     */
    private static void doCopyFile(final File srcFile, final File destFile, final boolean preserveFileDate)
            throws IOException {
        if (destFile.exists() && destFile.isDirectory()) {
            throw new IOException("Destination '" + destFile + "' exists but is a directory");
        }

        final Path srcPath = srcFile.toPath();
        final Path destPath = destFile.toPath();
        final long newLastModifed = preserveFileDate ? srcFile.lastModified() : destFile.lastModified();
        Files.copy(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);

        // TODO IO-386: Do we still need this check?
        checkEqualSizes(srcFile, destFile, Files.size(srcPath), Files.size(destPath));
        // TODO IO-386: Do we still need this check?
        checkEqualSizes(srcFile, destFile, srcFile.length(), destFile.length());

        destFile.setLastModified(newLastModifed);
    }

    /**
     * Checks that two file lengths are equal.
     *
     * @param srcFile Source file.
     * @param destFile Destination file.
     * @param srcLen Source file length.
     * @param dstLen Destination file length
     * @throws IOException Thrown when the given sizes are not equal.
     */
    private static void checkEqualSizes(final File srcFile, final File destFile, final long srcLen, final long dstLen)
            throws IOException {
        if (srcLen != dstLen) {
            throw new IOException("Failed to copy full contents from '" + srcFile + "' to '" + destFile
                    + "' Expected length: " + srcLen + " Actual: " + dstLen);
        }
    }
}
