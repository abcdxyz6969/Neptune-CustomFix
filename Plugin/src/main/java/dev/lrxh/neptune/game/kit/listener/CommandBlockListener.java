package dev.lrxh.neptune.game.kit.listener;

import dev.lrxh.neptune.API;
import dev.lrxh.neptune.profile.data.ProfileState;
import dev.lrxh.neptune.profile.impl.Profile;
import dev.lrxh.neptune.utils.CC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CommandBlockListener implements Listener {

    private static final Set<String> BLOCKED = new HashSet<>(Arrays.asList(
            "queue", "queues",
            "spawn", "lobby", "hub", "leave",
            "party", "p",
            "duel", "match",
            "spec", "spectate",
            "rematch", "playagain",
            "ffa",
            "settings",
            "kit", "kits"
    ));

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        Profile profile = API.getProfile(player);
        if (profile == null) return;

        if (!profile.hasState(ProfileState.IN_KIT_EDITOR)) return;

        String msg = event.getMessage();
        if (msg == null) return;

        msg = msg.trim();
        if (msg.isEmpty()) return;

        String lower = msg.toLowerCase();
        if (!lower.startsWith("/")) return;

        String cmd = lower.substring(1);
        int space = cmd.indexOf(' ');
        if (space != -1) cmd = cmd.substring(0, space);

        if (cmd.equals("kiteditor") || cmd.equals("ke")) return;

        if (BLOCKED.contains(cmd)) {
            event.setCancelled(true);
            player.sendMessage(CC.color("&cYou can't use that command while editing a kit."));
        }
    }
}
