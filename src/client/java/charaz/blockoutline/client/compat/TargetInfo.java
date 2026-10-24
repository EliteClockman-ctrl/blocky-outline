package charaz.blockoutline.client.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;

public record TargetInfo(BlockPos pos, VoxelShape shape) {
}
