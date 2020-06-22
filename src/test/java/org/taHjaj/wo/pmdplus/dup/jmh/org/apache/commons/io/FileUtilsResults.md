# Results

I used PmdPlus on apache commons io 2.7.1-SNAPSHOT and found that 

```
copyDirectory(final File srcDir, final File destDir, final FileFilter filter, final boolean preserveFileDate)
```

executes getCanonicalPath() multiple times on the same path. I might be wrong, but that
seems wasteful.

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Forking command line: cmd.exe /X /C ""C:\Program Files\Amazon Corretto\jdk11.0.7_10\bin\java" -jar C:\try\PmdPlus\target\surefire\surefirebooter3813109799777338655.jar C:\try\PmdPlus\target\surefire\surefire15806943013007607170tmp C:\try\PmdPlus\target\surefire\surefire_011145740502747984006tmp"
Running org.taHjaj.wo.pmdplus.dup.DupTest
Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.266 sec
Running org.taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest
WARNING: An illegal reflective access operation has occurred
WARNING: Illegal reflective access by org.openjdk.jmh.util.Utils (file:/C:/Users/NL02032/.m2/repository/org/openjdk/jmh/jmh-core/1.23/jmh-core-1.23.jar) to field java.io.PrintStream.charOut
WARNING: Please consider reporting this to the maintainers of org.openjdk.jmh.util.Utils
WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations
WARNING: All illegal access operations will be denied in a future release
# JMH version: 1.23
# VM version: JDK 11.0.7, OpenJDK 64-Bit Server VM, 11.0.7+10-LTS
# VM invoker: C:\Program Files\Amazon Corretto\jdk11.0.7_10\bin\java.exe
# VM options: <none>
# Warmup: 5 iterations, 2 s each
# Measurement: 50 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: org.taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.dedupBenchmark3by3

# Run progress: 0,00% complete, ETA 00:07:20
# Fork: 1 of 1
# Warmup Iteration   1: 2505222,700 us/op
# Warmup Iteration   2: 274281,775 us/op
# Warmup Iteration   3: 257045,488 us/op
# Warmup Iteration   4: 236497,367 us/op
# Warmup Iteration   5: 235425,833 us/op
Iteration   1: 228561,956 us/op
Iteration   2: 235979,678 us/op
Iteration   3: 233773,056 us/op
Iteration   4: 228602,667 us/op
Iteration   5: 209134,300 us/op
Iteration   6: 228702,000 us/op
Iteration   7: 229314,122 us/op
Iteration   8: 203792,800 us/op
Iteration   9: 226406,322 us/op
Iteration  10: 281838,913 us/op
Iteration  11: 211456,400 us/op
Iteration  12: 241928,133 us/op
Iteration  13: 210685,750 us/op
Iteration  14: 215438,920 us/op
Iteration  15: 217684,240 us/op
Iteration  16: 194632,764 us/op
Iteration  17: 222823,240 us/op
Iteration  18: 198928,664 us/op
Iteration  19: 202096,320 us/op
Iteration  20: 221337,200 us/op
Iteration  21: 200198,010 us/op
Iteration  22: 223009,789 us/op
Iteration  23: 217594,870 us/op
Iteration  24: 212335,830 us/op
Iteration  25: 261499,150 us/op
Iteration  26: 199055,909 us/op
Iteration  27: 225129,156 us/op
Iteration  28: 217407,430 us/op
Iteration  29: 202191,327 us/op
Iteration  30: 225004,022 us/op
Iteration  31: 198587,409 us/op
Iteration  32: 237637,389 us/op
Iteration  33: 211714,300 us/op
Iteration  34: 204810,360 us/op
Iteration  35: 230830,844 us/op
Iteration  36: 207138,420 us/op
Iteration  37: 217874,050 us/op
Iteration  38: 210825,030 us/op
Iteration  39: 205160,440 us/op
Iteration  40: 231119,078 us/op
Iteration  41: 198823,545 us/op
Iteration  42: 218508,100 us/op
Iteration  43: 215308,410 us/op
Iteration  44: 200371,510 us/op
Iteration  45: 229016,178 us/op
Iteration  46: 201359,560 us/op
Iteration  47: 211417,390 us/op
Iteration  48: 228378,300 us/op
Iteration  49: 195472,791 us/op
Iteration  50: 228673,522 us/op


Result "org.taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.dedupBenchmark3by3":
  218191,391 ±(99.9%) 8312,200 us/op [Average]
  (min, avg, max) = (194632,764, 218191,391, 281838,913), stdev = 16791,055
  CI (99.9%): [209879,191, 226503,591] (assumes normal distribution)


# JMH version: 1.23
# VM version: JDK 11.0.7, OpenJDK 64-Bit Server VM, 11.0.7+10-LTS
# VM invoker: C:\Program Files\Amazon Corretto\jdk11.0.7_10\bin\java.exe
# VM options: <none>
# Warmup: 5 iterations, 2 s each
# Measurement: 50 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: org.taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.deduppedBenchmark

