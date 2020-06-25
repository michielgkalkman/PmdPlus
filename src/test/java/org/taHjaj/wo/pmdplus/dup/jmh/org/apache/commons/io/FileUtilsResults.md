# Results

I used PmdPlus on apache commons io 2.7.1-SNAPSHOT and found that 

```
copyDirectory(final File srcDir, final File destDir, final FileFilter filter, final boolean preserveFileDate)
```

executes getCanonicalPath() multiple times on the same path. I might be wrong, but that
seems wasteful.

```
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
# Measurement: 50 iterations, 15 ms each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: org.taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.dedupBenchmark3by3

# Run progress: 0,00% complete, ETA 00:00:43
# Fork: 1 of 1
# Warmup Iteration   1: 60506,500 us/op
# Warmup Iteration   2: 26429,433 us/op
# Warmup Iteration   3: 25827,000 us/op
# Warmup Iteration   4: 27309,408 us/op
# Warmup Iteration   5: 25680,169 us/op
Iteration   1: 24600,700 us/op
Iteration   2: 24150,600 us/op
Iteration   3: 26669,600 us/op
Iteration   4: 25017,200 us/op
Iteration   5: 25556,300 us/op
Iteration   6: 26911,100 us/op
Iteration   7: 26615,400 us/op
Iteration   8: 28656,700 us/op
Iteration   9: 33418,500 us/op
Iteration  10: 31895,800 us/op
Iteration  11: 24767,800 us/op
Iteration  12: 26047,700 us/op
Iteration  13: 26232,600 us/op
Iteration  14: 24808,700 us/op
Iteration  15: 24489,100 us/op
Iteration  16: 24764,000 us/op
Iteration  17: 25124,300 us/op
Iteration  18: 26504,300 us/op
Iteration  19: 25078,200 us/op
Iteration  20: 24954,000 us/op
Iteration  21: 28873,700 us/op
Iteration  22: 25667,200 us/op
Iteration  23: 28413,300 us/op
Iteration  24: 24415,700 us/op
Iteration  25: 26970,200 us/op
Iteration  26: 30266,300 us/op
Iteration  27: 26287,200 us/op
Iteration  28: 24864,500 us/op
Iteration  29: 30743,400 us/op
Iteration  30: 25818,800 us/op
Iteration  31: 26010,900 us/op
Iteration  32: 32433,000 us/op
Iteration  33: 28450,200 us/op
Iteration  34: 31629,200 us/op
Iteration  35: 25668,100 us/op
Iteration  36: 24313,600 us/op
Iteration  37: 26648,800 us/op
Iteration  38: 26863,500 us/op
Iteration  39: 24885,300 us/op
Iteration  40: 23863,200 us/op
Iteration  41: 25882,700 us/op
Iteration  42: 30119,700 us/op
Iteration  43: 24974,300 us/op
Iteration  44: 31182,500 us/op
Iteration  45: 23747,900 us/op
Iteration  46: 23658,000 us/op
Iteration  47: 23664,700 us/op
Iteration  48: 25030,400 us/op
Iteration  49: 23732,100 us/op
Iteration  50: 23642,700 us/op


Result "org.taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.dedupBenchmark3by3":
  26499,674 ±(99.9%) 1272,241 us/op [Average]
  (min, avg, max) = (23642,700, 26499,674, 33418,500), stdev = 2569,991
  CI (99.9%): [25227,433, 27771,915] (assumes normal distribution)


# JMH version: 1.23
# VM version: JDK 11.0.7, OpenJDK 64-Bit Server VM, 11.0.7+10-LTS
# VM invoker: C:\Program Files\Amazon Corretto\jdk11.0.7_10\bin\java.exe
# VM options: <none>
# Warmup: 5 iterations, 2 s each
# Measurement: 50 iterations, 15 ms each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: org.taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.deduppedBenchmark

# Run progress: 25,00% complete, ETA 00:01:03
# Fork: 1 of 1
# Warmup Iteration   1: 6939,200 us/op
# Warmup Iteration   2: 6541,340 us/op
# Warmup Iteration   3: 5736,952 us/op
# Warmup Iteration   4: 5778,307 us/op
# Warmup Iteration   5: 6599,605 us/op
Iteration   1: 6238,467 us/op
Iteration   2: 6826,067 us/op
Iteration   3: 6828,750 us/op
Iteration   4: 6245,667 us/op
Iteration   5: 6459,200 us/op
Iteration   6: 6429,667 us/op
Iteration   7: 6265,050 us/op
Iteration   8: 6287,767 us/op
Iteration   9: 6092,900 us/op
Iteration  10: 6217,600 us/op
Iteration  11: 5928,225 us/op
Iteration  12: 5982,733 us/op
Iteration  13: 6077,925 us/op
Iteration  14: 6457,225 us/op
Iteration  15: 7392,100 us/op
Iteration  16: 7958,267 us/op
Iteration  17: 5829,933 us/op
Iteration  18: 6379,467 us/op
Iteration  19: 6092,975 us/op
Iteration  20: 7115,900 us/op
Iteration  21: 5989,475 us/op
Iteration  22: 5260,000 us/op
Iteration  23: 5573,100 us/op
Iteration  24: 7170,333 us/op
Iteration  25: 60414,400 us/op
Iteration  26: 7557,567 us/op
Iteration  27: 6179,733 us/op
Iteration  28: 5131,850 us/op
Iteration  29: 8259,867 us/op
Iteration  30: 7922,750 us/op
Iteration  31: 7174,833 us/op
Iteration  32: 7534,733 us/op
Iteration  33: 7456,867 us/op
Iteration  34: 8996,800 us/op
Iteration  35: 6902,700 us/op
Iteration  36: 7256,633 us/op
Iteration  37: 9951,300 us/op
Iteration  38: 5574,025 us/op
Iteration  39: 6371,367 us/op
Iteration  40: 6119,550 us/op
Iteration  41: 5915,125 us/op
Iteration  42: 6938,967 us/op
Iteration  43: 6560,767 us/op
Iteration  44: 6367,550 us/op
Iteration  45: 6043,633 us/op
Iteration  46: 6278,500 us/op
Iteration  47: 6334,100 us/op
Iteration  48: 6958,433 us/op
Iteration  49: 5923,825 us/op
Iteration  50: 6599,633 us/op


Result "org.taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.deduppedBenchmark":
  7716,486 ±(99.9%) 3790,750 us/op [Average]
  (min, avg, max) = (5131,850, 7716,486, 60414,400), stdev = 7657,503
  CI (99.9%): [3925,736, 11507,236] (assumes normal distribution)


# JMH version: 1.23
# VM version: JDK 11.0.7, OpenJDK 64-Bit Server VM, 11.0.7+10-LTS
# VM invoker: C:\Program Files\Amazon Corretto\jdk11.0.7_10\bin\java.exe
# VM options: <none>
# Warmup: 5 iterations, 2 s each
# Measurement: 50 iterations, 15 ms each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: org.taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.runBenchmark

# Run progress: 50,00% complete, ETA 00:00:38
# Fork: 1 of 1
# Warmup Iteration   1: 8272,600 us/op
# Warmup Iteration   2: 5923,081 us/op
# Warmup Iteration   3: 6546,538 us/op
# Warmup Iteration   4: 5809,398 us/op
# Warmup Iteration   5: 6481,091 us/op
Iteration   1: 6410,767 us/op
Iteration   2: 6383,100 us/op
Iteration   3: 5907,150 us/op
Iteration   4: 9580,150 us/op
Iteration   5: 7428,167 us/op
Iteration   6: 7137,267 us/op
Iteration   7: 7129,000 us/op
Iteration   8: 10037,700 us/op
Iteration   9: 8011,567 us/op
Iteration  10: 7598,000 us/op
Iteration  11: 7610,967 us/op
Iteration  12: 7377,567 us/op
Iteration  13: 7639,100 us/op
Iteration  14: 7191,000 us/op
Iteration  15: 5919,625 us/op
Iteration  16: 6767,067 us/op
Iteration  17: 6284,875 us/op
Iteration  18: 6153,467 us/op
Iteration  19: 5437,075 us/op
Iteration  20: 6244,267 us/op
Iteration  21: 6597,167 us/op
Iteration  22: 5744,600 us/op
Iteration  23: 6369,667 us/op
Iteration  24: 6735,050 us/op
Iteration  25: 8986,850 us/op
Iteration  26: 6511,700 us/op
Iteration  27: 7216,167 us/op
Iteration  28: 7513,000 us/op
Iteration  29: 7695,667 us/op
Iteration  30: 7255,833 us/op
Iteration  31: 7178,067 us/op
Iteration  32: 6875,100 us/op
Iteration  33: 6386,775 us/op
Iteration  34: 6471,600 us/op
Iteration  35: 8245,933 us/op
Iteration  36: 6635,850 us/op
Iteration  37: 6400,625 us/op
Iteration  38: 7524,200 us/op
Iteration  39: 6538,033 us/op
Iteration  40: 6938,125 us/op
Iteration  41: 6159,833 us/op
Iteration  42: 6653,000 us/op
Iteration  43: 6464,967 us/op
Iteration  44: 7181,800 us/op
Iteration  45: 6597,967 us/op
Iteration  46: 5830,450 us/op
Iteration  47: 6680,667 us/op
Iteration  48: 7185,500 us/op
Iteration  49: 5569,733 us/op
Iteration  50: 6819,600 us/op


Result "org.taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.runBenchmark":
  6944,228 ±(99.9%) 453,129 us/op [Average]
  (min, avg, max) = (5437,075, 6944,228, 10037,700), stdev = 915,343
  CI (99.9%): [6491,099, 7397,357] (assumes normal distribution)


# JMH version: 1.23
# VM version: JDK 11.0.7, OpenJDK 64-Bit Server VM, 11.0.7+10-LTS
# VM invoker: C:\Program Files\Amazon Corretto\jdk11.0.7_10\bin\java.exe
# VM options: <none>
# Warmup: 5 iterations, 2 s each
# Measurement: 50 iterations, 15 ms each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations
# Benchmark mode: Average time, time/op
# Benchmark: org.taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.runBenchmark3by3

# Run progress: 75,00% complete, ETA 00:00:18
# Fork: 1 of 1
# Warmup Iteration   1: 60780,400 us/op
# Warmup Iteration   2: 25536,862 us/op
# Warmup Iteration   3: 25419,475 us/op
# Warmup Iteration   4: 26710,517 us/op
# Warmup Iteration   5: 25410,100 us/op
Iteration   1: 24991,000 us/op
Iteration   2: 24833,200 us/op
Iteration   3: 24519,800 us/op
Iteration   4: 32020,400 us/op
Iteration   5: 23798,000 us/op
Iteration   6: 25004,000 us/op
Iteration   7: 25103,300 us/op
Iteration   8: 25291,600 us/op
Iteration   9: 27592,400 us/op
Iteration  10: 26080,900 us/op
Iteration  11: 24009,400 us/op
Iteration  12: 24666,000 us/op
Iteration  13: 26079,100 us/op
Iteration  14: 26000,600 us/op
Iteration  15: 27845,200 us/op
Iteration  16: 24961,300 us/op
Iteration  17: 24165,100 us/op
Iteration  18: 27365,500 us/op
Iteration  19: 25537,600 us/op
Iteration  20: 24462,600 us/op
Iteration  21: 24653,300 us/op
Iteration  22: 25971,100 us/op
Iteration  23: 24194,200 us/op
Iteration  24: 24331,000 us/op
Iteration  25: 25113,800 us/op
Iteration  26: 28469,100 us/op
Iteration  27: 27025,200 us/op
Iteration  28: 30629,400 us/op
Iteration  29: 24419,000 us/op
Iteration  30: 24704,500 us/op
Iteration  31: 27139,100 us/op
Iteration  32: 26557,200 us/op
Iteration  33: 28190,800 us/op
Iteration  34: 29930,900 us/op
Iteration  35: 25466,800 us/op
Iteration  36: 23765,600 us/op
Iteration  37: 23940,100 us/op
Iteration  38: 26287,700 us/op
Iteration  39: 23996,800 us/op
Iteration  40: 24858,900 us/op
Iteration  41: 23878,100 us/op
Iteration  42: 25375,400 us/op
Iteration  43: 26512,200 us/op
Iteration  44: 23769,000 us/op
Iteration  45: 27300,500 us/op
Iteration  46: 23845,800 us/op
Iteration  47: 26883,300 us/op
Iteration  48: 24524,400 us/op
Iteration  49: 98705,700 us/op
Iteration  50: 25174,100 us/op


Result "org.taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.runBenchmark3by3":
  27198,800 ±(99.9%) 5187,704 us/op [Average]
  (min, avg, max) = (23765,600, 27198,800, 98705,700), stdev = 10479,419
  CI (99.9%): [22011,096, 32386,504] (assumes normal distribution)


# Run complete. Total time: 00:01:21

REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
experiments, perform baseline and negative tests that provide experimental control, make sure
the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
Do not assume the numbers tell you what you want them to tell.

Benchmark                                                                                  Mode  Cnt      Score      Error  Units
taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.dedupBenchmark3by3  avgt   50  26499,674 ± 1272,241  us/op
taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.deduppedBenchmark   avgt   50   7716,486 ± 3790,750  us/op
taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.runBenchmark        avgt   50   6944,228 ±  453,129  us/op
taHjaj.wo.pmdplus.dup.jmh.org.apache.commons.io.FileUtilsBenchmarkTest.runBenchmark3by3    avgt   50  27198,800 ± 5187,704  us/op
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 383.999 sec
```
