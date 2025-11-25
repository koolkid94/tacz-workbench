package net.kuwulkid.taczworkbench.blocks.helper;


import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.concurrent.atomic.AtomicReference;

public class ShapeUtils {
    public static VoxelShape rotateY(VoxelShape shape, Direction from, Direction to) {
        if (from == to || shape == Shapes.empty()) return shape;

        int times = (to.get2DDataValue() - from.get2DDataValue() + 4) % 4;
        VoxelShape result = shape;

        for (int i = 0; i < times; i++) {
            result = rotateY90(result);
        }

        return result;
    }

    private static VoxelShape rotateY90(VoxelShape shape) {
        // This is a simple voxel rotation: swap x and z, invert x
        // WARNING: This only works for shapes built from Shapes.box
        // For complex shapes, may need more advanced method
        //thank you mr gpt
        AtomicReference<VoxelShape> result = new AtomicReference<>(Shapes.empty());

        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double newMinX = 1 - maxZ;
            double newMaxX = 1 - minZ;
            double newMinZ = minX;
            double newMaxZ = maxX;
            result.set(Shapes.or(result.get(), Shapes.box(newMinX, minY, newMinZ, newMaxX, maxY, newMaxZ)));
        });

        return result.get();
    }
}