# Run progress: 25,00% complete, ETA 00:07:38
# Fork: 1 of 1
# Warmup Iteration   1: 2039346,700 us/op
# Warmup Iteration   2: 16222,484 us/op
# Warmup Iteration   3: 13827,650 us/op
# Warmup Iteration   4: 14117,772 us/op
# Warmup Iteration   5: 15696,365 us/op
Iteration   1: 13067,042 us/op
Iteration   2: 14965,751 us/op
Iteration   3: 14244,302 us/op
Iteration   4: 12265,499 us/op
Iteration   5: 15178,989 us/op
Iteration   6: 14374,541 us/op
Iteration   7: 15212,211 us/op
Iteration   8: 15526,316 us/op
Iteration   9: 15782,736 us/op
Iteration  10: 16143,118 us/op
Iteration  11: 15309,785 us/op
Iteration  12: 16049,046 us/op
Iteration  13: 15726,445 us/op
Iteration  14: 15630,567 us/op
Iteration  15: 15783,707 us/op
Iteration  16: 16291,984 us/op
Iteration  17: 16279,714 us/op
Iteration  18: 15785,679 us/op
Iteration  19: 16376,282 us/op
Iteration  20: 15560,288 us/op
Iteration  21: 20001,114 us/op
Iteration  22: 15510,593 us/op
Iteration  23: 15714,058 us/op
Iteration  24: 15382,122 us/op
Iteration  25: 15750,469 us/op
Iteration  26: 16540,600 us/op
Iteration  27: 17258,881 us/op
Iteration  28: 15817,941 us/op
Iteration  29: 15846,717 us/op
Iteration  30: 15356,667 us/op
Iteration  31: 15832,976 us/op
Iteration  32: 15706,537 us/op
Iteration  33: 15432,781 us/op
Iteration  34: 15304,489 us/op
Iteration  35: 15545,937 us/op
Iteration  36: 15088,870 us/op
Iteration  37: 15724,549 us/op
Iteration  38: 15649,793 us/op
Iteration  39: 15373,422 us/op
Iteration  40: 15170,760 us/op
Iteration  41: 15397,729 us/op
Iteration  42: 15812,252 us/op
Iteration  43: 16537,583 us/op
Iteration  44: 15483,738 us/op
Iteration  45: 15597,727 us/op
Iteration  46: 15458,264 us/op
Iteration  47: 15850,687 us/op
Iteration  48: 15400,673 us/op
Iteration  49: 15427,954 us/op
Iteration  50: 15511,161 us/op


Result "org.taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.deduppedBenchmark":
  15600,821 ±(99.9%) 494,818 us/op [Average]
  (min, avg, max) = (12265,499, 15600,821, 20001,114), stdev = 999,558
  CI (99.9%): [15106,003, 16095,639] (assumes normal distribution)


# JMH version: 1.23
# VM version: JDK 11.0.7, OpenJDK 64-Bit Server VM, 11.0.7+10-LTS
# VM invoker: C:\Program Files\Amazon Corretto\jdk11.0.7_10\bin\java.exe
# VM options: <none>
# Warmup: 5 iterations, 2 s each
# Measurement: 50 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: org.taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.runBenchmark

# Run progress: 50,00% complete, ETA 00:04:59
# Fork: 1 of 1
# Warmup Iteration   1: 2289507,200 us/op
# Warmup Iteration   2: 16541,888 us/op
# Warmup Iteration   3: 15827,765 us/op
# Warmup Iteration   4: 15453,865 us/op
# Warmup Iteration   5: 15804,757 us/op
Iteration   1: 15754,324 us/op
Iteration   2: 15155,139 us/op
Iteration   3: 15462,106 us/op
Iteration   4: 15997,186 us/op
Iteration   5: 15259,827 us/op
Iteration   6: 17197,887 us/op
Iteration   7: 15660,077 us/op
Iteration   8: 16039,157 us/op
Iteration   9: 15838,763 us/op
Iteration  10: 16147,786 us/op
Iteration  11: 15839,527 us/op
Iteration  12: 15551,157 us/op
Iteration  13: 15330,545 us/op
Iteration  14: 15566,733 us/op
Iteration  15: 16043,626 us/op
Iteration  16: 16476,516 us/op
Iteration  17: 16057,408 us/op
Iteration  18: 15961,827 us/op
Iteration  19: 15423,800 us/op
Iteration  20: 15413,505 us/op
Iteration  21: 15243,618 us/op
Iteration  22: 15616,458 us/op
Iteration  23: 15512,847 us/op
Iteration  24: 15626,426 us/op
Iteration  25: 15678,030 us/op
Iteration  26: 16816,772 us/op
Iteration  27: 16953,292 us/op
Iteration  28: 15812,193 us/op
Iteration  29: 15440,415 us/op
Iteration  30: 15917,971 us/op
Iteration  31: 16189,444 us/op
Iteration  32: 15464,505 us/op
Iteration  33: 15632,641 us/op
Iteration  34: 15573,993 us/op
Iteration  35: 15455,549 us/op
Iteration  36: 15401,082 us/op
Iteration  37: 15777,476 us/op
Iteration  38: 15784,617 us/op
Iteration  39: 15655,544 us/op
Iteration  40: 15807,753 us/op
Iteration  41: 15379,131 us/op
Iteration  42: 15168,959 us/op
Iteration  43: 15561,284 us/op
Iteration  44: 15408,413 us/op
Iteration  45: 15831,792 us/op
Iteration  46: 15361,035 us/op
Iteration  47: 15495,122 us/op
Iteration  48: 15417,152 us/op
Iteration  49: 15416,524 us/op
Iteration  50: 15287,300 us/op


