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

package com.vitor.vPortrait;

import com.vitor.vPortrait.Commands.PortraitCommand;
import com.vitor.vPortrait.Listeners.FrameProtectionListener;
import com.vitor.vPortrait.Listeners.PlayerJoinListener;
import com.vitor.vPortrait.Listeners.SelectionListener;
import com.vitor.vPortrait.Manager.PortraitManager;
import com.vitor.vPortrait.Manager.SelectionManager;
import com.vitor.vPortrait.Services.MetricsService;
import com.vitor.vPortrait.Services.PortraitCleanupService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class vPortrait extends JavaPlugin {
    private static vPortrait instance;
    private SelectionManager selectionManager;
    private PortraitManager portraitManager;

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        instance = this;

        getLogger().info("Initializing Plugin...\n\n");
        getLogger().info("=================================");
        getLogger().info("Developer: vitor1227_op (Discord)");
        getLogger().info("Developer: SoldadoHumano (GitHub)");
        getLogger().info("GitHub: https://github.com/SoldadoHumano");
        getLogger().info("Project GitHub Page: https://github.com/SoldadoHumano/vPortrait");
        String version = this.getDescription().getVersion();
        getLogger().info("Version: " + version);
        getLogger().info("Java: " + System.getProperty("java.version"));
        getLogger().info("OS: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ")");
        getLogger().info("=================================\n\n");

        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            log(Level.SEVERE, "Could not create data folder!");
            return;
        }

        this.selectionManager = new SelectionManager();
        this.portraitManager = new PortraitManager(this);

        MetricsService metricsService = new MetricsService(this);
        metricsService.setupMetrics();

        try {
            getCommand("vportrait").setExecutor(new PortraitCommand(this));
            getServer().getPluginManager().registerEvents(new SelectionListener(this), this);
            getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
            getServer().getPluginManager().registerEvents(new FrameProtectionListener(this), this);

            log(Level.INFO, "Commands and listeners registered successfully.");
        } catch (Exception e) {
            log(Level.SEVERE, "Registration error: " + e.getMessage());
            e.printStackTrace();
        }

        // Delay loading to ensure worlds are fully initialized
        // This is where the magic happens: we clean BEFORE we load.
        Bukkit.getScheduler().runTaskLater(this, () -> {
            // Step 1: Absolute Cleanup
            log(Level.INFO, "Executing startup cleanup sequence...");
            PortraitCleanupService cleanupService = new PortraitCleanupService(this);
            cleanupService.cleanupOldPortraits();

            // Step 2: Load and Respawn
            log(Level.INFO, "Loading portraits...");
            this.portraitManager.loadPortraits();

            log(Level.INFO, "vPortrait enabled in " + (System.currentTimeMillis() - startTime) + "ms");
        }, 60L);
    }

    @Override
    public void onDisable() {
        if (portraitManager != null) {
            portraitManager.savePortraits();
            log(Level.INFO, "Portraits saved successfully.");
        }

        log(Level.INFO, "vPortrait disabled.");
    }

    public void log(Level level, String message) {
        getLogger().log(level, message);
    }

    public static vPortrait getInstance() { return instance; }
    public SelectionManager getSelectionManager() { return selectionManager; }
    public PortraitManager getPortraitManager() { return portraitManager; }
}