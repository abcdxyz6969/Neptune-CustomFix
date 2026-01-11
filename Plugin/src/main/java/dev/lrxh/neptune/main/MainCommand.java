package dev.lrxh.neptune.main;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Require;
import com.jonahseguin.drink.annotation.Sender;
import dev.lrxh.neptune.API;
import dev.lrxh.neptune.Neptune;
import dev.lrxh.neptune.configs.ConfigService;
import dev.lrxh.neptune.feature.cosmetics.CosmeticService;
import dev.lrxh.neptune.feature.hotbar.HotbarService;
import dev.lrxh.neptune.game.arena.Arena;
import dev.lrxh.neptune.game.arena.ArenaService;
import dev.lrxh.neptune.game.match.Match;
import dev.lrxh.neptune.game.match.MatchService;
import dev.lrxh.neptune.profile.data.ProfileState;
import dev.lrxh.neptune.profile.impl.Profile;
import dev.lrxh.neptune.utils.CC;
import dev.lrxh.neptune.utils.GithubUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class MainCommand {

    private static BukkitTask autoReloadSnapshotsTask;
    private static int autoReloadMinutes = 0;

    private static long totalReloadSnapshots = 0;
    private static long autoReloadSnapshotsRuns = 0;
    private static long lastReloadSnapshotsAtMillis = 0L;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

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

    // =========================
    // SNAPSHOTS
    // =========================

    @Command(name = "reloadsnapshots", desc = "Reload all arena snapshots")
    @Require("neptune.admin")
    public void reloadsnapshots(@Sender CommandSender sender) {
        ReloadResult result = reloadAllSnapshots(false);
        sender.sendMessage(CC.color("&aReload snapshots done &7(reloaded: &f" + result.reloaded
                + "&7, skipped(no region): &f" + result.skippedNoRegion
                + "&7, skipped(loading): &f" + result.skippedLoading + "&7)"));
    }

    @Command(name = "startautoreloadsnapshots", desc = "Start auto reload snapshots task", usage = "[minutes]")
    @Require("neptune.admin")
    public void startautoreloadsnapshots(@Sender CommandSender sender, Integer minutes) {
        if (autoReloadSnapshotsTask != null) {
            sender.sendMessage(CC.color("&cThere currently a task for Auto Reload Snapshots, try stop and start again."));
            return;
        }

        int m = (minutes == null ? 15 : minutes);
        if (m <= 0) m = 15;

        autoReloadMinutes = m;

        long periodTicks = (long) m * 60L * 20L;

        autoReloadSnapshotsTask = Bukkit.getScheduler().runTaskTimer(
                Neptune.get(),
                () -> {
                    ReloadResult result = reloadAllSnapshots(true);
                    autoReloadSnapshotsRuns++;
                },
                periodTicks,
                periodTicks
        );

        sender.sendMessage(CC.color("&aStarted Auto Reload Snapshots &7(every &f" + autoReloadMinutes + " &7minutes)"));
    }

    @Command(name = "stopautoreloadsnapshots", desc = "Stop auto reload snapshots task")
    @Require("neptune.admin")
    public void stopautoreloadsnapshots(@Sender CommandSender sender) {
        if (autoReloadSnapshotsTask == null) {
            sender.sendMessage(CC.color("&cThere is no Auto Reload Snapshots task running."));
            return;
        }

        autoReloadSnapshotsTask.cancel();
        autoReloadSnapshotsTask = null;
        autoReloadMinutes = 0;

        sender.sendMessage(CC.color("&aStopped Auto Reload Snapshots."));
    }

    @Command(name = "autoreloadsnapshotsstatus", desc = "Show auto reload snapshots status")
    @Require("neptune.admin")
    public void autoreloadsnapshotsstatus(@Sender CommandSender sender) {
        boolean running = autoReloadSnapshotsTask != null;

        sender.sendMessage(CC.color("&eAuto Reload Snapshots: " + (running ? "&aRUNNING" : "&cSTOPPED")));
        if (running) {
            sender.sendMessage(CC.color("&eInterval: &f" + autoReloadMinutes + " &eminutes"));
        }
        sender.sendMessage(CC.color("&eTotal reload snapshots: &f" + totalReloadSnapshots));
        sender.sendMessage(CC.color("&eAuto reload runs: &f" + autoReloadSnapshotsRuns));

        if (lastReloadSnapshotsAtMillis > 0L) {
            sender.sendMessage(CC.color("&eLast reload at: &f" + TIME_FMT.format(Instant.ofEpochMilli(lastReloadSnapshotsAtMillis))));
        } else {
            sender.sendMessage(CC.color("&eLast reload at: &fNever"));
        }
    }

    private ReloadResult reloadAllSnapshots(boolean silent) {
        int reloaded = 0;
        int skippedNoRegion = 0;
        int skippedLoading = 0;

        for (Arena arena : ArenaService.get().arenas) {
            if (arena == null) continue;

            if (arena.getMin() == null || arena.getMax() == null) {
                skippedNoRegion++;
                continue;
            }

            // đang loading snapshot thì bỏ qua để tránh spam task
            if (!arena.isDoneLoading()) {
                skippedLoading++;
                continue;
            }

            // Trick: gọi lại setMin/setMax để trigger tạo snapshot mới theo code Arena.setMin/setMax
            arena.setMin(arena.getMin());
            arena.setMax(arena.getMax());
            reloaded++;
        }

        totalReloadSnapshots++;
        lastReloadSnapshotsAtMillis = System.currentTimeMillis();

        return new ReloadResult(reloaded, skippedNoRegion, skippedLoading);
    }

    private static class ReloadResult {
        private final int reloaded;
        private final int skippedNoRegion;
        private final int skippedLoading;

        private ReloadResult(int reloaded, int skippedNoRegion, int skippedLoading) {
            this.reloaded = reloaded;
            this.skippedNoRegion = skippedNoRegion;
            this.skippedLoading = skippedLoading;
        }
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