Result "org.taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.runBenchmark":
  15717,285 ±(99.9%) 214,062 us/op [Average]
  (min, avg, max) = (15155,139, 15717,285, 17197,887), stdev = 432,417
  CI (99.9%): [15503,222, 15931,347] (assumes normal distribution)


# JMH version: 1.23
# VM version: JDK 11.0.7, OpenJDK 64-Bit Server VM, 11.0.7+10-LTS
# VM invoker: C:\Program Files\Amazon Corretto\jdk11.0.7_10\bin\java.exe
# VM options: <none>
# Warmup: 5 iterations, 2 s each
# Measurement: 50 iterations, 2 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: org.taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.runBenchmark3by3

# Run progress: 75,00% complete, ETA 00:02:28
# Fork: 1 of 1
# Warmup Iteration   1: 2633157,400 us/op
# Warmup Iteration   2: 251554,175 us/op
# Warmup Iteration   3: 228272,878 us/op
# Warmup Iteration   4: 238206,644 us/op
# Warmup Iteration   5: 233196,311 us/op
Iteration   1: 234990,211 us/op
Iteration   2: 229812,822 us/op
Iteration   3: 241654,778 us/op
Iteration   4: 229527,267 us/op
Iteration   5: 230139,878 us/op
Iteration   6: 242994,778 us/op
Iteration   7: 237244,456 us/op
Iteration   8: 222082,620 us/op
Iteration   9: 227544,856 us/op
Iteration  10: 224174,722 us/op
Iteration  11: 229094,411 us/op
Iteration  12: 223679,144 us/op
Iteration  13: 230761,444 us/op
Iteration  14: 222548,344 us/op
Iteration  15: 224589,756 us/op
Iteration  16: 229371,878 us/op
Iteration  17: 231369,789 us/op
Iteration  18: 210402,740 us/op
Iteration  19: 222362,111 us/op
Iteration  20: 213328,470 us/op
Iteration  21: 210990,890 us/op
Iteration  22: 217730,920 us/op
Iteration  23: 198578,409 us/op
Iteration  24: 228447,322 us/op
Iteration  25: 207828,945 us/op
Iteration  26: 237210,700 us/op
Iteration  27: 198466,664 us/op
Iteration  28: 227198,200 us/op
Iteration  29: 208160,630 us/op
Iteration  30: 204566,930 us/op
Iteration  31: 229914,489 us/op
Iteration  32: 202370,090 us/op
Iteration  33: 219774,410 us/op
Iteration  34: 213960,930 us/op
Iteration  35: 215134,760 us/op
Iteration  36: 218675,640 us/op
Iteration  37: 208077,710 us/op
Iteration  38: 214481,700 us/op
Iteration  39: 207333,840 us/op
Iteration  40: 201022,780 us/op
Iteration  41: 225686,322 us/op
Iteration  42: 207490,900 us/op
Iteration  43: 220969,570 us/op
Iteration  44: 233209,500 us/op
Iteration  45: 200736,280 us/op
Iteration  46: 235961,678 us/op
Iteration  47: 202825,060 us/op
Iteration  48: 211117,430 us/op
Iteration  49: 227806,856 us/op
Iteration  50: 206124,540 us/op


Result "org.taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.runBenchmark3by3":
  219990,571 ±(99.9%) 6010,614 us/op [Average]
  (min, avg, max) = (198466,664, 219990,571, 242994,778), stdev = 12141,738
  CI (99.9%): [213979,957, 226001,186] (assumes normal distribution)


# Run complete. Total time: 00:09:58

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

Benchmark                                                                                  Mode  Cnt       Score      Error  Units
taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.dedupBenchmark3by3  avgt   50  218191,391 ± 8312,200  us/op
taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.deduppedBenchmark   avgt   50   15600,821 ±  494,818  us/op
taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.runBenchmark        avgt   50   15717,285 ±  214,062  us/op
taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.runBenchmark3by3    avgt   50  219990,571 ± 6010,614  us/op
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 600.934 sec
```