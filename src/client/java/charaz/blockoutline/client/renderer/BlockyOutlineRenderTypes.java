package charaz.blockoutline.client.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.lang.reflect.Method;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public final class BlockyOutlineRenderTypes {
    private static RenderType XRAY_LINES;
    private static RenderType XRAY_QUADS;
    private static Method createRenderTypeMethod;

    static {
        try {
            createRenderTypeMethod = RenderType.class.getDeclaredMethod("create", String.class, RenderSetup.class);
            createRenderTypeMethod.setAccessible(true);
        } catch (Throwable t) {
            createRenderTypeMethod = null;
        }
    }

    private BlockyOutlineRenderTypes() {}

    private static RenderType invokeCreate(String name, RenderSetup setup, RenderType fallback) {
        if (createRenderTypeMethod != null) {
            try {
                return (RenderType) createRenderTypeMethod.invoke(null, name, setup);
            } catch (Throwable ignored) {
            }
        }
        return fallback;
    }

    public static RenderType xrayLines() {
        if (XRAY_LINES == null) {
            try {
                RenderPipeline pipeline = BlockyOutlinePipelines.createXrayLinesPipeline();
                RenderSetup setup = RenderSetup.builder(pipeline)
                        .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                        .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                        .createRenderSetup();
                XRAY_LINES = invokeCreate("blocky_outline_xray_lines", setup, RenderTypes.lines());
            } catch (Throwable t) {
                XRAY_LINES = RenderTypes.lines();
            }
        }
        return XRAY_LINES;
    }

    public static RenderType xrayQuads() {
        if (XRAY_QUADS == null) {
            try {
                RenderPipeline pipeline = BlockyOutlinePipelines.createXrayQuadsPipeline();
                RenderSetup setup = RenderSetup.builder(pipeline)
                        .sortOnUpload()
                        .createRenderSetup();
                XRAY_QUADS = invokeCreate("blocky_outline_xray_quads", setup, RenderTypes.debugQuads());
            } catch (Throwable t) {
                XRAY_QUADS = RenderTypes.debugQuads();
            }
        }
        return XRAY_QUADS;
    }
}

