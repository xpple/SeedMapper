package dev.xpple.seedmapper.buildscript;

import org.apache.tools.ant.taskdefs.condition.Os;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public abstract class CreateJavaBindingsTask extends DefaultTask {

    private static final String EXTENSION = Os.isFamily(Os.FAMILY_WINDOWS) ? ".bat" : "";

    @Inject
    protected abstract ExecOperations getExecOperations();

    {
        // always run task
        this.getOutputs().upToDateWhen(_ -> false);
    }

    @TaskAction
    protected void run() {
        Path rootPath = getProject().getRootDir().toPath();
        this.executeCommand(rootPath.resolve("src", "main", "c", "cubiomes"), "cmake", "-S", ".", "-B", "build", "-DEXPORT_HEADERS=ON");

        this.executeCommand(rootPath, "./jextract/build/jextract/bin/jextract" + EXTENSION, "--include-dir", "src/main/c/cubiomes", "--header-class-name", "Cubiomes", "-D", "STRUCT_CONFIG_OVERRIDE=1", "--dump-includes", "dump_includes.txt", "@src/main/c/cubiomes/build/headers.txt");

        try (Stream<String> lines = Files.lines(rootPath.resolve("dump_includes.txt"))) {
            Files.write(rootPath.resolve("dump_includes_filtered.txt"), lines
                .filter(l -> l.contains("/SeedMapper/src/main/c/cubiomes/"))
                .filter(l -> !l.contains("/SeedMapper/src/main/c/cubiomes/loot/cjson/"))
                .toList());
        } catch (IOException e) {
            this.getLogger().error("Could not filter 'dump_includes.txt'", e);
            return;
        }

        this.executeCommand(rootPath, "./jextract/build/jextract/bin/jextract" + EXTENSION, "--include-dir", "src/main/c/cubiomes", "--include-dir", "src/main/c/cubiomes", "--output", "src/main/java", "--use-system-load-library", "--target-package", "com.github.cubiomes", "--header-class-name", "Cubiomes", "-D", "STRUCT_CONFIG_OVERRIDE=1", "@dump_includes_filtered.txt", "@src/main/c/cubiomes/build/headers.txt");
    }

    private void executeCommand(Path path, Object... args) {
        this.getExecOperations().exec(execSpec -> {
            execSpec.setWorkingDir(path);
            execSpec.setStandardOutput(System.out);
            execSpec.commandLine(args);
        }).rethrowFailure();
    }
}
