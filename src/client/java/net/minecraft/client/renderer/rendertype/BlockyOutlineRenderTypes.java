package net.minecraft.client.renderer.rendertype;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.Identifier;

public final class BlockyOutlineRenderTypes {
    public static final RenderType XRAY_LINES;
    public static final RenderType XRAY_QUADS;

    static {
        RenderPipeline linesPipeline = RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath("blocky-outline", "pipeline/lines_xray"))
                .withVertexShader("core/rendertype_lines")
                .withFragmentShader("core/rendertype_lines")
                .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                .withUniform("Fog", UniformType.UNIFORM_BUFFER)
                .withUniform("Globals", UniformType.UNIFORM_BUFFER)
                .withBlend(BlendFunction.TRANSLUCENT)
                .withCull(false)
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .withDepthWrite(false)
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH, VertexFormat.Mode.LINES)
                .build();

        RenderSetup linesSetup = RenderSetup.builder(linesPipeline)
                .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                .createRenderSetup();

        XRAY_LINES = RenderType.create("blocky_outline_xray_lines", linesSetup);

        RenderPipeline quadsPipeline = RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath("blocky-outline", "pipeline/debug_quads_xray"))
                .withVertexShader("core/position_color")
                .withFragmentShader("core/position_color")
                .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                .withBlend(BlendFunction.TRANSLUCENT)
                .withCull(false)
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .withDepthWrite(false)
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
                .build();

        RenderSetup quadsSetup = RenderSetup.builder(quadsPipeline)
                .sortOnUpload()
                .createRenderSetup();

        XRAY_QUADS = RenderType.create("blocky_outline_xray_quads", quadsSetup);
    }

    private BlockyOutlineRenderTypes() {
    }
}
