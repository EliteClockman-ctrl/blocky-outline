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

public final class LegacyTargetExtractor implements TargetExtractor {
    private final TargetInfo cached = new TargetInfo();

    @Override
    public TargetInfo extract(Object targetObj, Minecraft mc) {
        if (targetObj instanceof BlockHitResult hit && mc.level != null) {
            BlockPos pos = hit.getBlockPos();
            BlockState state = mc.level.getBlockState(pos);
            Entity camEntity = mc.getCameraEntity() != null ? mc.getCameraEntity() : mc.player;
            VoxelShape shape = state.getShape(mc.level, pos,
                    camEntity != null ? CollisionContext.of(camEntity) : CollisionContext.empty());
            cached.set(pos, shape);
            return cached;
        }
        return null;
    }
}
