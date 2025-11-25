package net.kuwulkid.taczworkbench.mixins;

import net.kuwulkid.taczworkbench.network.AttachmentMessage;
import net.minecraftforge.network.NetworkDirection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.tacz.guns.network.NetworkHandler.CHANNEL;

@Mixin(targets = "com.tacz.guns.network.NetworkHandler")
public class NetworkHandlerMixin {

    @Shadow
    private static final AtomicInteger ID_COUNT = new AtomicInteger(1);

    @Inject(method = "init", at = @At("TAIL"))
    private static void inject(CallbackInfo ci) {
        CHANNEL.registerMessage(ID_COUNT.getAndIncrement(), AttachmentMessage.class, AttachmentMessage::encode, AttachmentMessage::decode, AttachmentMessage::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

}