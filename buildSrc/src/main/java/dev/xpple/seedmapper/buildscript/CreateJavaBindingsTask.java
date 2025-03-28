package dev.xpple.seedmapper.buildscript;

import org.apache.tools.ant.taskdefs.condition.Os;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;

public abstract class CreateJavaBindingsTask extends DefaultTask {

    private static final String JAVA_HOME = System.getenv("JAVA_HOME");
    private static final String LLVM_HOME = System.getenv("LLVM_HOME");
    private static final String EXTENSION = Os.isFamily(Os.FAMILY_WINDOWS) ? ".bat" : "";

    {
        // always run task
        this.getOutputs().upToDateWhen(_ -> false);
    }

    @Inject
    protected abstract ExecOperations getExecOperations();

    @TaskAction
    public void run() {
        String output;
        File jextractDir = this.getProject().getRootDir().toPath().resolve("jextract").toFile();
        output = this.executeCommand(jextractDir, "./gradlew" + EXTENSION, "--stacktrace", "-Pjdk_home=" + JAVA_HOME, "-Pllvm_home=" + LLVM_HOME, "clean", "verify");
        System.out.println(output);
        File rootDir = this.getProject().getRootDir();
        output = this.executeCommand(rootDir, "./jextract/build/jextract/bin/jextract" + EXTENSION, "--include-dir", "src/main/c", "--output", "src/main/java", "--use-system-load-library", "--target-package", "com.github.cubiomes", "--header-class-name", "Cubiomes", "@includes.txt", "src/main/c/tables/btree18.h", "tables/btree19.h", "tables/btree20.h", "tables/btree192.h", "tables/btree21wd.h", "biomenoise.h", "biomes.h", "finders.h", "generator.h", "layers.h", "noise.h", "quadbase.h", "rng.h", "util.h");
        System.out.println(output);
    }

    private String executeCommand(File workingDir, Object... args) {
        ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
        this.getExecOperations().exec(execSpec -> {
            execSpec.setWorkingDir(workingDir);
            execSpec.setStandardOutput(outputBytes);
            execSpec.commandLine(args);
        }).rethrowFailure();
        return outputBytes.toString(StandardCharsets.UTF_8);
    }
}
