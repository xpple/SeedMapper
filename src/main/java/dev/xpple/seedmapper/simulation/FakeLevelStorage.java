package dev.xpple.seedmapper.simulation;

import com.mojang.datafixers.DataFixer;
import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess;
import net.minecraft.datafixer.Schemas;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.World;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorageException;
import org.jetbrains.annotations.Nullable;
import sun.misc.Unsafe;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

public class FakeLevelStorage extends LevelStorage {

    private static final Unsafe UNSAFE = UnsafeAccess.UNSAFE;

    private FakeLevelStorage(Path savesDirectory, Path backupsDirectory, DataFixer dataFixer) {
        super(savesDirectory, backupsDirectory, dataFixer);
    }

    public static FakeLevelStorage create() {
        try {
            return (FakeLevelStorage) UNSAFE.allocateInstance(FakeLevelStorage.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Path getSavesDirectory() {
        return Path.of("fake");
    }

    @Override
    public Path getBackupsDirectory() {
        return Path.of("fake");
    }

    @Override
    public LevelList getLevelList() {
        return new LevelList(Collections.emptyList());
    }

    @Override
    public boolean levelExists(String name) {
        return false;
    }

    @Override
    public Session createSession(String directoryName) {
        try {
            return (FakeSession) UNSAFE.allocateInstance(FakeSession.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public class FakeSession extends LevelStorage.Session {
        public FakeSession() throws IOException {
            super("fake");
        }

        @Override
        public Path getWorldDirectory(RegistryKey<World> key) {
            return Path.of("fake");
        }

        @Override
        public WorldSaveHandler createSaveHandler() {
            return null;
        }

        @Override
        public Path getDirectory(WorldSavePath savePath) {
            return Path.of("fake");
        }

        @Override
        public void save(String name) {
        }

        @Override
        public void backupLevelDataFile(DynamicRegistryManager registryManager, SaveProperties saveProperties) {
        }

        @Override
        public void backupLevelDataFile(DynamicRegistryManager registryManager, SaveProperties saveProperties, @Nullable NbtCompound nbt) {
        }

        @Override
        public long createBackup() {
            return 0;
        }

        @Override
        public void close() {
        }
    }
}
