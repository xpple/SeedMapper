package dev.xpple.seedmapper.command;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import static dev.xpple.seedmapper.SeedMapper.CLIENT;

public class FakeCommandSource extends ServerCommandSource {

    public FakeCommandSource(ClientPlayerEntity player) {
        super(player, player.getPos(), player.getRotationClient(), null, 0, player.getEntityName(), player.getName(), null, player);
    }

    @Override
    public Collection<String> getPlayerNames() {
        return Objects.requireNonNull(CLIENT.getNetworkHandler()).getPlayerList().stream().map(e -> e.getProfile().getName()).collect(Collectors.toList());
    }
}
