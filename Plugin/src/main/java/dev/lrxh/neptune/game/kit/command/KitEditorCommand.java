package dev.lrxh.neptune.game.kit.command;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import dev.lrxh.neptune.API;
import dev.lrxh.neptune.configs.impl.MessagesLocale;
import dev.lrxh.neptune.game.kit.Kit;
import dev.lrxh.neptune.game.kit.menu.editor.KitEditorMenu;
import dev.lrxh.neptune.game.kit.menu.editor.button.KitEditorSelectButton;
import dev.lrxh.neptune.profile.data.ProfileState;
import dev.lrxh.neptune.profile.impl.Profile;
import dev.lrxh.neptune.providers.clickable.Replacement;
import dev.lrxh.neptune.utils.CC;
import dev.lrxh.neptune.utils.PlayerUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Arrays;

public class KitEditorCommand {

    private static final String SAVED_MESSAGE = "&a✔ Layout kit saved";

    @Command(name = "menu", desc = "")
    public void open(@Sender Player player) {
        Profile profile = API.getProfile(player);
        if (profile == null) return;
        if (profile.hasState(ProfileState.IN_LOBBY, ProfileState.IN_PARTY)) {
            new KitEditorMenu().open(player);
        }
    }

    @Command(name = "edit", desc = "", usage = "<kit>")
    public void edit(@Sender Player player, Kit kit) {
        if (player == null) return;
        Profile profile = API.getProfile(player);
        if (profile.hasState(ProfileState.IN_LOBBY, ProfileState.IN_PARTY)) {
            new KitEditorSelectButton(0, kit).onClick(ClickType.LEFT, player);
        }
    }

    @Command(name = "reset", desc = "", usage = "<kit>")
    public void reset(@Sender Player player, Kit kit) {
        if (player == null) return;
        Profile profile = API.getProfile(player);

        profile.getGameData().get(kit).setKitLoadout(kit.getItems());

        if (profile.hasState(ProfileState.IN_KIT_EDITOR) && profile.getGameData().getKitEditor() != null) {
            if (profile.getGameData().getKitEditor().equals(kit)) {
                kit.giveLoadout(player.getUniqueId());
                player.updateInventory();
            }
        }

        MessagesLocale.KIT_EDITOR_RESET.send(player.getUniqueId(), new Replacement("<kit>", kit.getDisplayName()));
    }

    @Command(name = "save", desc = "")
    public void save(@Sender Player player) {
        Profile profile = API.getProfile(player);
        if (profile == null) return;
        if (!profile.hasState(ProfileState.IN_KIT_EDITOR)) return;
        if (profile.getGameData().getKitEditor() == null) return;

        Kit kit = profile.getGameData().getKitEditor();
        profile.getGameData().get(kit).setKitLoadout(Arrays.asList(player.getInventory().getContents()));

        player.sendMessage(CC.color(SAVED_MESSAGE));
        player.sendActionBar(CC.color(SAVED_MESSAGE).content());
    }

    @Command(name = "exit", desc = "")
    public void exit(@Sender Player player) {
        Profile profile = API.getProfile(player);
        if (profile == null) return;
        if (!profile.hasState(ProfileState.IN_KIT_EDITOR)) return;
        if (profile.getGameData().getKitEditor() == null) return;

        Kit kit = profile.getGameData().getKitEditor();
        profile.getGameData().get(kit).setKitLoadout(Arrays.asList(player.getInventory().getContents()));

        player.sendMessage(CC.color(SAVED_MESSAGE));
        player.sendActionBar(CC.color(SAVED_MESSAGE).content());

        profile.getGameData().setKitEditor(null);

        if (profile.getGameData().getParty() == null) {
            profile.setState(ProfileState.IN_LOBBY);
        } else {
            profile.setState(ProfileState.IN_PARTY);
        }

        PlayerUtil.teleportToSpawn(player.getUniqueId());
    }
}
