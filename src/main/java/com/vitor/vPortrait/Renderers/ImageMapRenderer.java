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

package com.vitor.vPortrait.Renderers;

import com.vitor.vPortrait.vPortrait;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class ImageMapRenderer extends MapRenderer {
    private final vPortrait plugin;
    private final BufferedImage image;
    private final int mapId;
    private final Map<UUID, Long> lastRenderTime = new HashMap<>();
    private static final long RENDER_COOLDOWN = 10000; // 10 seconds

    public ImageMapRenderer(vPortrait plugin, BufferedImage image, int mapId) {
        super(true);
        this.plugin = plugin;
        this.image = image;
        this.mapId = mapId;
    }

    @Override
    public void render(@NotNull MapView map, @NotNull MapCanvas canvas, @NotNull Player player) {
        UUID playerId = player.getUniqueId();
        long now = System.currentTimeMillis();

        if (lastRenderTime.containsKey(playerId)) {
            if (now - lastRenderTime.get(playerId) < RENDER_COOLDOWN) {
                return;
            }
        }

        if (image != null) {
            try {
                canvas.drawImage(0, 0, image);
                lastRenderTime.put(playerId, now);

                player.sendMap(map);
                plugin.log(Level.FINE, String.format("Map ID %d rendered for %s", mapId, player.getName()));
            } catch (Exception e) {
                plugin.log(Level.SEVERE, "Failed to render map " + mapId + ": " + e.getMessage());
            }
        }
    }

    public void forceRenderForPlayer(Player player) {
        lastRenderTime.remove(player.getUniqueId());
    }

    public void clearCache() {
        lastRenderTime.clear();
    }
}