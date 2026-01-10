package dev.lrxh.neptune.main;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import dev.lrxh.neptune.API;
import dev.lrxh.neptune.Neptune;
import dev.lrxh.neptune.configs.ConfigService;
import dev.lrxh.neptune.feature.cosmetics.CosmeticService;
import dev.lrxh.neptune.feature.hotbar.HotbarService;
import dev.lrxh.neptune.game.match.Match;
import dev.lrxh.neptune.game.match.MatchService;
import dev.lrxh.neptune.profile.data.ProfileState;
import dev.lrxh.neptune.profile.impl.Profile;
import dev.lrxh.neptune.utils.CC;
import dev.lrxh.neptune.utils.GithubUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MainCommand {

    @Command(name = "", desc = "")
    @Require("neptune.admin")
    public void help(@Sender Player player) {
        new MainMenu().open(player);
    }

    @Command(name = "setspawn", desc = "")
    @Require("neptune.admin")
    public void setspawn(@Sender Player player) {
        Neptune.get().getCache().setSpawn(player.getLocation());
        player.sendMessage(CC.color("&aSuccessfully set spawn!"));
    }

    @Command(name = "info", desc = "")
    @Require("neptune.admin")
    public void info(@Sender Player player) {
        player.sendMessage(CC.color("&eThis server is running Neptune version: "
                + Neptune.get().getDescription().getVersion()));
        player.sendMessage(CC.color("&eCommit: &f" + GithubUtils.getCommitId()));
        player.sendMessage(CC.color("&eMessage: &f" + GithubUtils.getCommitMessage()));
    }

    @Command(name = "reload", desc = "")
    @Require("neptune.admin")
    public void reload(@Sender CommandSender sender) {
        ConfigService.get().load();
        CosmeticService.get().load();
        HotbarService.get().load();

        for (Player p : Bukkit.getOnlinePlayers()) {
            Profile profile = API.getProfile(p);
            if (profile.getState().equals(ProfileState.IN_GAME)
                    || profile.getState().equals(ProfileState.IN_KIT_EDITOR))
                return;
            HotbarService.get().giveItems(p);
        }

        sender.sendMessage(CC.color("&aSuccessfully reloaded configs!"));
    }

    @Command(name = "setkiteditorlocation", desc = "")
    @Require("neptune.admin")
    public void setkiteditorlocation(@Sender Player player) {
        dev.lrxh.neptune.game.kit.KitEditorLocationService.setLocation(player.getLocation());
        player.sendMessage(CC.color("&aSuccessfully set kit editor location!"));
    }

    @Command(name = "removekiteditorlocation", desc = "")
    @Require("neptune.admin")
    public void removekiteditorlocation(@Sender Player player) {
        dev.lrxh.neptune.game.kit.KitEditorLocationService.removeLocation();
        player.sendMessage(CC.color("&aSuccessfully removed kit editor location!"));
    }

    @Command(name = "setkiteditorsaveblock", desc = "")
    @Require("neptune.admin")
    public void setkiteditorsaveblock(@Sender Player player) {
        org.bukkit.block.Block block = player.getTargetBlockExact(5);
        if (block == null || block.getType().isAir()) {
            player.sendMessage(CC.color("&cYou must look at a solid block."));
            return;
        }
        dev.lrxh.neptune.game.kit.KitEditorLocationService.setSaveBlock(block);
        player.sendMessage(CC.color("&aSuccessfully set kit editor SAVE block!"));
    }

    @Command(name = "setkiteditorresetblock", desc = "")
    @Require("neptune.admin")
    public void setkiteditorresetblock(@Sender Player player) {
        org.bukkit.block.Block block = player.getTargetBlockExact(5);
        if (block == null || block.getType().isAir()) {
            player.sendMessage(CC.color("&cYou must look at a solid block."));
            return;
        }
        dev.lrxh.neptune.game.kit.KitEditorLocationService.setResetBlock(block);
        player.sendMessage(CC.color("&aSuccessfully set kit editor RESET block!"));
    }

    @Command(name = "arenastats", desc = "")
    @Require("neptune.admin")
    public void arenastats(@Sender Player player) {
        new dev.lrxh.neptune.game.arena.menu.ArenaStatsMenu().open(player);
    }

    @Command(name = "playerinarena", desc = "", usage = "<arena>")
    @Require("neptune.admin")
    public void playerinarena(@Sender Player player, String arenaName) {
        dev.lrxh.neptune.game.arena.Arena arena =
                dev.lrxh.neptune.game.arena.ArenaService.get().getArenaByName(arenaName);

        if (arena == null) {
            player.sendMessage(CC.color("&cArena not found: &f" + arenaName).content());
            return;
        }

        if (arena.getMin() == null || arena.getMax() == null) {
            player.sendMessage(CC.color("&cArena &f" + arena.getName() + " &chas no min/max set.").content());
            return;
        }

        org.bukkit.Location minLoc = arena.getMin();
        org.bukkit.Location maxLoc = arena.getMax();

        if (minLoc.getWorld() == null || maxLoc.getWorld() == null) {
            player.sendMessage(CC.color("&cArena &f" + arena.getName() + " &cworld is null.").content());
            return;
        }

        if (!minLoc.getWorld().getName().equalsIgnoreCase(maxLoc.getWorld().getName())) {
            player.sendMessage(CC.color("&cArena &f" + arena.getName() + " &cmin/max world mismatch.").content());
            return;
        }

        int minX = Math.min(minLoc.getBlockX(), maxLoc.getBlockX());
        int maxX = Math.max(minLoc.getBlockX(), maxLoc.getBlockX());
        int minY = Math.min(minLoc.getBlockY(), maxLoc.getBlockY());
        int maxY = Math.max(minLoc.getBlockY(), maxLoc.getBlockY());
        int minZ = Math.min(minLoc.getBlockZ(), maxLoc.getBlockZ());
        int maxZ = Math.max(minLoc.getBlockZ(), maxLoc.getBlockZ());

        List<String> names = new ArrayList<>();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getWorld() == null) continue;
            if (!p.getWorld().getName().equalsIgnoreCase(minLoc.getWorld().getName())) continue;

            int x = p.getLocation().getBlockX();
            int y = p.getLocation().getBlockY();
            int z = p.getLocation().getBlockZ();

            if (x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ) {
                names.add(p.getName());
            }
        }

        player.sendMessage(CC.color("&bArena: &f" + arena.getName()).content());
        player.sendMessage(CC.color("&7Players inside region: &f" + names.size()).content());

        if (!names.isEmpty()) {
            player.sendMessage(CC.color("&7List:").content());
            for (String name : names) {
                player.sendMessage(CC.color("&f- " + name).content());
            }
        }
    }

    @Command(name = "reloadsnapshots", desc = "")
    @Require("neptune.admin")
    public void reloadsnapshots(@Sender Player player) {

        List<dev.lrxh.neptune.game.arena.Arena> arenas =
                new ArrayList<>(dev.lrxh.neptune.game.arena.ArenaService.get().getArenas());

        if (arenas.isEmpty()) {
            player.sendMessage(CC.color("&cNo arenas loaded.").content());
            return;
        }

        player.sendMessage(CC.color("&bReloading snapshots for &f" + arenas.size() + " &baren as...").content());

        AtomicInteger total = new AtomicInteger(0);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);

        for (dev.lrxh.neptune.game.arena.Arena arena : arenas) {
            if (arena == null) continue;
            if (arena.getMin() == null || arena.getMax() == null) continue;

            total.incrementAndGet();

            arena.setDoneLoading(false);
            arena.setSnapshot(null);

            Bukkit.getScheduler().runTask(Neptune.get(), () -> {
                try {
                    org.bukkit.Location min = arena.getMin();
                    org.bukkit.Location max = arena.getMax();

                    if (min.getWorld() == null || max.getWorld() == null) {
                        failed.incrementAndGet();
                        arena.setDoneLoading(false);
                        checkFinish(player, total, success, failed);
                        return;
                    }

                    int minChunkX = Math.min(min.getBlockX(), max.getBlockX()) >> 4;
                    int maxChunkX = Math.max(min.getBlockX(), max.getBlockX()) >> 4;
                    int minChunkZ = Math.min(min.getBlockZ(), max.getBlockZ()) >> 4;
                    int maxChunkZ = Math.max(min.getBlockZ(), max.getBlockZ()) >> 4;

                    for (int cx = minChunkX; cx <= maxChunkX; cx++) {
                        for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                            min.getWorld().getChunkAt(cx, cz).load(true);
                        }
                    }

                    dev.lrxh.blockChanger.snapshot.CuboidSnapshot.create(min, max).thenAccept(snapshot -> {
                        if (snapshot == null) {
                            failed.incrementAndGet();
                            arena.setDoneLoading(false);
                        } else {
                            success.incrementAndGet();
                            arena.setSnapshot(snapshot);
                            arena.setDoneLoading(true);
                        }

                        checkFinish(player, total, success, failed);
                    });

                } catch (Throwable t) {
                    failed.incrementAndGet();
                    arena.setDoneLoading(false);
                    checkFinish(player, total, success, failed);
                }
            });
        }

        if (total.get() == 0) {
            player.sendMessage(CC.color("&cNo valid arenas with min/max set to reload.").content());
        }
    }

    private void checkFinish(Player player, AtomicInteger total, AtomicInteger success, AtomicInteger failed) {
        int done = success.get() + failed.get();
        if (done < total.get()) return;

        player.sendMessage(CC.color("&a✔ Snapshot reload finished!").content());
        player.sendMessage(CC.color("&7Total: &f" + total.get()
                + " &7| &aSuccess: &f" + success.get()
                + " &7| &cFailed: &f" + failed.get()).content());
    }

    @Command(name = "stop", desc = "")
    public void stop(@Sender Player player) {
        Neptune.get().setAllowMatches(false);

        for (Match match : MatchService.get().matches) {
            match.resetArena();
        }

        Bukkit.getServer().shutdown();
    }
}
