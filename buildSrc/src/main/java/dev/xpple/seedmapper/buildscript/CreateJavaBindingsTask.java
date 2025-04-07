package dev.xpple.seedmapper.buildscript;

import org.apache.tools.ant.taskdefs.condition.Os;
import org.gradle.api.tasks.Exec;

public abstract class CreateJavaBindingsTask extends Exec {

    private static final String EXTENSION = Os.isFamily(Os.FAMILY_WINDOWS) ? ".bat" : "";

    {
        // always run task
        this.getOutputs().upToDateWhen(_ -> false);

        this.setWorkingDir(this.getProject().getRootDir());
        this.setStandardOutput(System.out);
        this.commandLine("./jextract/build/jextract/bin/jextract" + EXTENSION, "--include-dir", "src/main/c", "--output", "src/main/java", "--use-system-load-library", "--target-package", "com.github.cubiomes", "--header-class-name", "Cubiomes", "@includes.txt", "src/main/c/tables/btree18.h", "tables/btree19.h", "tables/btree20.h", "tables/btree192.h", "tables/btree21wd.h", "biomenoise.h", "biomes.h", "finders.h", "generator.h", "layers.h", "noise.h", "quadbase.h", "rng.h", "util.h");
    }
}
