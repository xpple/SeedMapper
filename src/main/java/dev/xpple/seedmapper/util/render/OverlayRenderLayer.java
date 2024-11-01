package dev.xpple.seedmapper.util.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.xpple.seedmapper.mixin.render.RenderLayerAccessor;
import net.minecraft.client.renderer.RenderType;

import java.util.OptionalDouble;

public class OverlayRenderLayer extends RenderType {

    private static RenderType OVERLAY;

    private OverlayRenderLayer(String name, VertexFormat vertexFormat, VertexFormat.Mode mode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
        super(name, vertexFormat, mode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
    }

    public static RenderType getOverlay() {
        if (OVERLAY == null) {
            CompositeState parameters = CompositeState.builder()
                .setShaderState(RENDERTYPE_LINES_SHADER)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setOutputState(OUTLINE_TARGET)
                .setWriteMaskState(COLOR_WRITE)
                .setCullState(NO_CULL)
                .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                .setLineState(new LineStateShard(OptionalDouble.of(2.0F)))
                .createCompositeState(false);

            OVERLAY = RenderLayerAccessor.create("overlay", DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 256, false, false, parameters);
        }
        return OVERLAY;
    }
}
