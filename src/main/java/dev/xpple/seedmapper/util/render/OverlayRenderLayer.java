package dev.xpple.seedmapper.util.render;

import dev.xpple.seedmapper.mixin.render.RenderLayerAccessor;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

import java.util.OptionalDouble;

public class OverlayRenderLayer extends RenderLayer {

    private static RenderLayer OVERLAY;

    private OverlayRenderLayer(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
    }

    public static RenderLayer getOverlay() {
        if (OVERLAY == null) {
            MultiPhaseParameters parameters = MultiPhaseParameters.builder()
                .program(LINES_PROGRAM)
                .transparency(TRANSLUCENT_TRANSPARENCY)
                .target(OUTLINE_TARGET)
                .writeMaskState(COLOR_MASK)
                .cull(DISABLE_CULLING)
                .layering(VIEW_OFFSET_Z_LAYERING)
                .lineWidth(new LineWidth(OptionalDouble.of(2.0F)))
                .build(false);

            OVERLAY = RenderLayerAccessor.of("overlay", VertexFormats.LINES, VertexFormat.DrawMode.LINES, 256, false, false, parameters);
        }
        return OVERLAY;
    }
}
