package com.gtocore.common.network;

import com.gtocore.client.ClientCache;
import com.gtocore.config.GTOConfig;
import com.gtocore.integration.emi.EmiPersist;

import com.gtolib.GTOCore;
import com.gtolib.api.misc.PlanetManagement;
import com.gtolib.api.player.organ.capability.OrganCapability;
import com.gtolib.mixin.BookContentResourceListenerLoaderAccessor;
import com.gtolib.utils.ServerUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import dev.emi.emi.runtime.EmiPersistentData;
import org.jetbrains.annotations.Nullable;
import vazkii.patchouli.client.book.BookContentResourceListenerLoader;
import vazkii.patchouli.client.book.ClientBookRegistry;

public final class ServerMessage {

    public static void sendData(MinecraftServer server, @Nullable ServerPlayer player, String channel, @Nullable CompoundTag data) {
        if (player != null) {
            new FromServerMessage(channel, data).sendTo(player);
        } else {
            new FromServerMessage(channel, data).sendToAll(server);
        }
    }

    public static void disableDrift(ServerPlayer serverPlayer, boolean drift) {
        CompoundTag data = new CompoundTag();
        data.putBoolean("disableDrift", drift);
        sendData(serverPlayer.server, serverPlayer, "disableDrift", data);
    }

    public static void planetUnlock(ServerPlayer serverPlayer, ResourceLocation planet) {
        CompoundTag data = new CompoundTag();
        data.putString("planet", planet.toString());
        sendData(serverPlayer.server, serverPlayer, "planetUnlock", data);
    }

    public static void organCapAsync(ServerPlayer serverPlayer, CompoundTag tag) { // ServerToClient 全量同步
        sendData(serverPlayer.server, serverPlayer, "organCapAsync", tag);
    }

    static void handle(String channel, @Nullable Player player, CompoundTag data) {
        if (player == null) return;
        switch (channel) {
            case "planetUnlock" -> {
                ResourceLocation planet = new ResourceLocation(data.getString("planet"));
                PlanetManagement.clientUnlock(planet);
            }
            case "disableDrift" -> ClientCache.disableDrift = data.getBoolean("disableDrift");
            case "loggedIn" -> {
                ClientCache.UNLOCKED_PLANET.clear();
                if (Minecraft.getInstance().level != null && !ClientCache.initializedBook) {
                    ClientCache.initializedBook = true;
                    Thread thread = new Thread(() -> {
                        ClientBookRegistry.INSTANCE.reload();
                        ((BookContentResourceListenerLoaderAccessor) BookContentResourceListenerLoader.INSTANCE).getData().clear();
                    });
                    thread.setDaemon(true);
                    thread.start();
                }
                ClientCache.SERVER_IDENTIFIER = data.getUUID(ServerUtils.IDENTIFIER_KEY);
                if (!GTOConfig.INSTANCE.emiGlobalFavorites && EmiPersist.needsRefresh) {
                    // emi has loaded before we receive SERVER_IDENTIFIER, reload it.
                    EmiPersistentData.load();
                    GTOCore.LOGGER.warn("emi reloaded");
                    EmiPersist.needsRefresh = false;
                }
            }
            case "organCapAsync" -> OrganCapability.of(player).deserializeNBT(data);
        }
    }
}
