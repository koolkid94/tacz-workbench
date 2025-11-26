package net.kuwulkid.taczworkbench.network;


import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.network.message.ServerMessageRefreshRefitScreen;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static com.mojang.text2speech.Narrator.LOGGER;

public class AttachmentMessage {
    private final ItemStack attachmentItem;
    private final ItemStack gunItem;
    private final AttachmentType attachmentType;

    public AttachmentMessage(ItemStack attachmentItem, ItemStack gunItem, AttachmentType attachmentType) {
        this.attachmentItem = attachmentItem;
        this.gunItem = gunItem;
        this.attachmentType = attachmentType;
    }

    public static void encode(AttachmentMessage message, FriendlyByteBuf buf) {
        buf.writeItem(message.attachmentItem);
        buf.writeItem(message.gunItem);
        buf.writeEnum(message.attachmentType);
    }

    public static AttachmentMessage decode(FriendlyByteBuf buf) {
        return new AttachmentMessage(buf.readItem(), buf.readItem(), buf.readEnum(AttachmentType.class));
    }

    public static void handle(AttachmentMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();
                if (player == null) {
                    return;
                }
                ItemStack attachmentItem = message.attachmentItem;
                ItemStack gunItem = message.gunItem;
                IGun iGun = IGun.getIGunOrNull(gunItem);
                if (iGun != null) {
                    if (iGun.allowAttachment(gunItem, attachmentItem)) {
                        iGun.installAttachment(gunItem, attachmentItem);
                        // 刷新配件数据
                        AttachmentPropertyManager.postChangeEvent(player, gunItem);
                        // 如果卸载的是扩容弹匣，吐出所有子弹
                        player.inventoryMenu.broadcastChanges();
                        LOGGER.info("Sending packet: {}", message.getClass().getName());
                        NetworkHandler.sendToClientPlayer(new ServerMessageRefreshRefitScreen(), player);
                        //can maybe cut down on bloat here
                    }
                }
            });
        }
        context.setPacketHandled(true);
    }

}