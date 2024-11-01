package dev.xpple.seedmapper.mixin.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderType.class)
public interface RenderLayerAccessor {
    @Invoker(value = "create")
    static RenderType.CompositeRenderType create(String name, VertexFormat vertexFormat, VertexFormat.Mode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, RenderType.CompositeState phases) {
        throw new AssertionError();
    }
}
