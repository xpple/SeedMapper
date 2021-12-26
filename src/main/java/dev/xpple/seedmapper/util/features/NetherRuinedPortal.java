package dev.xpple.seedmapper.util.features;

import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcfeature.structure.RuinedPortal;

public class NetherRuinedPortal extends RuinedPortal {

    public NetherRuinedPortal(MCVersion version) {
        super(Dimension.NETHER, version);
    }

    public NetherRuinedPortal(Config config, MCVersion version) {
        super(Dimension.NETHER, config, version);
    }

    public static String name() {
        return "nether_ruined_portal";
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public boolean isValidDimension(Dimension dimension) {
        return dimension == Dimension.NETHER;
    }
}
