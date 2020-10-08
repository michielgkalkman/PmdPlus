# Results

I used PmdPlus on apache commons io 2.7.1-SNAPSHOT and found that 

```
copyDirectory(final File srcDir, final File destDir, final FileFilter filter, final boolean preserveFileDate)
```

executes getCanonicalPath() multiple times on the same path. I might be wrong, but that
seems wasteful.

Therefore, I create a junit test using jmh
and ran this code on my Dell Latitude E5550 laptop using:
- Windows 10 64-bit with 16GB memory and
- an Intel(R) Core(TM) i7-5600 CPU @ 2.60GHz
- and using a LITEONIT LCS-256L9S-11 2.5 7mm 256GB disk drive

and ran the test with the following JDK:
- openjdk version "11.0.8" 2020-07-14 LTS
- OpenJDK Runtime Environment Corretto-11.0.8.10.1 (build 11.0.8+10-LTS)
- OpenJDK 64-Bit Server VM Corretto-11.0.8.10.1 (build 11.0.8+10-LTS, mixed mode)

Results from two test runs:

Benchmark                                | Mode | Cnt | Score       | Error       | Units |
---------------------------------------- | ---- | --- | ----------- | ----------- | ----- |
taHjaj.wo.pmdplus.....arunBenchmark      | avgt | 100 |   11083,590 | ±   768,848 | us/op |
taHjaj.wo.pmdplus.....arunBenchmark1by1  | avgt | 100 |   51988,380 | ± 11676,741 | us/op |
taHjaj.wo.pmdplus.....arunBenchmark2by2  | avgt | 100 |  204613,482 | ± 37215,888 | us/op |
taHjaj.wo.pmdplus.....arunBenchmark3by3  | avgt | 100 | 1174152,673 | ± 22541,114 | us/op |
taHjaj.wo.pmdplus.....dedupBenchmark1by1 | avgt | 100 |   31088,342 | ±  1264,120 | us/op |
taHjaj.wo.pmdplus.....dedupBenchmark2by2 | avgt | 100 |  146071,467 | ±  3787,178 | us/op |
taHjaj.wo.pmdplus.....dedupBenchmark3by3 | avgt | 100 | 1154023,563 | ± 15089,337 | us/op |
taHjaj.wo.pmdplus.....deduppedBenchmark  | avgt | 100 |   14246,154 | ±  2367,264 | us/op |

Benchmark                                | Mode | Cnt | Score       | Error       | Units |
---------------------------------------- | ---- | --- | ----------- | ----------- | ----- |
taHjaj.wo.pmdplus.....arunBenchmark      | avgt | 100 |    11273,827 | ±  2165,753 | us/op |
taHjaj.wo.pmdplus.....arunBenchmark1by1  | avgt | 100 |    32702,611 | ±  1813,470 | us/op |
taHjaj.wo.pmdplus.....arunBenchmark2by2  | avgt | 100 |   152587,381 | ±  4642,588 | us/op |
taHjaj.wo.pmdplus.....arunBenchmark3by3  | avgt | 100 |  1195478,564 | ± 24303,450 | us/op |
taHjaj.wo.pmdplus.....dedupBenchmark1by1 | avgt   100 |    30884,652 | ±  1310,080 | us/op |
taHjaj.wo.pmdplus.....dedupBenchmark2by2 | avgt | 100 |   147020,471 | ±  4613,101 | us/op |
taHjaj.wo.pmdplus.....dedupBenchmark3by3 | avgt | 100 |  1185322,846 | ± 24429,620 | us/op |
taHjaj.wo.pmdplus.....deduppedBenchmark  | avgt | 100 |    11565,818 | ±  1937,442 | us/op |

On the whole, the deduppedBenchmarks seem to perform better, but I am definitely not an expert on this.

In partcularly I cannot explain why the deduppedBenchmark benchmark seems to be slower than the arunBenchmark.

Interesting sources:
- http://tutorials.jenkov.com/java-performance/jmh.html
- https://github.com/corretto/corretto-jmc/releases
- https://docs.oracle.com/cd/E15289_01/JRCLR/optionxx.htm#JRCLR275 (Parameters for -XX:StartFlightRecording)
- https://stackoverflow.com/questions/36807008/is-it-possible-to-run-a-jmh-benchmark-under-an-external-profiler#answer-37857708
- https://github.com/zolyfarkas/spf4j
- https://stackoverflow.com/questions/38926255/maven-annotation-processing-processor-not-found
