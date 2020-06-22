package org.taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io;

import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.*;

public class FileUtilsBenchmarkTest {

    @Test
    public void testCopyDirectories() throws Exception {
        Options opt = new OptionsBuilder()
                // Specify which benchmarks to run.
                // You can be more specific if you'd like to run only one benchmark per test.
                .include(this.getClass().getName() + ".*")
                // Set the following options as needed
                .mode (Mode.AverageTime)
                .timeUnit(TimeUnit.MICROSECONDS)
                .warmupTime(TimeValue.seconds(2))
                .warmupIterations(5)
                .measurementTime(TimeValue.seconds(2))
                .measurementIterations(50)
                .threads(1)
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                //.jvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintInlining")
                //.addProfiler(WinPerfAsmProfiler.class)
                .build();

        new Runner(opt).run();
    }

    @Benchmark
    public void runBenchmark() throws IOException {
        Path fromTempDir = Files.createTempDirectory("benchmark-1");
        Path toTempDir = Files.createTempDirectory("benchmark-2");

        final File fromTempFile = fromTempDir.toFile();
        final File toTempFile = toTempDir.toFile();

        copyDirectory(fromTempFile, toTempFile, null);

        deleteDirectoryRecursively(fromTempDir);
        deleteDirectoryRecursively(toTempDir);
    }

    private void deleteDirectoryRecursively(Path fromTempDir) throws IOException {
        Files.walk(fromTempDir)
            .map(Path::toFile).forEach(File::delete);
    }

    @Benchmark
    public void deduppedBenchmark() throws IOException {
        Path fromTempDir = Files.createTempDirectory("benchmark-1");
        Path toTempDir = Files.createTempDirectory("benchmark-2");

        final File fromTempFile = fromTempDir.toFile();
        final File toTempFile = toTempDir.toFile();

        deduppedCopyDirectory(fromTempFile, toTempFile, null);

        deleteDirectoryRecursively(fromTempDir);
        deleteDirectoryRecursively(toTempDir);
    }

    @Benchmark
    public void runBenchmark3by3() throws IOException {
        Path fromTempDir = Files.createTempDirectory("benchmark-1");

        fill( fromTempDir, 3, 3);

        Path toTempDir = Files.createTempDirectory("benchmark-2");

        final File fromTempFile = fromTempDir.toFile();
        final File toTempFile = toTempDir.toFile();

        copyDirectory(fromTempFile, toTempFile, null);

        deleteDirectoryRecursively(fromTempDir);
        deleteDirectoryRecursively(toTempDir);
    }

    @Benchmark
    public void dedupBenchmark3by3() throws IOException {
        Path fromTempDir = Files.createTempDirectory("benchmark-1");

        fill( fromTempDir, 3, 3);

        Path toTempDir = Files.createTempDirectory("benchmark-2");

        final File fromTempFile = fromTempDir.toFile();
        final File toTempFile = toTempDir.toFile();

        copyDirectory(fromTempFile, toTempFile, null);

        deleteDirectoryRecursively(fromTempDir);
        deleteDirectoryRecursively(toTempDir);
    }

    private void fill(Path fromTempDir, int nrSubDirs, int nrFiles) throws IOException {
        for(int i=0;i<nrFiles;i++) {
            Files.createTempFile( fromTempDir, "benchmark-" + i, ".txt");
        }
        final int nrSubSubDirs = nrSubDirs - 1;
        for( int j=0;j<nrSubDirs;j++) {
            Path newTempDir = Files.createTempDirectory("benchmark-2");
            fill( newTempDir, nrSubSubDirs, nrFiles);
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
