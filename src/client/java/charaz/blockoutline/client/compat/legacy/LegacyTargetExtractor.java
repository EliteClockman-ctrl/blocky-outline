package charaz.blockoutline.client.compat.legacy;

import charaz.blockoutline.client.compat.TargetExtractor;
import charaz.blockoutline.client.compat.TargetInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public final class LegacyTargetExtractor implements TargetExtractor {
    private final TargetInfo cached = new TargetInfo();
    private static MethodHandle getShapeHandle;

    @Override
    public TargetInfo extract(Object targetObj, Minecraft mc) {
        if (targetObj instanceof BlockHitResult hit && mc.level != null) {
            BlockPos pos = hit.getBlockPos();
            BlockState state = mc.level.getBlockState(pos);
            Entity camEntity = mc.getCameraEntity() != null ? mc.getCameraEntity() : mc.player;
            CollisionContext ctx = camEntity != null ? CollisionContext.of(camEntity) : CollisionContext.empty();
            VoxelShape shape = null;
            try {
                if (getShapeHandle == null) {
                    for (Method m : state.getClass().getMethods()) {
                        if (VoxelShape.class.isAssignableFrom(m.getReturnType()) && m.getParameterCount() == 3) {
                            m.setAccessible(true);
                            getShapeHandle = MethodHandles.lookup().unreflect(m);
                            break;
                        }
                    }
                }
                if (getShapeHandle != null) {
                    shape = (VoxelShape) getShapeHandle.invoke(state, mc.level, pos, ctx);
                }
            } catch (Throwable ignored) {
            }
            if (shape != null) {
                cached.set(pos, shape);
                return cached;
            }
        }
        return null;
    }
}
