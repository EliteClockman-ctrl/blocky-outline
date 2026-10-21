package com.mojang.blaze3d.pipeline;

import com.mojang.blaze3d.platform.CompareOp;
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

    public static RenderPipeline createXrayPipeline(RenderPipeline base, String name) {
        DepthStencilState xrayDepth = new DepthStencilState(CompareOp.ALWAYS_PASS, false);
        RenderPipeline pipeline = new RenderPipeline(
                Identifier.fromNamespaceAndPath("blocky-outline", name),
                base.getVertexShader(),
                base.getFragmentShader(),
                base.getShaderDefines(),
                base.getBindGroupLayouts(),
                base.getColorTargetStates(),
                xrayDepth,
                base.getPolygonMode(),
                base.isCull(),
                base.getVertexFormatBindings(),
                base.getPrimitiveTopology(),
                base.getSortKey()
        );
        register(pipeline);
        return pipeline;
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
