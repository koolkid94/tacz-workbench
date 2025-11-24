package net.kuwulkid.taczworkbench.blocks.custom;

import net.kuwulkid.taczworkbench.blocks.entity.ModBlockEntities;
import net.kuwulkid.taczworkbench.blocks.entity.WorkbenchEntity;
import net.kuwulkid.taczworkbench.blocks.helper.ShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class Workbench extends BaseEntityBlock {
    public static final EnumProperty<BedPart> PART = BlockStateProperties.BED_PART;


    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public VoxelShape makeShape(){
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.00625, 0.75, -0.05625, 1.99375, 1, 0.99375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0125, 0.75, 0.125, 1.9875, 0.9375, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0125, 0, 0, 0.1375, 0.9375, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(1.8625, 0, 0, 1.9875, 0.9375, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0125, 0, 0.75, 0.1375, 0.9375, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(1.8625, 0, 0.75, 1.9875, 0.9375, 0.875), BooleanOp.OR);

        return shape;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        return ShapeUtils.rotateY(makeShape(), Direction.NORTH, facing);
    }

    public Workbench(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, BedPart.FOOT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        // Player’s facing direction, opposite because blocks "face" toward the player
        Direction playerFacing = ctx.getHorizontalDirection().getOpposite();
        return this.defaultBlockState().setValue(FACING, playerFacing);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new WorkbenchEntity( pPos, pState);
    }

    public RenderShape getRenderShape(BlockState pState){
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof WorkbenchEntity) {
                ((WorkbenchEntity) blockEntity).drops();
            }
        }

        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {

        WorkbenchEntity workbench = (WorkbenchEntity) pLevel.getBlockEntity(pPos);
        ItemStack mainHand = pPlayer.getMainHandItem();
        if (!mainHand.isEmpty() && workbench.getItem(0).isEmpty()) {
            ItemStack toInsert = mainHand.copy();
            toInsert.setCount(1); // only take 1 item (change if needed)
            workbench.setItem(0, toInsert);
            System.out.println("TO INSERT" + toInsert);
            mainHand.shrink(1);
            workbench.setChanged();
            return InteractionResult.CONSUME;
        }
        if (mainHand.isEmpty()) {
            assert workbench != null;
            if (!workbench.getItem(0).isEmpty()) {
                Containers.dropItemStack(pLevel, pPos.getX(), pPos.getY() + .43, pPos.getZ(), workbench.removeItem(0));
                workbench.setChanged();
            }
        }


        Vec3 hitPos = pHit.getLocation();   // <-- EXACT world-space click location
        Direction face = pHit.getDirection(); // Which face

        // Convert to coordinates *inside the block* (0–1 range)
        double localX = hitPos.x - pPos.getX();
        double localY = hitPos.y - pPos.getY();
        double localZ = hitPos.z - pPos.getZ();

        System.out.println("Hit local coords: " +
                localX + ", " + localY + ", " + localZ);

        if (pLevel.isClientSide) { // Only run on client
            pLevel.addParticle(
                    ParticleTypes.FLAME,
                    hitPos.x, hitPos.y, hitPos.z,
                    0, 0, 0 // motion
            );
        }



        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if(pLevel.isClientSide()) {
            return null;
        }

        return createTickerHelper(pBlockEntityType, ModBlockEntities.WORKBENCH.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1, pPos, pState1));
    }
}
