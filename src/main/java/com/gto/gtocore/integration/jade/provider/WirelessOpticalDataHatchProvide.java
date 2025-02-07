package com.gto.gtocore.integration.jade.provider;

import com.gto.gtocore.GTOCore;
import com.gto.gtocore.api.capability.BindCapability;
import com.gto.gtocore.common.machine.multiblock.part.WirelessOpticalDataHatchMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.integration.jade.provider.CapabilityBlockProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * @author EasterFG on 2024/10/27
 */
public final class WirelessOpticalDataHatchProvide extends CapabilityBlockProvider<BindCapability> {

    public WirelessOpticalDataHatchProvide() {
        super(GTOCore.id("wireless_data_hatch_provider"));
    }

    @Override
    protected @Nullable BindCapability getCapability(Level level, BlockPos pos, @Nullable Direction side) {
        if (level.getBlockEntity(pos) instanceof MetaMachineBlockEntity metaMachineBlockEntity &&
                metaMachineBlockEntity.getMetaMachine() instanceof BindCapability capability) {
            return capability;
        }
        return null;
    }

    @Override
    protected void write(CompoundTag data, BindCapability capability) {
        if (capability != null) {
            data.putBoolean("is_bind", capability.bind());
            data.putString("pos", capability.pos());
        } else {
            data.putBoolean("is_bind", false);
            data.putString("pos", "");
        }
    }

    @Override
    protected void addTooltip(CompoundTag capData, ITooltip tooltip, Player player, BlockAccessor block, BlockEntity blockEntity, IPluginConfig config) {
        if (!(blockEntity instanceof MetaMachineBlockEntity metaMachineBlockEntity)) return;
        var metaMachine = metaMachineBlockEntity.getMetaMachine();
        if (metaMachine instanceof WirelessOpticalDataHatchMachine wod) {
            if (capData.getBoolean("is_bind")) {
                if (wod.isTransmitter()) {
                    tooltip.add(Component.translatable("gtocore.machine.wireless_data_transmitter_hatch.bind", capData.getString("pos")));
                } else {
                    tooltip.add(Component.translatable("gtocore.machine.wireless_data_receiver_hatch.bind", capData.getString("pos")));
                }
            } else {
                if (wod.isTransmitter()) {
                    tooltip.add(Component.translatable("gtocore.machine.wireless_data_transmitter_hatch.unbind"));
                } else {
                    tooltip.add(Component.translatable("gtocore.machine.wireless_data_receiver_hatch.unbind"));
                }
            }
        }
    }
}
