package net.minecraft.client.renderer.rendertype;

import com.mojang.blaze3d.pipeline.BlockyOutlinePipelines;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;

public final class BlockyOutlineRenderTypes {
    private static RenderType XRAY_LINES;
    private static RenderType XRAY_QUADS;

    private BlockyOutlineRenderTypes() {}

    public static RenderType xrayLines() {
        if (XRAY_LINES == null) {
            try {
                RenderPipeline pipeline = BlockyOutlinePipelines.createXrayPipeline(RenderPipelines.LINES, "pipeline/xray_lines");
                RenderSetup setup = RenderSetup.builder(pipeline)
                        .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                        .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                        .createRenderSetup();
                XRAY_LINES = RenderType.create("blocky_outline_xray_lines", setup);
            } catch (Throwable t) {
                XRAY_LINES = RenderTypes.lines();
            }
        }
        return XRAY_LINES;
    }

    public static RenderType xrayQuads() {
        if (XRAY_QUADS == null) {
            try {
                RenderPipeline pipeline = BlockyOutlinePipelines.createXrayPipeline(RenderPipelines.DEBUG_QUADS, "pipeline/xray_quads");
                RenderSetup setup = RenderSetup.builder(pipeline)
                        .sortOnUpload()
                        .createRenderSetup();
                XRAY_QUADS = RenderType.create("blocky_outline_xray_quads", setup);
            } catch (Throwable t) {
                XRAY_QUADS = RenderTypes.debugQuads();
            }
        }
        return XRAY_QUADS;
    }
}
