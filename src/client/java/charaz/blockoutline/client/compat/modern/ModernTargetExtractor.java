package charaz.blockoutline.client.compat.modern;

import charaz.blockoutline.client.compat.TargetExtractor;
import charaz.blockoutline.client.compat.TargetInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public final class ModernTargetExtractor implements TargetExtractor {
    private final TargetInfo cached = new TargetInfo();
    private static MethodHandle posHandle;
    private static MethodHandle shapeHandle;
    private static Class<?> cachedClass;

    @Override
    public TargetInfo extract(Object targetObj, Minecraft mc) {
        if (targetObj == null) return null;
        Class<?> clazz = targetObj.getClass();
        try {
            if (clazz != cachedClass) {
                cachedClass = clazz;
                Method posMethod = null;
                Method shapeMethod = null;
                for (Method m : clazz.getMethods()) {
                    if (m.getParameterCount() == 0) {
                        String name = m.getName();
                        if (name.equals("pos") || name.equals("method_69919") || BlockPos.class.isAssignableFrom(m.getReturnType())) {
                            posMethod = m;
                        } else if (name.equals("shape") || name.equals("method_69923") || VoxelShape.class.isAssignableFrom(m.getReturnType())) {
                            shapeMethod = m;
                        }
                    }
                }
                if (posMethod != null) {
                    posMethod.setAccessible(true);
                    posHandle = MethodHandles.lookup().unreflect(posMethod);
                }
                if (shapeMethod != null) {
                    shapeMethod.setAccessible(true);
                    shapeHandle = MethodHandles.lookup().unreflect(shapeMethod);
                }
            }
            if (posHandle != null && shapeHandle != null) {
                BlockPos pos = (BlockPos) posHandle.invoke(targetObj);
                VoxelShape shape = (VoxelShape) shapeHandle.invoke(targetObj);
                if (pos != null && shape != null) {
                    cached.set(pos, shape);
                    return cached;
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }
}
