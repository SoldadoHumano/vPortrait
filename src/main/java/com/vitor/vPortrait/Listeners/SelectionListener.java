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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SelectionListener implements Listener {

    private final vPortrait plugin;

    public SelectionListener(vPortrait plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.GOLDEN_AXE) return;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

        // Simple check for our wand name
        String name = ((net.kyori.adventure.text.TextComponent) item.getItemMeta().displayName()).content();
        if (!name.contains("Portrait Wand")) return;

        event.setCancelled(true); // Don't strip logs or break blocks

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            plugin.getSelectionManager().setPos1(event.getPlayer(), event.getClickedBlock().getLocation());
            event.getPlayer().sendMessage(Component.text("Position 1 set.", NamedTextColor.LIGHT_PURPLE));
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            plugin.getSelectionManager().setPos2(event.getPlayer(), event.getClickedBlock().getLocation());
            event.getPlayer().sendMessage(Component.text("Position 2 set.", NamedTextColor.LIGHT_PURPLE));
        }
    }
}