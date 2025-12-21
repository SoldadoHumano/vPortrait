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

package com.vitor.vPortrait.Commands;

import com.vitor.vPortrait.vPortrait;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class PortraitCommand implements CommandExecutor {
    private final vPortrait plugin;

    // Prefix padronizado para manter a fluidez visual no chat
    private final Component prefix = Component.text("[vPortrait] ", NamedTextColor.AQUA);

    public PortraitCommand(vPortrait plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can execute this command.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "tool" -> {
                if (!player.hasPermission("vportrait.use")) {
                    sendNoPermission(player);
                    return true;
                }
                giveTool(player);
                player.sendMessage(prefix.append(Component.text("Selection tool received.", NamedTextColor.GREEN)));
            }

            case "upload" -> {
                if (!player.hasPermission("vportrait.use")) {
                    sendNoPermission(player);
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(prefix.append(Component.text("Usage: /vportrait upload <url>", NamedTextColor.RED)));
                    return true;
                }
                if (!plugin.getSelectionManager().hasValidSelection(player)) {
                    player.sendMessage(prefix.append(Component.text("Invalid selection! It must be 1 block thick.", NamedTextColor.RED)));
                    return true;
                }

                String url = args[1];
                BlockFace facing = plugin.getSelectionManager().calculateFacing(player);
                player.sendMessage(prefix.append(Component.text("Processing image...", NamedTextColor.YELLOW)));
                plugin.getPortraitManager().createPortrait(player, url, facing);
            }

            case "remove" -> {
                if (!player.hasPermission("vportrait.use")) {
                    sendNoPermission(player);
                    return true;
                }
                boolean removed = plugin.getPortraitManager().removePortraitAtCursor(player);
                if (removed) {
                    player.sendMessage(prefix.append(Component.text("Portrait removed successfully.", NamedTextColor.GREEN)));
                } else {
                    player.sendMessage(prefix.append(Component.text("No portrait found at your crosshair.", NamedTextColor.RED)));
                }
            }

            case "reload" -> {
                if (!player.hasPermission("vportrait.admin")) {
                    sendNoPermission(player);
                    return true;
                }
                player.sendMessage(prefix.append(Component.text("Reloading portraits...", NamedTextColor.YELLOW)));
                plugin.getPortraitManager().loadPortraits();
                player.sendMessage(prefix.append(Component.text("Portraits reloaded from storage.", NamedTextColor.GREEN)));
            }

            case "sync" -> {
                if (!player.hasPermission("vportrait.sync")) {
                    sendNoPermission(player);
                    return true;
                }
                player.sendMessage(prefix.append(Component.text("Synchronizing portraits...", NamedTextColor.YELLOW)));
                plugin.getPortraitManager().syncAllPortraitsToPlayer(player);
                player.sendMessage(prefix.append(Component.text("Synchronization complete!", NamedTextColor.GREEN)));
            }

            case "list" -> {
                if (!player.hasPermission("vportrait.admin")) {
                    sendNoPermission(player);
                    return true;
                }
                player.sendMessage(prefix.append(Component.text("Currently loaded portraits: ", NamedTextColor.AQUA))
                        .append(Component.text(plugin.getPortraitManager().getPortraitCount(), NamedTextColor.WHITE)));
            }

            default -> sendUsage(player);
        }

        return true;
    }

    private void sendUsage(Player player) {
        player.sendMessage(Component.text("--- ", NamedTextColor.GRAY).append(prefix).append(Component.text(" ---", NamedTextColor.GRAY)));
        player.sendMessage(createUsageLine("/vportrait tool", "Receive the selection wand"));
        player.sendMessage(createUsageLine("/vportrait upload <url>", "Create a new portrait"));
        player.sendMessage(createUsageLine("/vportrait remove", "Remove the targeted portrait"));
        player.sendMessage(createUsageLine("/vportrait sync", "Force sync images for yourself"));
        player.sendMessage(createUsageLine("/vportrait reload", "Reload portraits from config (Admin)"));
        player.sendMessage(createUsageLine("/vportrait list", "Show count of active portraits (Admin)"));
    }

    private Component createUsageLine(String cmd, String desc) {
        return Component.text("» ", NamedTextColor.AQUA)
                .append(Component.text(cmd, NamedTextColor.YELLOW))
                .append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text(desc, NamedTextColor.WHITE));
    }

    private void sendNoPermission(Player player) {
        player.sendMessage(prefix.append(Component.text("You don't have permission to do this.", NamedTextColor.RED)));
    }

    private void giveTool(Player player) {
        ItemStack wand = new ItemStack(Material.GOLDEN_AXE);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Portrait Wand", NamedTextColor.GOLD, TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("Left Click: ", NamedTextColor.YELLOW).append(Component.text("Set Pos 1", NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false),
                    Component.text("Right Click: ", NamedTextColor.YELLOW).append(Component.text("Set Pos 2", NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false)
            ));
            wand.setItemMeta(meta);
        }
        player.getInventory().addItem(wand);
    }
}