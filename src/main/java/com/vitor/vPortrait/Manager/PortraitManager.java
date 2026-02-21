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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.vitor.vPortrait.Listeners.FrameProtectionListener;
import com.vitor.vPortrait.Model.PortraitData;
import com.vitor.vPortrait.Renderers.ImageMapRenderer;
import com.vitor.vPortrait.vPortrait;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.GlowItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.util.RayTraceResult;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;


public class PortraitManager {
    private final vPortrait plugin;
    private final List<PortraitData> portraits = new ArrayList<>();
    private final File dataFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<Integer, ImageMapRenderer> rendererCache = new HashMap<>();
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 5000;
    private static final int MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5 MB


    public PortraitManager(vPortrait plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "portraits.json");
    }

    public int getPortraitCount() {
        return portraits.size();
    }

    // Creates a new portrait based on the player's selection
    public void createPortrait(Player player, String imageUrl, BlockFace facing) {
        if (!plugin.getSelectionManager().hasValidSelection(player)) {
            player.sendMessage(Component.text("Invalid selection! It must be a flat vertical area.").color(NamedTextColor.RED));
            return;
        }

        // Invert the facing so that the portrait "looks" back at the player
        BlockFace portraitFacing = facing.getOppositeFace();

        Location p1 = plugin.getSelectionManager().getPos1(player);
        Location p2 = plugin.getSelectionManager().getPos2(player);

        CompletableFuture.runAsync(() -> {
            try {
                BufferedImage fullImage = downloadImage(imageUrl);

                int minX = Math.min(p1.getBlockX(), p2.getBlockX());
                int minY = Math.min(p1.getBlockY(), p2.getBlockY());
                int minZ = Math.min(p1.getBlockZ(), p2.getBlockZ());
                int maxX = Math.max(p1.getBlockX(), p2.getBlockX());
                int maxY = Math.max(p1.getBlockY(), p2.getBlockY());
                int maxZ = Math.max(p1.getBlockZ(), p2.getBlockZ());

                int width = getSelectionWidth(p1, p2, portraitFacing);
                int height = maxY - minY + 1;

                BufferedImage resized = resizeImage(fullImage, width * 128, height * 128);

                PortraitData data = new PortraitData(p1.getWorld().getName(),
                        minX, minY, minZ, maxX, maxY, maxZ, imageUrl, portraitFacing);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    spawnFramesAndApplyMaps(data, resized, portraitFacing);
                    portraits.add(data);
                    savePortraits();

                    player.sendMessage(Component.text("Portrait created").color(NamedTextColor.GREEN));
                });

            } catch (Exception e) {
                plugin.log(Level.SEVERE, "Failed to create portrait: " + e.getMessage());
                player.sendMessage(Component.text("Error processing image: " + e.getMessage()).color(NamedTextColor.RED));
            }
        });
    }

    private int getSelectionWidth(Location p1, Location p2, BlockFace facing) {
        if (facing == BlockFace.NORTH || facing == BlockFace.SOUTH) {
            return Math.abs(p1.getBlockX() - p2.getBlockX()) + 1;
        } else {
            return Math.abs(p1.getBlockZ() - p2.getBlockZ()) + 1;
        }
    }

    private void spawnFramesAndApplyMaps(PortraitData data, BufferedImage image, BlockFace facing) {
        World world = Bukkit.getWorld(data.getWorldName());
        if (world == null) return;

        int width = getSelectionWidthFromData(data, facing);
        int height = data.getY2() - data.getY1() + 1;

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                try {
                    Location blockLocation = calculateBlockLocation(data, facing, col, row);

                    world.getNearbyEntities(blockLocation, 0.5, 0.5, 0.5).forEach(entity -> {
                        if (entity instanceof GlowItemFrame) entity.remove();
                    });

                    BufferedImage slice = image.getSubimage(col * 128, row * 128, 128, 128);

                    MapView view = Bukkit.createMap(world);
                    view.getRenderers().forEach(view::removeRenderer);

                    ImageMapRenderer renderer = new ImageMapRenderer(plugin, slice, view.getId());
                    view.addRenderer(renderer);
                    rendererCache.put(view.getId(), renderer);

                    ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
                    MapMeta meta = (MapMeta) mapItem.getItemMeta();
                    meta.setMapView(view);
                    mapItem.setItemMeta(meta);

                    Location frameLocation = adjustForItemFrame(blockLocation, facing)
                            .add(0.5, 0.5, 0.5);

                    GlowItemFrame frame = world.spawn(frameLocation, GlowItemFrame.class, f -> {
                        f.setFacingDirection(facing, true);
                        f.setItem(mapItem);
                        f.setVisible(false);
                        f.setFixed(true);
                        f.setPersistent(true);
                    });

                    FrameProtectionListener.markAsPortraitFrame(frame, plugin);
                    data.addMapId(view.getId());
                    data.addEntityUuid(frame.getUniqueId());

                } catch (Exception e) {
                    plugin.log(Level.SEVERE, "Error in the frame " + col + "," + row + ": " + e.getMessage());
                }
            }
        }
        syncMapsToAllPlayers(data);
    }

    /*
     Calculates the block's position by inverting the axes according to the face
     so that the image is not mirrored or has swapped columns.
    */
    private Location calculateBlockLocation(PortraitData data, BlockFace facing, int col, int row) {
        World world = Bukkit.getWorld(data.getWorldName());
        int minX = Math.min(data.getX1(), data.getX2());
        int maxX = Math.max(data.getX1(), data.getX2());
        int minZ = Math.min(data.getZ1(), data.getZ2());
        int maxZ = Math.max(data.getZ1(), data.getZ2());
        int x = data.getX1();
        int y = data.getY2() - row; // always from top to bottom
        int z = data.getZ1();

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

    private int getSelectionWidthFromData(PortraitData data, BlockFace facing) {
        if (facing == BlockFace.NORTH || facing == BlockFace.SOUTH) {
            return Math.abs(data.getX2() - data.getX1()) + 1;
        } else {
            return Math.abs(data.getZ2() - data.getZ1()) + 1;
        }
    }

    public boolean removePortraitAtCursor(Player player) {
        RayTraceResult entityResult = player.rayTraceEntities(5);
        if (entityResult != null && entityResult.getHitEntity() instanceof GlowItemFrame frame) {
            if (FrameProtectionListener.isPortraitFrame(frame)) {
                return removePortraitByFrame(frame);
            }
        }
        RayTraceResult blockResult = player.rayTraceBlocks(5.0);
        if (blockResult == null || blockResult.getHitBlock() == null) return false;

        Location hit = blockResult.getHitBlock().getLocation();

        for (Entity nearby : hit.getWorld().getNearbyEntities(hit.add(0.5, 0.5, 0.5), 1.6, 1.6, 1.6)) {
            if (nearby instanceof GlowItemFrame frame) {
                if (FrameProtectionListener.isPortraitFrame(frame)) {
                    return removePortraitByFrame(frame);
                }
            }
        }

        return false;
    }

    public boolean removePortraitByFrame(GlowItemFrame frame) {
        Iterator<PortraitData> it = portraits.iterator();
        while (it.hasNext()) {
            PortraitData data = it.next();
            if (data.getEntityUuids().contains(frame.getUniqueId())) {
                for (UUID uuid : data.getEntityUuids()) {
                    Entity e = Bukkit.getEntity(uuid);
                    if (e != null) e.remove();
                }
                for (int mapId : data.getMapIds()) rendererCache.remove(mapId);
                it.remove();
                savePortraits();
                return true;
            }
        }
        return false;
    }

    public void loadPortraits() {
        if (!dataFile.exists()) return;
        try (Reader reader = new FileReader(dataFile)) {
            List<PortraitData> loaded = gson.fromJson(reader, new TypeToken<List<PortraitData>>(){}.getType());
            if (loaded != null) {
                this.portraits.clear();
                this.portraits.addAll(loaded);
                for (PortraitData data : portraits) respawnPortrait(data);
            }
        } catch (IOException e) {
            plugin.log(Level.SEVERE, "Loading error: " + e.getMessage());
        }
    }

    private void respawnPortrait(PortraitData data) {
        for (UUID uuid : data.getEntityUuids()) {
            Entity e = Bukkit.getEntity(uuid);
            if (e != null) e.remove();
        }
        data.getEntityUuids().clear();
        data.getMapIds().clear();

        CompletableFuture.runAsync(() -> {
            try {
                BufferedImage fullImage = downloadImage(data.getImageUrl());
                BlockFace facing = data.getFacing();
                int width = getSelectionWidthFromData(data, facing);
                int height = data.getY2() - data.getY1() + 1;
                BufferedImage resized = resizeImage(fullImage, width * 128, height * 128);
                Bukkit.getScheduler().runTask(plugin, () -> spawnFramesAndApplyMaps(data, resized, facing));
            } catch (Exception e) {
                plugin.log(Level.SEVERE, "Error respawning: " + e.getMessage());
            }
        });
    }

    public void savePortraits() {
        try (Writer writer = new FileWriter(dataFile)) {
            gson.toJson(portraits, writer);
        } catch (IOException e) {
            plugin.log(Level.SEVERE, "Error saving portraits: " + e);
        }
    }

    public void despawnAllPortraits() {
        for (PortraitData data : portraits) {
            for (UUID uuid : data.getEntityUuids()) {
                Entity e = Bukkit.getEntity(uuid);
                if (e != null) e.remove();
            }
        }
        rendererCache.clear();
    }

    private void syncMapsToAllPlayers(PortraitData data) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) syncMapsToPlayer(onlinePlayer, data);
        }, 10L);
    }

    public void syncMapsToPlayer(Player player, PortraitData data) {
        World world = Bukkit.getWorld(data.getWorldName());
        if (world == null || !world.equals(player.getWorld())) return;
        for (int mapId : data.getMapIds()) {
            MapView view = Bukkit.getMap(mapId);
            if (view != null) player.sendMap(view);
        }
    }

    public void syncAllPortraitsToPlayer(Player player) {
        for (PortraitData data : portraits) {
            if (data.getWorldName().equals(player.getWorld().getName())) syncMapsToPlayer(player, data);
        }
    }

    /**
     * Downloads an image from a URL.
     * * <p>This implementation avoids the deprecated {@link URL} constructor by using
     * {@link URI#toURL()} as recommended since Java 20. It also includes
     * connection timeouts to ensure application stability.</p>
     *
     * @param urlString The string representation of the image location.
     * @return The processed {@link BufferedImage}.
     * @throws IOException If a network error occurs or the stream is not a valid image.
     */
    private BufferedImage downloadImage(String urlString) throws IOException {
        URI uri = URI.create(urlString);
        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            throw new IOException("Only HTTPS URLs are allowed");
        }

        String host = uri.getHost();
        if (host == null) {
            throw new IOException("Invalid URL: missing host.");
        }

        InetAddress address = InetAddress.getByName(host);
        if (isPrivateAddress(address)) {
            throw new IOException("Private or local address are not allowed.");
        }

        URL url = uri.toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setInstanceFollowRedirects(false);
        conn.setRequestProperty("User-Agent", "vPortrait/1.2.0");
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to download image. HTTP Code: " + responseCode);
        }

        int contentLength = conn.getContentLength();
        if (contentLength < 0 || contentLength > MAX_IMAGE_SIZE) {
            throw new IOException("Image too large or invalid size.");
        }

        String contentType = conn.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("URL does not point to a valid image.");
        }

        InetAddress connectAddress = InetAddress.getByName(conn.getURL().getHost());
        if (isPrivateAddress(connectAddress)) {
            throw new IOException("Redirected to private address. Blocked.");
        }

        try (InputStream in = conn.getInputStream()) {
            InputStream limitedStream = new BufferedInputStream(in) {
                private int totalRead = 0;

                @Override
                public synchronized int read(byte[] b, int off, int len) throws IOException {
                    int bytesRead = super.read(b, off, len);
                    if (bytesRead > 0) {
                        totalRead += bytesRead;
                        if (totalRead > MAX_IMAGE_SIZE) {
                            throw new IOException("Image exceeds maximum allowed size.");
                        }
                    }
                    return bytesRead;
                }
            };

            BufferedImage img = ImageIO.read(limitedStream);
            if (img == null) {
                throw new IOException("Invalid image format.");
            }

            return img;
        }
    }

    private boolean isPrivateAddress(InetAddress address) {
        return address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress();
    }

    private BufferedImage resizeImage(BufferedImage original, int targetW, int targetH) {
        BufferedImage dimg = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2d = dimg.createGraphics();
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.drawImage(original, 0, 0, targetW, targetH, null);
        g2d.dispose();
        return dimg;
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