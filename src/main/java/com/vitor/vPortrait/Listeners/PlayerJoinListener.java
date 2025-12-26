/*
 * Copyright (c) 2025 Vitor (SoldadoHumano)
 * This file is part of vPortrait.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.vitor.vPortrait.Listeners;

import com.vitor.vPortrait.vPortrait;
import io.netty.channel.*;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;

import java.util.logging.Level;

/**
 * Optimized Packet Interceptor to handle portrait synchronization.
 */
public class PlayerJoinListener implements Listener {
    private final vPortrait plugin;
    private static final String HANDLER_NAME = "vportrait_packet_handler";

    public PlayerJoinListener(vPortrait plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Injects the packet handler into the player's network pipeline
        injectPlayer(player);

        // Synchronize all portraits after a small delay to ensure client readiness
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getPortraitManager().syncAllPortraitsToPlayer(player);
            plugin.log(Level.INFO, "Portraits synchronized for player: " + player.getName());
        }, 40L); // 2-second delay
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        removePlayer(event.getPlayer());
    }

    private void injectPlayer(Player player) {
        ChannelDuplexHandler handler = new ChannelDuplexHandler() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                try {
                    // Intercept the packet that notifies the client about a new entity spawn
                    if (msg instanceof ClientboundAddEntityPacket packet) {
                        int entityId = packet.getId();

                        // Sync back to the main server thread to access the Bukkit API safely
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            try {
                                // Security check: ensure the player hasn't disconnected and the world is still loaded
                                if (!player.isOnline() || player.getWorld() == null) {
                                    return;
                                }

                                // Retrieve the entity from the player's current world using the ID from the packet
                                // We use a stream for a clean search within the world's entity list
                                Entity entity = player.getWorld().getEntities().stream()
                                        .filter(e -> e.getEntityId() == entityId)
                                        .findFirst()
                                        .orElse(null);

                                // Verify if the spawned entity is an ItemFrame (or GlowItemFrame)
                                if (entity instanceof ItemFrame frame) {
                                    ItemStack item = frame.getItem();

                                    // verify if the item is a map and contains valid metadata
                                    if (item != null && item.getType().toString().contains("MAP")
                                            && item.getItemMeta() instanceof MapMeta meta) {

                                        if (meta.hasMapView() && meta.getMapView() != null) {
                                            // Force send the map data to the client immediately
                                            player.sendMap(meta.getMapView());

                                            plugin.log(Level.FINE, String.format(
                                                    "Successfully forced map update for %s (Entity ID: %d)",
                                                    player.getName(), entityId
                                            ));
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                // Specific catch for the scheduled task logic to prevent main thread issues
                                plugin.log(Level.SEVERE, "An error occurred during scheduled portrait processing for "
                                        + player.getName() + ": " + e.getMessage());
                            }
                        });
                    }
                } catch (Exception e) {
                    // Outer catch to protect the Netty pipeline from unexpected failures
                    plugin.log(Level.SEVERE, "Critical failure in vPortrait Netty Packet Handler for "
                            + player.getName() + ": " + e.getMessage());
                    e.printStackTrace();
                }

                // Always call the super method to ensure the packet flow is never interrupted
                super.write(ctx, msg, promise);
            }
        };

        try {
            // Safe cast to CraftPlayer to access the underlying NMS connection and Netty channel
            if (player instanceof CraftPlayer craftPlayer) {
                Channel channel = craftPlayer.getHandle().connection.connection.channel;

                // Inject the handler into the pipeline if it isn't already there
                if (channel != null && channel.pipeline().get(HANDLER_NAME) == null) {
                    // We add it before the default 'packet_handler' to intercept outbound packets
                    channel.pipeline().addBefore("packet_handler", HANDLER_NAME, handler);
                    plugin.log(Level.FINE, "Netty pipeline successfully injected for " + player.getName());
                }
            }
        } catch (Exception e) {
            // Log injection failures; this usually happens if the connection is closed during join
            plugin.log(Level.SEVERE, "Failed to inject Netty pipeline for " + player.getName() + ": " + e.getMessage());
        }
    }

    private void removePlayer(Player player) {
        try {
            Channel channel = ((CraftPlayer) player).getHandle().connection.connection.channel;
            if (channel.pipeline().get(HANDLER_NAME) != null) {
                channel.pipeline().remove(HANDLER_NAME);
                plugin.log(Level.FINE, "Netty pipeline removed for " + player.getName());
            }
        } catch (Exception ignored) {}
    }
}