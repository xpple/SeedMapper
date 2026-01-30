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
        this.commandLine("./jextract/build/jextract/bin/jextract" + EXTENSION, "--include-dir", "src/main/c/cubiomes", "--output", "src/main/java", "--use-system-load-library", "--target-package", "com.github.cubiomes", "--header-class-name", "Cubiomes", "@src/main/c/cubiomes/includes.txt", "biomenoise.h", "biomes.h", "finders.h", "generator.h", "layers.h", "biomenoise.h", "biomes.h", "noise.h", "terrainnoise.h", "rng.h", "util.h", "quadbase.h", "xrms.h", "loot/items.h", "loot/logging.h", "loot/loot_functions.h", "loot/loot_table_context.h", "loot/loot_table_parser.h", "loot/loot_tables.h", "loot/mc_loot.h");
    }
}
