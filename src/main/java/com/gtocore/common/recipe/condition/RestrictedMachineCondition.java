package com.gtocore.common.recipe.condition;

import com.gtolib.GTOCore;
import com.gtolib.api.recipe.Recipe;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

public final class RestrictedMachineCondition extends AbstractRecipeCondition {

    private static final RestrictedMachineCondition MULTIBLOCK = new RestrictedMachineCondition(GTOCore.id("multiblock"));

    public static RestrictedMachineCondition multiblock() {
        return MULTIBLOCK;
    }

    private final ResourceLocation id;

    private MachineDefinition definition;

    public RestrictedMachineCondition(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public RecipeConditionType<?> getType() {
        return RESTRICTED_MACHINE;
    }

    @Override
    public Component getTooltips() {
        if (id.equals(MULTIBLOCK.id)) {
            return Component.translatable("gtocore.recipe.restricted_machine", Component.translatable("gtceu.multiblock.title"));
        }
        if (definition == null) {
            definition = GTRegistries.MACHINES.get(id);
        }
        MachineDefinition machineDefinition = definition;
        return Component.translatable("gtocore.recipe.restricted_machine", machineDefinition == null ? "null" : Component.translatable(machineDefinition.getDescriptionId()));
    }

    @Override
    public boolean test(@NotNull Recipe recipe, @NotNull RecipeLogic recipeLogic) {
        MachineDefinition machineDefinition = recipeLogic.getMachine().getDefinition();
        if (this.equals(MULTIBLOCK)) {
            return recipeLogic.getMachine() instanceof MultiblockControllerMachine;
        }
        return machineDefinition.getId().equals(id);
    }
}
