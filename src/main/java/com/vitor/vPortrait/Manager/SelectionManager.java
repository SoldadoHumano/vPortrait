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

package com.vitor.vPortrait.Manager;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SelectionManager {
    private final Map<UUID, Location> pos1Map = new HashMap<>();
    private final Map<UUID, Location> pos2Map = new HashMap<>();

    public void setPos1(Player player, Location loc) {
        pos1Map.put(player.getUniqueId(), loc);
    }

    public void setPos2(Player player, Location loc) {
        pos2Map.put(player.getUniqueId(), loc);
    }

    public Location getPos1(Player player) {
        return pos1Map.get(player.getUniqueId());
    }

    public Location getPos2(Player player) {
        return pos2Map.get(player.getUniqueId());
    }

    public boolean hasValidSelection(Player player) {
        Location p1 = pos1Map.get(player.getUniqueId());
        Location p2 = pos2Map.get(player.getUniqueId());

        if (p1 == null || p2 == null) return false;
        if (!p1.getWorld().equals(p2.getWorld())) return false;

        // Calculate dimensions
        int dx = Math.abs(p1.getBlockX() - p2.getBlockX()) + 1;
        int dy = Math.abs(p1.getBlockY() - p2.getBlockY()) + 1;
        int dz = Math.abs(p1.getBlockZ() - p2.getBlockZ()) + 1;

        // The selection must be a flat wall (1 block thick in either X or Z)
        boolean isWall = (dx == 1 && dz > 1) || (dz == 1 && dx > 1);
        boolean isValidHeight = dy >= 1 && dy <= 50;

        return isWall && isValidHeight;
    }

    /**
     * Calculates the BlockFace for item frame placement based on the selection plane
     * and the player's current orientation.
     */
    public BlockFace calculateFacing(Player player) {
        Location p1 = getPos1(player);
        Location p2 = getPos2(player);

        if (p1 == null || p2 == null) {
            return BlockFace.NORTH;
        }

        boolean isXWall = p1.getBlockX() == p2.getBlockX(); // Constant X
        boolean isZWall = p1.getBlockZ() == p2.getBlockZ(); // Constant Z

        float yaw = player.getLocation().getYaw();

        // Normalize yaw to 0-360
        while (yaw < 0) yaw += 360;
        while (yaw >= 360) yaw -= 360;

        if (isXWall) {
            // Wall on the X axis: frame faces EAST or WEST
            if (yaw >= 45 && yaw < 135) {
                return BlockFace.WEST;
            } else if (yaw >= 135 && yaw < 225) {
                return (p1.getX() < player.getLocation().getX()) ? BlockFace.WEST : BlockFace.EAST;
            } else if (yaw >= 225 && yaw < 315) {
                return BlockFace.EAST;
            } else {
                return (p1.getX() < player.getLocation().getX()) ? BlockFace.WEST : BlockFace.EAST;
            }
        } else if (isZWall) {
            // Wall on the Z axis: frame faces NORTH or SOUTH
            if (yaw >= 45 && yaw < 135) {
                return (p1.getZ() < player.getLocation().getZ()) ? BlockFace.NORTH : BlockFace.SOUTH;
            } else if (yaw >= 135 && yaw < 225) {
                return BlockFace.NORTH;
            } else if (yaw >= 225 && yaw < 315) {
                return (p1.getZ() < player.getLocation().getZ()) ? BlockFace.NORTH : BlockFace.SOUTH;
            } else {
                return BlockFace.SOUTH;
            }
        }

        // Fallback based on player orientation
        if (yaw >= 45 && yaw < 135) {
            return BlockFace.EAST;
        } else if (yaw >= 135 && yaw < 225) {
            return BlockFace.SOUTH;
        } else if (yaw >= 225 && yaw < 315) {
            return BlockFace.WEST;
        } else {
            return BlockFace.NORTH;
        }
    }
}