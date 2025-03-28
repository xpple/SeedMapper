package dev.xpple.seedmapper.buildscript;

import org.apache.tools.ant.taskdefs.condition.Os;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class CreateJavaBindingsTask extends DefaultTask {

    private static final String JAVA_HOME = System.getenv("JAVA_HOME");
    private static final String LLVM_HOME = System.getenv("LLVM_HOME");
    private static final String EXTENSION = Os.isFamily(Os.FAMILY_WINDOWS) ? ".bat" : "";
    private static final String LIBRARY_LOCATION;

    static {
        String libraryName = System.mapLibraryName("cubiomes");
        Path libraryPath;
        try {
            libraryPath = Files.createTempFile(libraryName, "");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        LIBRARY_LOCATION = libraryPath.toAbsolutePath().toString();
    }

    {
        // always run task
        this.getOutputs().upToDateWhen(_ -> false);
    }

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @Inject
    protected abstract ExecOperations getExecOperations();

    @TaskAction
    public void run() {
        String output;
        File jextractDir = this.getProject().getRootDir().toPath().resolve("jextract").toFile();
        output = this.executeCommand(jextractDir, "./gradlew" + EXTENSION, "--stacktrace", "-Pjdk_home=" + JAVA_HOME, "-Pllvm_home=" + LLVM_HOME, "clean", "verify");
        System.out.println(output);
        File rootDir = this.getProject().getRootDir();
        output = this.executeCommand(rootDir, "./jextract/build/jextract/bin/jextract" + EXTENSION, "--include-dir", "src/main/c", "--output", "src/main/java", "--library", ':' + LIBRARY_LOCATION, "--use-system-load-library", "--target-package", "com.github.cubiomes", "--header-class-name", "Cubiomes", "src/main/c/tables/btree18.h", "tables/btree19.h", "tables/btree20.h", "tables/btree192.h", "tables/btree21wd.h", "biomenoise.h", "biomes.h", "finders.h", "generator.h", "layers.h", "noise.h", "quadbase.h", "rng.h", "util.h");
        System.out.println(output);

        try {
            Files.writeString(this.getOutputFile().get().getAsFile().toPath(), LIBRARY_LOCATION);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
