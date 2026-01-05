package dev.lrxh.neptune.game.kit;

import dev.lrxh.neptune.configs.ConfigService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public class KitEditorLocationService {

    private static final String PATH = "LOCATION";

    public static Location getLocation() {
        FileConfiguration config = ConfigService.get().getKitEditorLocationConfig().getConfiguration();
        if (!config.contains(PATH + ".WORLD")) return null;

        World world = Bukkit.getWorld(config.getString(PATH + ".WORLD"));
        if (world == null) return null;

        double x = config.getDouble(PATH + ".X");
        double y = config.getDouble(PATH + ".Y");
        double z = config.getDouble(PATH + ".Z");
        float yaw = (float) config.getDouble(PATH + ".YAW");
        float pitch = (float) config.getDouble(PATH + ".PITCH");

        return new Location(world, x, y, z, yaw, pitch);
    }

    public static void setLocation(Location loc) {
        FileConfiguration config = ConfigService.get().getKitEditorLocationConfig().getConfiguration();

        if (loc == null) {
            config.set(PATH, null);
            ConfigService.get().getKitEditorLocationConfig().save();
            return;
        }

        config.set(PATH + ".WORLD", loc.getWorld().getName());
        config.set(PATH + ".X", loc.getX());
        config.set(PATH + ".Y", loc.getY());
        config.set(PATH + ".Z", loc.getZ());
        config.set(PATH + ".YAW", loc.getYaw());
        config.set(PATH + ".PITCH", loc.getPitch());

        ConfigService.get().getKitEditorLocationConfig().save();
    }

    public static void removeLocation() {
        setLocation(null);
    }
}
