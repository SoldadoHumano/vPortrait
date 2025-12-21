<div align="center">
  <h1>vPortrait</h1>
  <p><i>High-performance image rendering for Minecraft frames via Netty.</i></p>
  
  <div>
    <img src="https://img.shields.io/badge/License-AGPL--3.0-blue?style=flat-square" alt="License">
    <img src="https://img.shields.io/badge/Java-21%2B-orange?style=flat-square" alt="Java Version">
    <img src="https://img.shields.io/badge/MC-1.21.3-brightgreen?style=flat-square" alt="Minecraft Version">
    <img src="https://img.shields.io/badge/Platform-Spigot%20%7C%20Paper-green?style=flat-square" alt="Platform">
  </div>
</div>

<br />

## 1. Overview

**vPortrait** is a high-performance Spigot/Paper plugin designed to render external images into *Glow Item Frames* using custom maps. The project focuses on low-level packet manipulation and security to ensure a seamless and safe experience for large-scale servers.

## 2. Key Technical Features

### 🛡️ Security First
* **SSRF & DoS Mitigation:** The `PortraitManager` implements strict 5-second timeouts and `User-Agent` validation to prevent thread-locking or network exploitation.
* **High-Priority Protection:** Listeners registered at `HIGHEST` priority ensure that portrait entities are immutable and protected against accidental player interactions.

### ⚡ Performance & Fluidity
* **Netty Pipeline Injection:** Uses `PlayerJoinListener` to inject custom handlers into the server's Netty pipeline. It intercepts `ClientboundAddEntityPacket` to force-send map data immediately, eliminating visual flickering when a player approaches a portrait.
* **Optimized Rendering:** Features a 10-second per-player cooldown on map canvas updates to save CPU cycles.
* **Bicubic Interpolation:** Images are processed using `Graphics2D` with bicubic interpolation, ensuring high-fidelity downscaling.

### 🧩 Smart Logic
* **Auto-Facing Detection:** The `SelectionManager` calculates the `BlockFace` based on the player's `Yaw` and the selection axis (X or Z), making placement intuitive.
* **GSON Persistence:** Lightweight and efficient data storage for fast plugin startup and shutdown cycles.

## 3. Requirements

* **Java 21** or higher.
* **Spigot/Paper API** (Compatible with current stable versions).
* Tested on Paper 1.21.3

## 4. Commands & Permissions

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/vportrait tool` | Gives the Portrait Wand (Golden Axe). | `vportrait.use` |
| `/vportrait upload <url>` | Renders the image in the selected area. | `vportrait.use` |
| `/vportrait remove` | Removes the portrait being looked at. | `vportrait.use` |
| `/vportrait reload` | Reload the system. | `vportrait.admin` |
| `/vportrait list` | Show count of active portraits. | `vportrait.admin` |
| `/vportrait sync` | Allows force sync images for yourself. | `vportrait.sync` |

Attention! For security reasons, all permissions are set to OP by default. Use a permissions management system of your choice to grant the appropriate permissions to the appropriate groups.

## 5. License

This project is licensed under the **GNU Affero General Public License v3.0 (AGPL-3.0)**. Any modified version or network-deployed service using this software must make its full source code available under the same license, as required by the AGPL-3.0.

---

## Premium Version

Interested in more features, dedicated support, or advanced optimizations? Check out the paid version of our tools at our official Discord store:

👉 **[Join ByVitor Discord Store](https://discord.gg/YUbR6YDGwp)**

---

<div align="center">
  <p>Developed with love by <b>vitor1227_OP (SoldadoHumano)</b></p>
</div>
