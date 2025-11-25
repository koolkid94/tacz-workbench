package net.kuwulkid.taczworkbench.event;

import net.kuwulkid.taczworkbench.Taczworkbench;
import net.kuwulkid.taczworkbench.blocks.entity.ModBlockEntities;
import net.kuwulkid.taczworkbench.blocks.entity.renderer.WorkbenchRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Taczworkbench.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEventBusClientEvents {
    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
    }

    @SubscribeEvent
    public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.WORKBENCH.get(), WorkbenchRenderer::new);

    }
}