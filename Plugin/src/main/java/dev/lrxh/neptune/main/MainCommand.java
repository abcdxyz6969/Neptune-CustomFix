package dev.lrxh.neptune.main;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import dev.lrxh.blockChanger.snapshot.CuboidSnapshot;
import dev.lrxh.neptune.Neptune;
import dev.lrxh.neptune.game.arena.Arena;
import dev.lrxh.neptune.game.arena.ArenaService;
import dev.lrxh.neptune.game.arena.menu.ArenaStatsMenu;
import dev.lrxh.neptune.game.match.MatchService;
import dev.lrxh.neptune.game.match.Match;
import dev.lrxh.neptune.utils.CC;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.atomic.AtomicInteger;

public class MainCommand {

    private static BukkitTask autoReloadTask = null;
    private static long autoReloadMinutes = 15;

    private static final AtomicInteger reloadCount = new AtomicInteger(0);
    private static volatile long lastReloadMillis = 0L;

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

    // ====== Arena stats menu ======

    @Command(name = "arenastats", desc = "")
    @Require("neptune.admin")
    public void arenastats(@Sender Player player) {
        new ArenaStatsMenu().open(player);
    }

    // ====== Snapshots reload ======

    @Command(name = "reloadsnapshots", desc = "")
    @Require("neptune.admin")
    public void reloadsnapshots(@Sender CommandSender sender) {
        int total = 0;

        for (Arena arena : ArenaService.get().arenas) {
            if (arena == null) continue;

            // IGNORE disabled arenas
            if (!arena.isEnabled()) continue;

            if (arena.getMin() == null || arena.getMax() == null) continue;

            forceReloadSnapshot(arena);
            total++;
        }

        sender.sendMessage(CC.color("&aReload snapshots started for &f" + total + " &aarenas."));
    }

    @Command(name = "reloadsnapshotsarena", desc = "")
    @Require("neptune.admin")
    public void reloadsnapshotsarena(@Sender CommandSender sender, String arenaName) {
        Arena arena = ArenaService.get().getArenaByName(arenaName);
        if (arena == null) {
            sender.sendMessage(CC.color("&cArena not found: &f" + arenaName));
            return;
        }

        // If disabled -> ignore
        if (!arena.isEnabled()) {
            sender.sendMessage(CC.color("&eArena &f" + arena.getName() + " &eis &cdisabled &e-> ignored."));
            return;
        }

        if (arena.getMin() == null || arena.getMax() == null) {
            sender.sendMessage(CC.color("&cArena &f" + arena.getName() + " &cis not setup (min/max missing)."));
            return;
        }

        forceReloadSnapshot(arena);
        sender.sendMessage(CC.color("&aReload snapshot started for &f" + arena.getName() + "&a."));
    }

    private static void forceReloadSnapshot(Arena arena) {
        // mark not ready
        arena.setDoneLoading(false);
        arena.setSnapshot(null);

        // create new snapshot async
        CuboidSnapshot.create(arena.getMin(), arena.getMax()).thenAccept(snapshot -> {
            arena.setSnapshot(snapshot);
            arena.setDoneLoading(true);

            reloadCount.incrementAndGet();
            lastReloadMillis = System.currentTimeMillis();
        }).exceptionally(ex -> {
            arena.setDoneLoading(false);
            return null;
        });
    }

    // ====== Auto reload snapshots ======

    @Command(name = "startautoreloadsnapshots", desc = "")
    @Require("neptune.admin")
    public void startautoreloadsnapshots(@Sender CommandSender sender, Integer minutes) {
        if (autoReloadTask != null) {
            sender.sendMessage(CC.color("&cThere currently a task for Auto Reload Snapshots, try stop and start again."));
            return;
        }

        long mins = (minutes == null ? 15 : minutes);
        if (mins <= 0) mins = 15;

        autoReloadMinutes = mins;

        long periodTicks = autoReloadMinutes * 60L * 20L;

        autoReloadTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                Neptune.get(),
                () -> {
                    for (Arena arena : ArenaService.get().arenas) {
                        if (arena == null) continue;

                        // IGNORE disabled arenas
                        if (!arena.isEnabled()) continue;

                        if (arena.getMin() == null || arena.getMax() == null) continue;
                        forceReloadSnapshot(arena);
                    }
                },
                periodTicks,
                periodTicks
        );

        sender.sendMessage(CC.color("&aStarted Auto Reload Snapshots every &f" + autoReloadMinutes + " &aminutes."));
    }

    @Command(name = "stopautoreloadsnapshots", desc = "")
    @Require("neptune.admin")
    public void stopautoreloadsnapshots(@Sender CommandSender sender) {
        if (autoReloadTask != null) {
            autoReloadTask.cancel();
            autoReloadTask = null;
            sender.sendMessage(CC.color("&aStopped Auto Reload Snapshots."));
            return;
        }

        sender.sendMessage(CC.color("&cThere is no Auto Reload Snapshots task running."));
    }

    @Command(name = "autoreloadsnapshotsstatus", desc = "")
    @Require("neptune.admin")
    public void autoreloadsnapshotsstatus(@Sender CommandSender sender) {
        boolean running = autoReloadTask != null;

        sender.sendMessage(CC.color("&eAuto Reload Snapshots Status"));
        sender.sendMessage(CC.color("&7Running: " + (running ? "&aYES" : "&cNO")));
        sender.sendMessage(CC.color("&7Interval: &f" + autoReloadMinutes + " minutes"));
        sender.sendMessage(CC.color("&7Total reloads done: &f" + reloadCount.get()));

        if (lastReloadMillis > 0) {
            long secAgo = (System.currentTimeMillis() - lastReloadMillis) / 1000L;
            sender.sendMessage(CC.color("&7Last reload: &f" + secAgo + "s ago"));
        } else {
            sender.sendMessage(CC.color("&7Last reload: &cNever"));
        }
    }

    // ====== stop server ======

    @Command(name = "stop", desc = "")
    public void stop(@Sender Player player) {
        Neptune.get().setAllowMatches(false);

        for (Match match : MatchService.get().matches) {
            match.resetArena();
        }

        Bukkit.getServer().shutdown();
    }
}
