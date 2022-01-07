package dev.xpple.seedmapper.util.features;

import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcfeature.structure.RuinedPortal;

class OverworldRuinedPortal extends RuinedPortal {

    public OverworldRuinedPortal(MCVersion version) {
        super(Dimension.OVERWORLD, version);
    }

    public OverworldRuinedPortal(Config config, MCVersion version) {
        super(Dimension.OVERWORLD, config, version);
    }

    public static String name() {
        return "overworld_ruined_portal";
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public boolean isValidDimension(Dimension dimension) {
        return dimension == Dimension.OVERWORLD;
    }
}
