package dev.lrxh.neptune.game.arena.menu.button;

import dev.lrxh.blockChanger.snapshot.CuboidSnapshot;
import dev.lrxh.neptune.game.arena.Arena;
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
        super(slot, false);
        this.arena = arena;
    }

    @Override
    public void onClick(ClickType type, Player player) {
        arena.setEnabled(!arena.isEnabled());
        player.sendMessage(CC.color("&aUpdated arena &f" + arena.getName() + " &ato &f" + (arena.isEnabled() ? "ENABLED" : "DISABLED")));
    }

    @Override
    public ItemStack getItemStack(Player player) {
        boolean setup = arena.isSetup();
        boolean enabled = arena.isEnabled();
        boolean used = arena.isUsed();
        boolean doneLoading = arena.isDoneLoading();
        CuboidSnapshot snapshot = arena.getSnapshot();

        Material mat;
        if (!setup) mat = Material.BARRIER;
        else if (!enabled) mat = Material.RED_CONCRETE;
        else if (used) mat = Material.LIME_CONCRETE;
        else mat = Material.YELLOW_CONCRETE;

        List<String> lore = new ArrayList<>();
        lore.add("&7Name: &f" + arena.getName());
        lore.add("&7Display: &f" + (arena.getDisplayName() == null ? "&cnull" : arena.getDisplayName()));
        lore.add("&7Setup: " + (setup ? "&aYES" : "&cNO"));
        lore.add("&7Enabled: " + (enabled ? "&aYES" : "&cNO"));
        lore.add("&7Used: " + (used ? "&cIN USE" : "&aIDLE"));
        lore.add("&7DoneLoading: " + (doneLoading ? "&aYES" : "&eNO"));
        lore.add("&7Snapshot: &f" + (snapshot == null ? "&cnull" : "&aREADY"));
        lore.add("&8");
        lore.add("&eClick &7to toggle enabled");

        return new ItemBuilder(mat)
                .name("&e" + arena.getName())
                .lore(lore)
                .build();
    }
}
