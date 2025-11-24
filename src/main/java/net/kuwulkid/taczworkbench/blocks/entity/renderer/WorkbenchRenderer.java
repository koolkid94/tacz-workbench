package net.kuwulkid.taczworkbench.blocks.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.kuwulkid.taczworkbench.blocks.custom.Workbench;
import net.kuwulkid.taczworkbench.blocks.entity.WorkbenchEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public class WorkbenchRenderer implements BlockEntityRenderer<WorkbenchEntity> {
    private final ModelPart headRoot;
    private final ModelPart footRoot;

    public WorkbenchRenderer(BlockEntityRendererProvider.Context context) {
        this.headRoot = context.bakeLayer(ModelLayers.BED_HEAD);
        this.footRoot = context.bakeLayer(ModelLayers.BED_FOOT);
    }

    @Override
    public void render(WorkbenchEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack,
                       MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {


        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ItemStack itemStack = pBlockEntity.getRenderStack(0);
        if(itemStack.isEmpty()){
            return;
        }


        pPoseStack.pushPose();
        pPoseStack.scale(0.67f, 0.67f, 0.67f);
        double yHeight = 1.52f;
        Direction facing = pBlockEntity.getBlockState().getValue(Workbench.FACING);
        if(facing.toString().equals("east")){

            pPoseStack.translate(.89d, yHeight, 1.5f);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(270));
            pPoseStack.mulPose(Axis.XP.rotationDegrees(90));
        }
        else if(facing.toString().equals("north")){

            pPoseStack.translate(1.5d, yHeight, 0.62f);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(270));
            pPoseStack.mulPose(Axis.XP.rotationDegrees(90));
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(270));

        }
        else if(facing.toString().equals("south")){
            pPoseStack.translate(-0.03d, yHeight, 0.86f);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(270));
            pPoseStack.mulPose(Axis.XP.rotationDegrees(90));
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(-270));
        }
        else  if(facing.toString().equals("west")){
            pPoseStack.translate(.59d, yHeight, -0.04f);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(-270));
            pPoseStack.mulPose(Axis.XP.rotationDegrees(90));
        }
        else
        {

            pPoseStack.translate(1.04d, yHeight, 0.5f);
            pPoseStack.mulPose(Axis.XP.rotationDegrees(270));
        }

        itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, getLightLevel(pBlockEntity.getLevel(), pBlockEntity.getBlockPos()),
                OverlayTexture.NO_OVERLAY, pPoseStack, pBuffer, pBlockEntity.getLevel(), 1);
        pPoseStack.popPose();
    }

    private int getLightLevel(Level level, BlockPos pos) {
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }
}