package dev.lrxh.neptune.game.arena.tasks;

import dev.lrxh.neptune.API;
import dev.lrxh.neptune.profile.data.ProfileState;
import dev.lrxh.neptune.profile.impl.Profile;
import dev.lrxh.neptune.utils.tasks.NeptuneRunnable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class ArenaForceTeleportTask extends NeptuneRunnable {

    private static final String ARENA_WORLD_NAME = "arenas";
    private static final String BYPASS_PERMISSION = "neptune.arenaforcetpdisable";

    private static final Location FALLBACK_LOC;

    static {
        World world = Bukkit.getWorld(ARENA_WORLD_NAME);
        if (world != null) {
            FALLBACK_LOC = new Location(world, 0.5, 100, 0.5, 0F, 0F);
        } else {
            FALLBACK_LOC = null;
        }
    }

    private final Map<UUID, Integer> stageMap = new HashMap<>();

    @Override
    public void run() {
        World arenaWorld = Bukkit.getWorld(ARENA_WORLD_NAME);
        if (arenaWorld == null) {
            stageMap.clear();
            return;
        }

        // cleanup offline uuids
        Iterator<UUID> it = stageMap.keySet().iterator();
        while (it.hasNext()) {
            UUID uuid = it.next();
            if (Bukkit.getPlayer(uuid) == null) it.remove();
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(BYPASS_PERMISSION)) {
                stageMap.remove(player.getUniqueId());
                continue;
            }

            if (player.getWorld() != arenaWorld) {
                stageMap.remove(player.getUniqueId());
                continue;
            }

            Profile profile = API.getProfile(player);
            if (profile == null) {
                stageMap.remove(player.getUniqueId());
                continue;
            }

            // Nếu đang trong trận / spectate / hoặc match != null -> không kéo
            if (profile.hasState(ProfileState.IN_GAME) || profile.hasState(ProfileState.IN_SPECTATOR) || profile.getMatch() != null) {
                stageMap.remove(player.getUniqueId());
                continue;
            }

            UUID uuid = player.getUniqueId();
            int stage = stageMap.getOrDefault(uuid, 0);

            if (stage == 0) {
                // phát hiện lần đầu -> đợi 5s nữa xử lý
                stageMap.put(uuid, 1);
                continue;
            }

            if (stage == 1) {
                // 5s sau -> tp về 0 100 0
                if (FALLBACK_LOC != null) {
                    player.teleport(FALLBACK_LOC);
                } else {
                    player.teleport(new Location(arenaWorld, 0.5, 100, 0.5, 0F, 0F));
                }
                stageMap.put(uuid, 2);
                continue;
            }

            if (stage == 2) {
                // 5s nữa -> tp về spawn
                dev.lrxh.neptune.utils.PlayerUtil.teleportToSpawn(uuid);
                stageMap.remove(uuid);
            }
        }
    }
}
