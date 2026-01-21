/*
 * Copyright (c) 2026 Vitor (SoldadoHumano)
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

package com.vitor.vPortrait.Services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.vitor.vPortrait.Model.PortraitData;
import com.vitor.vPortrait.vPortrait;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.GlowItemFrame;
import org.bukkit.entity.ItemFrame;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

/**
 * Service dedicated to the absolute removal of old portrait entities.
 * This ensures no "ghost" entities stack up, causing client-side FPS drops.
 */
public class PortraitCleanupService {

    private final vPortrait plugin;
    private final File dataFile;
    private final Gson gson;

    public PortraitCleanupService(vPortrait plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "portraits.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Executes the spatial cleanup.
     * This method ignores UUIDs and looks for physical entities at the target locations.
     */
    public void cleanupOldPortraits() {
        if (!dataFile.exists()) return;

        plugin.log(Level.INFO, "Starting spatial cleanup of portrait entities...");

        List<PortraitData> portraits = loadPortraitData();
        int removedCount = 0;

        for (PortraitData data : portraits) {
            World world = Bukkit.getWorld(data.getWorldName());

            // If the world is not loaded, we cannot clean up entities.
            // In a production server, ensuring the world is loaded is crucial.
            if (world == null) {
                plugin.log(Level.WARNING, "Skipping cleanup for world '" + data.getWorldName() + "' (Not Loaded).");
                continue;
            }

            // Calculate the bounds of the portrait
            BlockFace facing = data.getFacing();
            int width = getSelectionWidthFromData(data, facing);
            int height = data.getY2() - data.getY1() + 1;

            // Iterate through every single block position expected to have a frame
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    Location targetLoc = calculateBlockLocation(world, data, facing, col, row);

                    // Adjust to the center of the block where the entity effectively resides
                    Location entityCheckLoc = adjustForItemFrame(targetLoc, facing).add(0.5, 0.5, 0.5);

                    // Ensure the chunk is loaded to prevent async chunk load errors or missed entities
                    Chunk chunk = entityCheckLoc.getChunk();
                    if (!chunk.isLoaded()) {
                        chunk.load();
                    }

                    // Scans a small radius (0.8) around the center of the block.
                    // ItemFrames are entities, so getNearbyEntities is the most efficient way.
                    Collection<Entity> nearbyEntities = world.getNearbyEntities(entityCheckLoc, 0.8, 0.8, 0.8);

                    for (Entity entity : nearbyEntities) {
                        if (entity instanceof ItemFrame || entity instanceof GlowItemFrame) {
                            // SECURITY: We assume ANY frame at this exact registered coordinate 
                            // is part of the plugin logic and must be purged to prevent stacking.
                            entity.remove();
                            removedCount++;
                        }
                    }
                }
            }
        }

        plugin.log(Level.INFO, "Spatial cleanup finished. Removed " + removedCount + " entities that were occupying portrait slots.");
    }

    /**
     * Loads the raw data directly from JSON to ensure we have the persistent state,
     * not just what is currently in memory.
     */
    private List<PortraitData> loadPortraitData() {
        try (Reader reader = new FileReader(dataFile)) {
            List<PortraitData> loaded = gson.fromJson(reader, new TypeToken<List<PortraitData>>(){}.getType());
            return loaded != null ? loaded : new ArrayList<>();
        } catch (IOException e) {
            plugin.log(Level.SEVERE, "Failed to read portraits for cleanup: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // --- Helper Logic Duplicated for Independence (Safety) ---

    private int getSelectionWidthFromData(PortraitData data, BlockFace facing) {
        if (facing == BlockFace.NORTH || facing == BlockFace.SOUTH) {
            return Math.abs(data.getX2() - data.getX1()) + 1;
        } else {
            return Math.abs(data.getZ2() - data.getZ1()) + 1;
        }
    }

    private Location calculateBlockLocation(World world, PortraitData data, BlockFace facing, int col, int row) {
        int minX = Math.min(data.getX1(), data.getX2());
        int maxX = Math.max(data.getX1(), data.getX2());
        int minZ = Math.min(data.getZ1(), data.getZ2());
        int maxZ = Math.max(data.getZ1(), data.getZ2());

        // Default anchor points
        int x = data.getX1();
        int y = data.getY2() - row; // Top to bottom
        int z = data.getZ1();

        // Logic must match PortraitManager exactly to find the correct blocks
        switch (facing) {
            case NORTH:
                x = maxX - col;
                z = minZ;
                break;
            case SOUTH:
                x = minX + col;
                z = maxZ;
                break;
            case WEST:
                x = minX;
                z = minZ + col;
                break;
            case EAST:
                x = maxX;
                z = maxZ - col;
                break;
        }
        return new Location(world, x, y, z);
    }

    private Location adjustForItemFrame(Location loc, BlockFace face) {
        return switch (face) {
            case NORTH -> loc.clone().add(0, 0, 1);
            case SOUTH -> loc.clone().add(0, 0, -1);
            case WEST  -> loc.clone().add(1, 0, 0);
            case EAST  -> loc.clone().add(-1, 0, 0);
            default -> loc.clone();
        };
    }
}