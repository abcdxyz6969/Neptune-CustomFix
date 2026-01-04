package dev.lrxh.neptune.feature.hotbar.listener;

import dev.lrxh.neptune.API;
import dev.lrxh.neptune.feature.hotbar.impl.CustomItem;
import dev.lrxh.neptune.feature.hotbar.impl.Item;
import dev.lrxh.neptune.game.match.impl.MatchState;
import dev.lrxh.neptune.profile.data.ProfileState;
import dev.lrxh.neptune.profile.impl.Profile;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public class ItemListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Profile profile = API.getProfile(player);

        if (profile.getMatch() != null
                && profile.getMatch().getState().equals(MatchState.IN_ROUND)
                && profile.getState() != ProfileState.IN_SPECTATOR) {
            return;
        }

        if (player.getGameMode().equals(GameMode.CREATIVE)) return;
        if (profile.getState() == ProfileState.IN_CUSTOM) return;

        event.setCancelled(true);

        if (event.getItem() == null || event.getItem().getType().equals(Material.AIR)) return;
        if (!(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))) return;

        Item clickedItem = Item.getByItemStack(profile.getState(), event.getItem(), player.getUniqueId());
        if (clickedItem == null) return;

        if (!profile.hasCooldownEnded("hotbar")) return;

        if (clickedItem instanceof CustomItem) {
            CustomItem customItem = (CustomItem) clickedItem;

            List<String> consoleCommands = customItem.getConsoleCommands();
            if (consoleCommands != null && !consoleCommands.isEmpty()) {
                for (String command : consoleCommands) {
                    if (command == null) continue;
                    command = command.trim();
                    if (command.isEmpty() || command.equalsIgnoreCase("none")) continue;

                    command = command.replace("%player_name%", player.getName());

                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                }
            }

            List<String> commands = customItem.getCommands();
            if (commands != null && !commands.isEmpty()) {
                for (String command : commands) {
                    if (command == null) continue;
                    command = command.trim();
                    if (command.isEmpty() || command.equalsIgnoreCase("none")) continue;

                    command = command.replace("%player_name%", player.getName());

                    if (command.startsWith("/")) command = command.substring(1);
                    player.performCommand(command);
                }
            }
        } else {
            clickedItem.getAction().execute(player);
        }

        profile.addCooldown("hotbar", 200);
    }
}
