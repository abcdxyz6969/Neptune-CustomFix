package dev.lrxh.neptune.game.arena.menu.button;

import dev.lrxh.neptune.game.arena.Arena;
import dev.lrxh.neptune.game.arena.ArenaService;
import dev.lrxh.neptune.utils.CC;
import dev.lrxh.neptune.utils.ItemBuilder;
import dev.lrxh.neptune.utils.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ArenaStatsButton extends Button {

    private final Arena arena;

    public ArenaStatsButton(int slot, Arena arena) {
        super(slot);
        this.arena = arena;
    }

    @Override
    public void onClick(ClickType type, Player player) {
        if (arena == null) return;

        boolean newState = !arena.isEnabled();
        arena.setEnabled(newState);

        ArenaService.get().save();

        player.sendMessage(CC.color("&aUpdated arena &f" + arena.getName() + " &7-> " +
                (newState ? "&aENABLED" : "&cDISABLED")).content());

        new dev.lrxh.neptune.game.arena.menu.ArenaStatsMenu().open(player);
    }

    @Override
    public ItemStack getItemStack(Player player) {
        if (arena == null) {
            return new ItemBuilder(Material.BARRIER)
                    .name(CC.color("&cUnknown Arena").content())
                    .lore(CC.color("&7No data.").content())
                    .build();
        }

        boolean enabled = arena.isEnabled();
        boolean setup = arena.isSetup();
        boolean loaded = arena.isDoneLoading();
        boolean used = arena.isUsed();

        boolean hasSnapshot = arena.getSnapshot() != null;
        boolean hasOwner = arena.getOwner() != null;
        boolean hasAllocId = arena.getAllocationId() != null;

        String status;
        Material mat;

        if (!setup) {
            status = "&cNOT_SETUP";
            mat = Material.GRAY_CONCRETE;
        } else if (!enabled) {
            status = "&cDISABLED";
            mat = Material.RED_CONCRETE;
        } else if (!loaded) {
            status = "&eLOADING";
            mat = Material.YELLOW_CONCRETE;
        } else if (used) {
            status = "&6IN_USE";
            mat = Material.ORANGE_CONCRETE;
        } else {
            status = "&aAVAILABLE";
            mat = Material.LIME_CONCRETE;
        }

        List<String> lore = new ArrayList<>();

        lore.add(CC.color("&7Name: &f" + arena.getName()).content());
        lore.add(CC.color("&7Display: &f" + arena.getDisplayName()).content());
        lore.add(CC.color("&7Status: " + status).content());

        lore.add("");
        lore.add(CC.color("&7Enabled: " + (enabled ? "&aTrue" : "&cFalse")).content());
        lore.add(CC.color("&7Setup: " + (setup ? "&aTrue" : "&cFalse")).content());
        lore.add(CC.color("&7Done Loading: " + (loaded ? "&aTrue" : "&cFalse")).content());
        lore.add(CC.color("&7Used: " + (used ? "&eTrue" : "&aFalse")).content());

        lore.add("");
        lore.add(CC.color("&7Snapshot: " + (hasSnapshot ? "&aREADY" : "&cNONE")).content());
        lore.add(CC.color("&7Owner: &f" + (hasOwner ? arena.getOwner().getName() : "NONE")).content());
        lore.add(CC.color("&7Allocation ID: &f" + (hasAllocId ? arena.getAllocationId() : "NONE")).content());

        lore.add("");
        lore.add(CC.color("&eClick to toggle enabled status").content());

        return new ItemBuilder(mat)
                .name(CC.color((enabled ? "&a" : "&c") + arena.getName()).content())
                .lore(lore)
                .build();
    }
}
