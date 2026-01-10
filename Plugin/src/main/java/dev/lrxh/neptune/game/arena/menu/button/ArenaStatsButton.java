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
                (newState ? "&aENABLED" : "&cDISABLED")));
    }

    @Override
    public ItemStack getItemStack(Player player) {
        boolean enabled = arena.isEnabled();
        boolean setup = arena.isSetup();
        boolean loaded = arena.isDoneLoading();
        boolean used = arena.isUsed();
        boolean hasSnapshot = arena.getSnapshot() != null;

        Material mat = enabled ? Material.LIME_CONCRETE : Material.RED_CONCRETE;

        List<String> lore = new ArrayList<>();
        lore.add(CC.color("&7Name: &f" + arena.getName()));
        lore.add(CC.color("&7Display: &f" + arena.getDisplayName()));
        lore.add(CC.color("&7Enabled: " + (enabled ? "&aTrue" : "&cFalse")));
        lore.add(CC.color("&7Setup: " + (setup ? "&aTrue" : "&cFalse")));
        lore.add(CC.color("&7Snapshot Ready: " + (hasSnapshot ? "&aYes" : "&cNo")));
        lore.add(CC.color("&7Done Loading: " + (loaded ? "&aTrue" : "&cFalse")));
        lore.add(CC.color("&7Used: " + (used ? "&eTrue" : "&aFalse")));

        if (arena.getOwner() != null) {
            lore.add(CC.color("&7Duplicate Of: &f" + arena.getOwner().getName()));
        }

        if (arena.getAllocationId() != null) {
            lore.add(CC.color("&7AllocId: &f" + arena.getAllocationId()));
        }

        lore.add(CC.color(""));
        lore.add(CC.color("&eClick to toggle enabled status"));

        return new ItemBuilder(mat)
                .name(CC.color((enabled ? "&a" : "&c") + arena.getName()))
                .lore(lore)
                .build();
    }
}
