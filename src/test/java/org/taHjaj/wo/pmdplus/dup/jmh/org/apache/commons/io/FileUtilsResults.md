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

...
```