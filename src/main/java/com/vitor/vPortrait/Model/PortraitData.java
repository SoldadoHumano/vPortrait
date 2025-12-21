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

package com.vitor.vPortrait.Model;

import org.bukkit.block.BlockFace;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object for JSON persistence.
 */
public class PortraitData {
    private final String id;
    private final String worldName;
    private final int x1, y1, z1;
    private final int x2, y2, z2;
    private final String imageUrl;
    private final String blockFaceName;
    private List<Integer> mapIds;
    private List<UUID> entityUuids; // Stores the physical entities (GlowItemFrame)
    private final long createdAt;

    public PortraitData(String worldName, int x1, int y1, int z1, int x2, int y2, int z2, String imageUrl, BlockFace facing) {
        this.id = UUID.randomUUID().toString();
        this.worldName = worldName;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
        this.imageUrl = imageUrl;
        this.blockFaceName = facing.name();
        this.mapIds = new ArrayList<>();
        this.entityUuids = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * Lazy initialization for Gson to prevent NullPointerExceptions
     * when deserializing collections.
     */
    public List<Integer> getMapIds() {
        if (this.mapIds == null) this.mapIds = new ArrayList<>();
        return mapIds;
    }

    public List<UUID> getEntityUuids() {
        if (this.entityUuids == null) this.entityUuids = new ArrayList<>();
        return entityUuids;
    }

    public void addMapId(int id) {
        getMapIds().add(id);
    }

    public void addEntityUuid(UUID uuid) {
        getEntityUuids().add(uuid);
    }

    public String getId() { return id; }
    public String getWorldName() { return worldName; }
    public int getX1() { return x1; }
    public int getY1() { return y1; }
    public int getZ1() { return z1; }
    public int getX2() { return x2; }
    public int getY2() { return y2; }
    public int getZ2() { return z2; }
    public String getImageUrl() { return imageUrl; }
    public BlockFace getFacing() { return BlockFace.valueOf(blockFaceName); }
    public long getCreatedAt() { return createdAt; }
}