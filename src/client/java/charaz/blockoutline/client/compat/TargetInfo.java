package charaz.blockoutline.client.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class TargetInfo {
    private BlockPos pos;
    private VoxelShape shape;

    public TargetInfo() {
    }

    public TargetInfo(BlockPos pos, VoxelShape shape) {
        this.pos = pos;
        this.shape = shape;
    }

    public void set(BlockPos pos, VoxelShape shape) {
        this.pos = pos;
        this.shape = shape;
    }

    public BlockPos pos() {
        return pos;
    }

    public VoxelShape shape() {
        return shape;
    }
}
