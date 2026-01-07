package dev.lrxh.neptune.game.kit.listener;

import dev.lrxh.neptune.API;
import dev.lrxh.neptune.game.kit.Kit;
import dev.lrxh.neptune.game.kit.KitEditorLocationService;
import dev.lrxh.neptune.profile.data.ProfileState;
import dev.lrxh.neptune.profile.impl.Profile;
import dev.lrxh.neptune.utils.CC;
import dev.lrxh.neptune.utils.PlayerUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;

public class KitEditorBlockListener implements Listener {

    private static final String ERROR_MSG = "&cHmm, We can't handle this request, this may be error, try contact admin.";
    private static final String SAVED_MSG = "&a✔ Layout kit saved";

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (!(action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK)) return;

        Block clicked = event.getClickedBlock();
        if (clicked == null) return;

        Location saveLoc = KitEditorLocationService.getSaveBlockLocation();
        Location resetLoc = KitEditorLocationService.getResetBlockLocation();

        if (saveLoc == null && resetLoc == null) return;

        boolean isSave = sameBlock(clicked.getLocation(), saveLoc);
        boolean isReset = sameBlock(clicked.getLocation(), resetLoc);

        if (!isSave && !isReset) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        Profile profile = API.getProfile(player);
        if (profile == null) return;

        if (!profile.hasState(ProfileState.IN_KIT_EDITOR) || profile.getGameData().getKitEditor() == null) {
            player.sendMessage(CC.color(ERROR_MSG));
            return;
        }

        Kit kit = profile.getGameData().getKitEditor();

        if (isSave) {
            profile.getGameData().get(kit).setKitLoadout(Arrays.asList(player.getInventory().getContents()));

            player.sendMessage(CC.color(SAVED_MSG));
            player.sendActionBar(CC.color(SAVED_MSG).content());

            if (player.isSneaking()) {
                profile.getGameData().setKitEditor(null);

                if (profile.getGameData().getParty() == null) {
                    profile.setState(ProfileState.IN_LOBBY);
                } else {
                    profile.setState(ProfileState.IN_PARTY);
                }

                PlayerUtil.teleportToSpawn(player.getUniqueId());
            }
            return;
        }

        if (isReset) {
            profile.getGameData().get(kit).setKitLoadout(kit.getItems());
            kit.giveLoadout(player.getUniqueId());
            player.updateInventory();
            player.sendMessage(CC.color("&a✔ Kit layout reset"));
            player.sendActionBar(CC.color("&a✔ Kit layout reset").content());
        }
    }

    private boolean sameBlock(Location a, Location b) {
        if (a == null || b == null) return false;
        if (a.getWorld() == null || b.getWorld() == null) return false;
        if (!a.getWorld().getName().equalsIgnoreCase(b.getWorld().getName())) return false;
        return a.getBlockX() == b.getBlockX() && a.getBlockY() == b.getBlockY() && a.getBlockZ() == b.getBlockZ();
    }
}
