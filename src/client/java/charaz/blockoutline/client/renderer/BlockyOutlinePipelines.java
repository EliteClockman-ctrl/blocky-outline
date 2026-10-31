package charaz.blockoutline.client.renderer;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class BlockyOutlinePipelines {
    private static Method registerMethod;

    static {
        try {
            registerMethod = RenderPipelines.class.getDeclaredMethod("register", RenderPipeline.class);
            registerMethod.setAccessible(true);
        } catch (Throwable t) {
            registerMethod = null;
        }
    }

    private BlockyOutlinePipelines() {}

    public static RenderPipeline createXrayLinesPipeline() {
        return createXrayFromSnippet("LINES_SNIPPET", "pipeline/xray_lines", false);
    }

    public static RenderPipeline createXrayQuadsPipeline() {
        return createXrayFromSnippet("DEBUG_FILLED_SNIPPET", "pipeline/xray_quads", false);
    }

    private static RenderPipeline createXrayFromSnippet(String snippetFieldName, String name, boolean cull) {
        try {
            Field snippetField = RenderPipelines.class.getDeclaredField(snippetFieldName);
            snippetField.setAccessible(true);
            RenderPipeline.Snippet snippet = (RenderPipeline.Snippet) snippetField.get(null);
            DepthStencilState xrayDepth = new DepthStencilState(CompareOp.ALWAYS_PASS, false);
            RenderPipeline pipeline = RenderPipeline.builder(snippet)
                    .withLocation(Identifier.fromNamespaceAndPath("blocky-outline", name))
                    .withCull(cull)
                    .withDepthStencilState(xrayDepth)
                    .build();
            return register(pipeline);
        } catch (Throwable t) {
            return "LINES_SNIPPET".equals(snippetFieldName) ? RenderPipelines.LINES : RenderPipelines.DEBUG_QUADS;
        }
    }

    public static RenderPipeline register(RenderPipeline pipeline) {
        if (registerMethod != null) {
            try {
                return (RenderPipeline) registerMethod.invoke(null, pipeline);
            } catch (Throwable ignored) {
            }
        }
        return pipeline;
    }
}

