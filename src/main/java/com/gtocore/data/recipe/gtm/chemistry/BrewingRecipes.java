package com.gtocore.data.recipe.gtm.chemistry;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.dust;
import static com.gregtechceu.gtceu.common.data.GTItems.BIO_CHAFF;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gtocore.common.data.GTORecipeTypes.BREWING_RECIPES;

final class BrewingRecipes {

    public static void init() {
        for (Material material : new Material[] { Talc, Soapstone, Redstone }) {
            BREWING_RECIPES.recipeBuilder("lubricant_from_oil_and_" + material.getName())
                    .inputItems(dust, material)
                    .inputFluids(Oil.getFluid(1000))
                    .outputFluids(Lubricant.getFluid(1000))
                    .duration(128).EUt(4).save();

            BREWING_RECIPES.recipeBuilder("lubricant_from_creosote_and_" + material.getName())
                    .inputItems(dust, material)
                    .inputFluids(Creosote.getFluid(1000))
                    .outputFluids(Lubricant.getFluid(1000))
                    .duration(128).EUt(4).save();

            BREWING_RECIPES.recipeBuilder("lubricant_from_seed_oil_and_" + material.getName())
                    .inputItems(dust, material)
                    .inputFluids(SeedOil.getFluid(1000))
                    .outputFluids(Lubricant.getFluid(1000))
                    .duration(128).EUt(4).save();
        }

        // Biomass
        BREWING_RECIPES.recipeBuilder("biomass_from_sapling").duration(800).EUt(3).inputItems(ItemTags.SAPLINGS)
                .inputFluids(Water.getFluid(100)).outputFluids(Biomass.getFluid(100)).save();
        BREWING_RECIPES.recipeBuilder("biomass_from_potato").duration(160).EUt(3).inputItems(Items.POTATO)
                .inputFluids(Water.getFluid(20)).outputFluids(Biomass.getFluid(20)).save();
        BREWING_RECIPES.recipeBuilder("biomass_from_carrot").duration(160).EUt(3).inputItems(Items.CARROT)
                .inputFluids(Water.getFluid(20)).outputFluids(Biomass.getFluid(20)).save();
        BREWING_RECIPES.recipeBuilder("biomass_from_cactus").duration(160).EUt(3).inputItems(Blocks.CACTUS.asItem())
                .inputFluids(Water.getFluid(20)).outputFluids(Biomass.getFluid(20)).save();
        BREWING_RECIPES.recipeBuilder("biomass_from_sugar_cane").duration(160).EUt(3).inputItems(Items.SUGAR_CANE)
                .inputFluids(Water.getFluid(20)).outputFluids(Biomass.getFluid(20)).save();
        BREWING_RECIPES.recipeBuilder("biomass_from_brown_mushroom").duration(160).EUt(3)
                .inputItems(Blocks.BROWN_MUSHROOM.asItem()).inputFluids(Water.getFluid(20))
                .outputFluids(Biomass.getFluid(20)).save();
        BREWING_RECIPES.recipeBuilder("biomass_from_red_mushroom").duration(160).EUt(3)
                .inputItems(Blocks.RED_MUSHROOM.asItem()).inputFluids(Water.getFluid(20))
                .outputFluids(Biomass.getFluid(20)).save();
        BREWING_RECIPES.recipeBuilder("biomass_from_beetroot").duration(160).EUt(3).inputItems(Items.BEETROOT)
                .inputFluids(Water.getFluid(20)).outputFluids(Biomass.getFluid(20)).save();
        BREWING_RECIPES.recipeBuilder("biomass_from_bio_chaff").EUt(4).duration(128).inputItems(BIO_CHAFF)
                .inputFluids(Water.getFluid(750)).outputFluids(Biomass.getFluid(750)).save();
        BREWING_RECIPES.recipeBuilder("biomass_from_kelp").duration(160).EUt(3).inputItems(Blocks.KELP.asItem())
                .inputFluids(Water.getFluid(20)).outputFluids(Biomass.getFluid(20)).save();
        BREWING_RECIPES.recipeBuilder("biomass_from_sea_pickle").duration(160).EUt(3)
                .inputItems(Blocks.SEA_PICKLE.asItem())
                .inputFluids(Water.getFluid(20)).outputFluids(Biomass.getFluid(20)).save();
        BREWING_RECIPES.recipeBuilder("biomass_from_sweet_berries").duration(160).EUt(3).inputItems(Items.SWEET_BERRIES)
                .inputFluids(Water.getFluid(20)).outputFluids(Biomass.getFluid(20)).save();
        BREWING_RECIPES.recipeBuilder("biomass_from_crimson_fungus").duration(160).EUt(3)
                .inputItems(Items.CRIMSON_FUNGUS)
                .inputFluids(Water.getFluid(20)).outputFluids(Biomass.getFluid(20)).save();
        BREWING_RECIPES.recipeBuilder("biomass_from_warped_fungus").duration(160).EUt(3).inputItems(Items.WARPED_FUNGUS)
                .inputFluids(Water.getFluid(20)).outputFluids(Biomass.getFluid(20)).save();
        BREWING_RECIPES.recipeBuilder("biomass_from_glow_berries").duration(160).EUt(3).inputItems(Items.GLOW_BERRIES)
                .inputFluids(Water.getFluid(20)).outputFluids(Biomass.getFluid(20)).save();
        BREWING_RECIPES.recipeBuilder("biomass_from_pitcher_pod").duration(160).EUt(3).inputItems(Items.PITCHER_POD)
                .inputFluids(Water.getFluid(20)).outputFluids(Biomass.getFluid(20)).save();
        BREWING_RECIPES.recipeBuilder("biomass_from_torchflower_seeds").duration(160).EUt(3)
                .inputItems(Items.TORCHFLOWER_SEEDS)
                .inputFluids(Water.getFluid(20)).outputFluids(Biomass.getFluid(20)).save();
    }
}
