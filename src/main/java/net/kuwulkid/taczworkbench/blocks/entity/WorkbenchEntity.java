package net.kuwulkid.taczworkbench.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorkbenchEntity extends BlockEntity {
    private final ItemStackHandler itemhandler = new ItemStackHandler(1);

    private static final int INPUT_SLOT = 0;
    protected final ContainerData data;

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.of(() -> itemhandler);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
       if(cap == ForgeCapabilities.ITEM_HANDLER){
           return lazyItemHandler.cast();
       }

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() ->itemhandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    public WorkbenchEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.WORKBENCH.get() ,pPos, pBlockState);
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex){
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {

            }

            @Override
            public int getCount() {
                return 0;
            }
        };
    }

    public void drops(){
        SimpleContainer inventory = new SimpleContainer(itemhandler.getSlots());
        for(int i = 0; i < itemhandler.getSlots(); i++){
            inventory.setItem(i, itemhandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public Component getDisplayName(){
        return Component.translatable("block.tacz.gun_smith_table");
    }

    protected void saveAdditional(CompoundTag pTag){
        pTag.put("inventory", itemhandler.serializeNBT());

        super.saveAdditional(pTag);
    }

    public void load(CompoundTag pTag){
        super.load(pTag);
        itemhandler.deserializeNBT(pTag.getCompound("inventory"));
    }

    public void tick(Level pLevel1, BlockPos pPos, BlockState pState1) {

    }

    public ItemStack getRenderStack(int slot) {
        //System.out.println(itemhandler.getStackInSlot(slot) + " RENDERSTACK");
        return itemhandler.getStackInSlot(slot);
    }

    public ItemStack getItem(int slot) {
       // System.out.println(itemhandler.getStackInSlot(slot) + " GET ITEM");
        return itemhandler.getStackInSlot(slot);
    }

    public void setItem(int slot, ItemStack stack) {
        itemhandler.setStackInSlot(slot, stack);
        setChanged(); // marks chunk dirty
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public ItemStack removeItem(int slot) {
        ItemStack stack = itemhandler.getStackInSlot(slot);
        itemhandler.setStackInSlot(slot, ItemStack.EMPTY);
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return stack;
    }


    @Override
    public CompoundTag getUpdateTag() {
        // Called when the chunk is sent to the client
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        // Called when the block changes and client needs to be updated
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void onDataPacket(ClientGamePacketListener net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }


}
