package dev.lrxh.neptune.game.kit;

import dev.lrxh.neptune.configs.ConfigService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

public class KitEditorLocationService {

    private static final String LOC_PATH = "LOCATION";
    private static final String SAVE_BLOCK_PATH = "SAVE_BLOCK";
    private static final String RESET_BLOCK_PATH = "RESET_BLOCK";

    public static Location getLocation() {
        return readLocation(LOC_PATH);
    }

    public static void setLocation(Location loc) {
        writeLocation(LOC_PATH, loc);
    }

    public static void removeLocation() {
        writeLocation(LOC_PATH, null);
    }

    public static Location getSaveBlockLocation() {
        return readLocation(SAVE_BLOCK_PATH);
    }

    public static void setSaveBlock(Block block) {
        if (block == null) {
            writeLocation(SAVE_BLOCK_PATH, null);
            return;
        }
        writeLocation(SAVE_BLOCK_PATH, block.getLocation());
    }

    public static Location getResetBlockLocation() {
        return readLocation(RESET_BLOCK_PATH);
    }

    public static void setResetBlock(Block block) {
        if (block == null) {
            writeLocation(RESET_BLOCK_PATH, null);
            return;
        }
        writeLocation(RESET_BLOCK_PATH, block.getLocation());
    }

    private static Location readLocation(String path) {
        FileConfiguration config = ConfigService.get().getKitEditorLocationConfig().getConfiguration();

        if (!config.contains(path + ".WORLD")) return null;

        World world = Bukkit.getWorld(config.getString(path + ".WORLD"));
        if (world == null) return null;

        double x = config.getDouble(path + ".X");
        double y = config.getDouble(path + ".Y");
        double z = config.getDouble(path + ".Z");
        float yaw = (float) config.getDouble(path + ".YAW", 0);
        float pitch = (float) config.getDouble(path + ".PITCH", 0);

        return new Location(world, x, y, z, yaw, pitch);
    }

    private static void writeLocation(String path, Location loc) {
        FileConfiguration config = ConfigService.get().getKitEditorLocationConfig().getConfiguration();

        if (loc == null) {
            config.set(path, null);
            ConfigService.get().getKitEditorLocationConfig().save();
            return;
        }

        config.set(path + ".WORLD", loc.getWorld().getName());
        config.set(path + ".X", loc.getBlockX());
        config.set(path + ".Y", loc.getBlockY());
        config.set(path + ".Z", loc.getBlockZ());
        config.set(path + ".YAW", loc.getYaw());
        config.set(path + ".PITCH", loc.getPitch());

        ConfigService.get().getKitEditorLocationConfig().save();
    }
}
