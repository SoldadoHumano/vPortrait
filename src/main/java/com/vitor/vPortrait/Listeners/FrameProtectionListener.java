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
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.logging.Level;

/**
 * Complete protection for vPortrait frames to ensure security and persistence.
 */
public class FrameProtectionListener implements Listener {

    private final vPortrait plugin;
    private static final String PORTRAIT_METADATA = "vPortraitFrame";

    public FrameProtectionListener(vPortrait plugin) {
        this.plugin = plugin;
    }

    /**
     * Marks an entity as a vPortrait component and applies physical locks.
     */
    public static void markAsPortraitFrame(Entity frame, vPortrait plugin) {
        frame.setMetadata(PORTRAIT_METADATA, new FixedMetadataValue(plugin, true));
        frame.setPersistent(true);

        if (frame instanceof ItemFrame itemFrame) {
            itemFrame.setFixed(true);
            itemFrame.setInvulnerable(true);
            itemFrame.setSilent(true);
        }
    }

    /**
     * Checks if the entity is registered as a vPortrait frame.
     */
    public static boolean isPortraitFrame(Entity frame) {
        return frame.hasMetadata(PORTRAIT_METADATA);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFrameInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof ItemFrame frame && isPortraitFrame(frame)) {
            event.setCancelled(true);

            plugin.log(Level.FINE, String.format(
                    "Protection: %s tried to interact with a portrait frame at %s",
                    event.getPlayer().getName(),
                    frame.getLocation().toVector().toString()
            ));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFrameDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof ItemFrame frame && isPortraitFrame(frame)) {
            event.setCancelled(true);
            // No logging needed for damage attempts to reduce console spam
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        if (event.getEntity() instanceof ItemFrame frame && isPortraitFrame(frame)) {
            event.setCancelled(true);

            if (event instanceof HangingBreakByEntityEvent breakEvent && breakEvent.getRemover() instanceof Player player) {
                // Only log if it's a player trying to break it
                plugin.log(Level.INFO, "Player " + player.getName() + " attempted to break a locked portrait.");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemFrameItemManipulation(HangingPlaceEvent event) {
        if (event.getEntity() instanceof ItemFrame frame && isPortraitFrame(frame)) {
            event.setCancelled(true);
        }
    }
}