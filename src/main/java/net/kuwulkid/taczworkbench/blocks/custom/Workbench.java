package net.kuwulkid.taczworkbench.blocks.custom;

import com.tacz.guns.item.AttachmentItem;
import com.tacz.guns.item.ModernKineticGunItem;
import com.tacz.guns.network.NetworkHandler;
import net.kuwulkid.taczworkbench.blocks.entity.ModBlockEntities;
import net.kuwulkid.taczworkbench.blocks.entity.WorkbenchEntity;
import net.kuwulkid.taczworkbench.blocks.helper.ShapeUtils;
import net.kuwulkid.taczworkbench.network.AttachmentMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
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

import static com.tacz.guns.api.item.attachment.AttachmentType.*;

public class Workbench extends BaseEntityBlock {
    public static final EnumProperty<BedPart> PART = BlockStateProperties.BED_PART;
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public VoxelShape makeShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.00625, 0.9375, -0.05625, 0.99375, 1, 0.93125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0125, 0.75, 0.125, 0.9875, 0.9375, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0125, 0, 0, 0.1375, 0.9375, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0125, 0, 0.75, 0.1375, 0.9375, 0.875), BooleanOp.OR);
        return shape;
    }

    public VoxelShape headShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.00625, 0.9375, -0.05625, 0.99375, 1, 0.93125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0125, 0.75, 0.125, 0.9875, 0.9375, 0.75), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.8625, 0, 0, 0.9875, 0.9375, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.8625, 0, 0.75, 0.9875, 0.9375, 0.875), BooleanOp.OR);
        return shape;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        if (state.getValue(PART) == BedPart.HEAD) {
            return ShapeUtils.rotateY(headShape(), Direction.NORTH, facing);
        }
        return ShapeUtils.rotateY(makeShape(), Direction.NORTH, facing);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        Direction facing = state.getValue(FACING);
        Direction left = facing.getClockWise();
        BlockPos otherPos = pos.relative(left);
        level.setBlock(otherPos, state.setValue(PART, BedPart.HEAD), 3);
    }

    public Workbench(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, BedPart.FOOT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction facing = ctx.getHorizontalDirection().getOpposite();
        BlockPos pos = ctx.getClickedPos();
        Direction left = facing.getClockWise();
        BlockPos otherPos = pos.relative(left);

        if (!ctx.getLevel().getBlockState(otherPos).canBeReplaced()) {
            return null; // Cancel placement if other part cannot be placed
        }

        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(PART, BedPart.FOOT);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getValue(PART) == BedPart.HEAD) {
            return null;
        }
        return new WorkbenchEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        if (state.getValue(PART) == BedPart.HEAD) {
            return RenderShape.INVISIBLE;
        }
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean isMoving) {
        if (state.getBlock() == newState.getBlock()) {
            super.onRemove(state, level, pos, newState, isMoving);
            return;
        }

        Direction left = state.getValue(FACING).getClockWise();
        BlockPos other = (state.getValue(PART) == BedPart.FOOT)
                ? pos.relative(left)
                : pos.relative(left.getOpposite());

        BlockState otherState = level.getBlockState(other);
        if (otherState.getBlock() == this) {
            level.destroyBlock(other, false);
        }

        if (state.getValue(PART) == BedPart.FOOT) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof WorkbenchEntity w) {
                w.drops();
            }
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (state.getValue(PART) == BedPart.HEAD) {
            Direction left = state.getValue(FACING).getClockWise();
            pos = pos.relative(left.getOpposite());
            state = level.getBlockState(pos);
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof WorkbenchEntity workbench)) {
            return InteractionResult.PASS;
        }

        ItemStack mainHand = player.getMainHandItem();

        if (!mainHand.isEmpty() && workbench.getItem(0).isEmpty()) {
            ItemStack toInsert = mainHand.copy();
            toInsert.setCount(1);
            workbench.setItem(0, toInsert);
            mainHand.shrink(1);
            workbench.setChanged();
            return InteractionResult.SUCCESS;
            //put item on bench
        }

        if (mainHand.isEmpty() && !workbench.getItem(0).isEmpty()) {
            Containers.dropItemStack(level, pos.getX(), pos.getY() + .93, pos.getZ(),
                    workbench.removeItem(0));
            workbench.setChanged();
            //remove itom on bench
            return InteractionResult.SUCCESS;
        }


        if(mainHand.getItem().toString().equals("attachment") && workbench.getItem(0).getItem() instanceof ModernKineticGunItem){
            ItemStack handAttachment = mainHand;
            AttachmentItem attachmentItem = (AttachmentItem) handAttachment.getItem();
            ModernKineticGunItem tableItem = (ModernKineticGunItem) workbench.getItem(0).getItem();
            System.out.println("I AM A " + attachmentItem.getType(mainHand));


            if(!tableItem.allowAttachment(workbench.getItem(0), mainHand)){
                return InteractionResult.FAIL;
            }

            if(!tableItem.getAttachmentId(workbench.getItem(0), SCOPE).toString().equals("empty") && attachmentItem.getType(mainHand).equals(SCOPE)){ //if not empty
                player.addItem(tableItem.getAttachment(workbench.getItem(0), SCOPE)); //gives player the attachment in slot
            }
            else if(!tableItem.getAttachmentId(workbench.getItem(0), STOCK).toString().equals("empty") && attachmentItem.getType(mainHand).equals(STOCK)){
                player.addItem(tableItem.getAttachment(workbench.getItem(0), STOCK)); //gives player the attachment in slot
            }
            else if(!tableItem.getAttachmentId(workbench.getItem(0), LASER).toString().equals("empty") && attachmentItem.getType(mainHand).equals(LASER)){
                player.addItem(tableItem.getAttachment(workbench.getItem(0), LASER)); //gives player the attachment in slot
            }
            else if(!tableItem.getAttachmentId(workbench.getItem(0), MUZZLE).toString().equals("empty") && attachmentItem.getType(mainHand).equals(MUZZLE)){
                player.addItem(tableItem.getAttachment(workbench.getItem(0), MUZZLE)); //gives player the attachment in slot
            }
            else if(!tableItem.getAttachmentId(workbench.getItem(0), GRIP).toString().equals("empty") && attachmentItem.getType(mainHand).equals(GRIP)){
                player.addItem(tableItem.getAttachment(workbench.getItem(0), GRIP)); //gives player the attachment in slot
            }


            tableItem.installAttachment(workbench.getItem(0), handAttachment);
            System.out.println("HAND ATTACHMENT:" + handAttachment);
            AttachmentMessage message = new AttachmentMessage(handAttachment, workbench.getItem(0), SCOPE); //dont know what scope does
            NetworkHandler.CHANNEL.sendToServer(message);
            if(!player.getAbilities().instabuild){
                mainHand.shrink(1);
                //in creative mode
            }
            if (level.isClientSide) {
                Vec3 hitPos = hit.getLocation();
                level.addParticle(ParticleTypes.SMOKE, hitPos.x, hitPos.y, hitPos.z, 0, 0, 0);
                level.playLocalSound(pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide()) {
            return null;
        }
        return createTickerHelper(pBlockEntityType, ModBlockEntities.WORKBENCH.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1, pPos, pState1));
    }

    public boolean hasBlockEntity(BlockState state) {
        return state.getValue(PART) == BedPart.FOOT;
    }
}
