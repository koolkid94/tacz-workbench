package net.kuwulkid.taczworkbench.datagen;

import net.kuwulkid.taczworkbench.Taczworkbench;
import net.kuwulkid.taczworkbench.blocks.BlocksInit;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Taczworkbench.MOD_ID, exFileHelper);
    }



    @Override
    protected void registerStatesAndModels() {

        simpleBlockWithItem(BlocksInit.WORKBENCH.get(),
                new ModelFile.UncheckedModelFile(modLoc("block/workbench")));
    }

    private void blockItem(RegistryObject<Block> blockRegistryObject) {
        simpleBlockItem(blockRegistryObject.get(), new ModelFile.UncheckedModelFile(Taczworkbench.MOD_ID +
                ":block/" + ForgeRegistries.BLOCKS.getKey(blockRegistryObject.get()).getPath()));
    }

}
