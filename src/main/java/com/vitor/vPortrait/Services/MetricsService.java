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

import com.vitor.vPortrait.vPortrait;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;

/**
 * Service to handle bStats metrics integration.
 */
public class MetricsService {

    private final vPortrait plugin;
    private final int BSTATS_ID = 28973;

    public MetricsService(vPortrait plugin) {
        this.plugin = plugin;
    }

    /**
     * Initializes the bStats metrics and adds custom charts.
     */
    public void setupMetrics() {
        Metrics metrics = new Metrics(plugin, BSTATS_ID);

        // Custom chart to track how many portraits are currently active on the server
        metrics.addCustomChart(new SimplePie("total_portraits", () -> {
            int count = plugin.getPortraitManager().getPortraitCount();
            return String.valueOf(count);
        }));

        plugin.log(java.util.logging.Level.INFO, "bStats metrics initialized successfully.");
    }
}