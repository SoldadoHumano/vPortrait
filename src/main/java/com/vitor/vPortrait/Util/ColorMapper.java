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

package com.vitor.vPortrait.Util;

import org.bukkit.Material;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

// Utility class to map RGB colors to Minecraft blocks
public class ColorMapper {

    private static final Map<Color, Material> PALETTE = new HashMap<>();

    static {
        PALETTE.put(new Color(127, 178, 56), Material.GRASS_BLOCK);
        PALETTE.put(new Color(247, 233, 163), Material.SAND);
        PALETTE.put(new Color(199, 199, 199), Material.WHITE_WOOL);
        PALETTE.put(new Color(255, 0, 0), Material.TNT);
        PALETTE.put(new Color(160, 160, 255), Material.ICE);
        PALETTE.put(new Color(167, 167, 167), Material.IRON_BLOCK);
        PALETTE.put(new Color(0, 124, 0), Material.OAK_LEAVES);
        PALETTE.put(new Color(255, 255, 255), Material.SNOW_BLOCK);
        PALETTE.put(new Color(164, 168, 184), Material.CLAY);
        PALETTE.put(new Color(151, 109, 77), Material.DIRT);
        PALETTE.put(new Color(112, 112, 112), Material.STONE);
        PALETTE.put(new Color(64, 64, 255), Material.WATER);
        PALETTE.put(new Color(143, 119, 72), Material.OAK_PLANKS);
        PALETTE.put(new Color(255, 252, 245), Material.QUARTZ_BLOCK);
        PALETTE.put(new Color(216, 127, 51), Material.ORANGE_CONCRETE);
        PALETTE.put(new Color(178, 76, 216), Material.MAGENTA_CONCRETE);
        PALETTE.put(new Color(102, 153, 216), Material.LIGHT_BLUE_CONCRETE);
        PALETTE.put(new Color(229, 229, 51), Material.YELLOW_CONCRETE);
        PALETTE.put(new Color(127, 204, 25), Material.LIME_CONCRETE);
        PALETTE.put(new Color(242, 127, 165), Material.PINK_CONCRETE);
        PALETTE.put(new Color(76, 76, 76), Material.GRAY_CONCRETE);
        PALETTE.put(new Color(153, 153, 153), Material.LIGHT_GRAY_CONCRETE);
        PALETTE.put(new Color(76, 127, 153), Material.CYAN_CONCRETE);
        PALETTE.put(new Color(127, 63, 178), Material.PURPLE_CONCRETE);
        PALETTE.put(new Color(51, 76, 178), Material.BLUE_CONCRETE);
        PALETTE.put(new Color(102, 76, 51), Material.BROWN_CONCRETE);
        PALETTE.put(new Color(102, 127, 51), Material.GREEN_CONCRETE);
        PALETTE.put(new Color(153, 51, 51), Material.RED_CONCRETE);
        PALETTE.put(new Color(25, 25, 25), Material.BLACK_CONCRETE);
        PALETTE.put(new Color(250, 238, 77), Material.GOLD_BLOCK);
        PALETTE.put(new Color(92, 219, 213), Material.DIAMOND_BLOCK);
        PALETTE.put(new Color(74, 128, 255), Material.LAPIS_BLOCK);
        PALETTE.put(new Color(0, 217, 58), Material.EMERALD_BLOCK);
        PALETTE.put(new Color(129, 86, 49), Material.PODZOL);
        PALETTE.put(new Color(112, 2, 0), Material.NETHERRACK);
        PALETTE.put(new Color(209, 177, 161), Material.WHITE_TERRACOTTA);
        PALETTE.put(new Color(159, 82, 36), Material.ORANGE_TERRACOTTA);
        PALETTE.put(new Color(149, 87, 108), Material.MAGENTA_TERRACOTTA);
        PALETTE.put(new Color(112, 108, 138), Material.LIGHT_BLUE_TERRACOTTA);
        PALETTE.put(new Color(186, 133, 36), Material.YELLOW_TERRACOTTA);
        PALETTE.put(new Color(103, 117, 53), Material.LIME_TERRACOTTA);
        PALETTE.put(new Color(160, 77, 78), Material.PINK_TERRACOTTA);
        PALETTE.put(new Color(57, 41, 35), Material.GRAY_TERRACOTTA);
        PALETTE.put(new Color(135, 107, 98), Material.LIGHT_GRAY_TERRACOTTA);
        PALETTE.put(new Color(87, 92, 92), Material.CYAN_TERRACOTTA);
        PALETTE.put(new Color(122, 73, 88), Material.PURPLE_TERRACOTTA);
        PALETTE.put(new Color(76, 62, 92), Material.BLUE_TERRACOTTA);
        PALETTE.put(new Color(76, 50, 35), Material.BROWN_TERRACOTTA);
        PALETTE.put(new Color(76, 82, 42), Material.GREEN_TERRACOTTA);
        PALETTE.put(new Color(142, 60, 46), Material.RED_TERRACOTTA);
        PALETTE.put(new Color(37, 22, 16), Material.BLACK_TERRACOTTA);
        PALETTE.put(new Color(189, 48, 49), Material.CRIMSON_NYLIUM);
        PALETTE.put(new Color(148, 63, 97), Material.CRIMSON_STEM);
        PALETTE.put(new Color(92, 25, 29), Material.CRIMSON_HYPHAE);
        PALETTE.put(new Color(22, 126, 134), Material.WARPED_NYLIUM);
        PALETTE.put(new Color(58, 142, 140), Material.WARPED_STEM);
        PALETTE.put(new Color(86, 44, 62), Material.WARPED_HYPHAE);
        PALETTE.put(new Color(20, 180, 133), Material.WARPED_WART_BLOCK);
        PALETTE.put(new Color(100, 100, 100), Material.DEEPSLATE);
        PALETTE.put(new Color(216, 175, 147), Material.RAW_IRON_BLOCK);
        PALETTE.put(new Color(127, 167, 150), Material.GLOW_LICHEN);
    }
}