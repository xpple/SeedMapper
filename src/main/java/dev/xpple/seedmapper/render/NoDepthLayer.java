package dev.xpple.seedmapper.render;

import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import dev.xpple.seedmapper.SeedMapper;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

public final class NoDepthLayer {
    private NoDepthLayer() {
    }

    private static final RenderPipeline LINES_NO_DEPTH_PIPELINE = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(SeedMapper.MOD_ID, "pipeline/lines_no_depth"))
            .withColorTargetState(ColorTargetState.DEFAULT)
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true))
            .build()
    );

    public static final RenderType LINES_NO_DEPTH_LAYER = RenderType.create(SeedMapper.MOD_ID + "_no_depth",
        RenderSetup.builder(LINES_NO_DEPTH_PIPELINE)
            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .createRenderSetup()
    );
}
