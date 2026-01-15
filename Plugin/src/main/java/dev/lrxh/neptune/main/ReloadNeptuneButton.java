package dev.lrxh.neptune.main;

import dev.lrxh.neptune.utils.menu.Button;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ReloadNeptuneButton extends Button {

    public ReloadNeptuneButton(int slot) {
        super(slot);
    }

    @Override
    public ItemStack getItemStack(Player player) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Reload Neptune"));
        meta.lore(List.of(
                Component.text("Reload config & services"),
                Component.text("Click to run: /neptune reload")
        ));

        item.setItemMeta(meta);
        return item;
    }

    private void handle(Player player) {
        player.performCommand("neptune reload");
    }

    // Các signature hay gặp trong các base Menu/Button khác nhau
    public void clicked(Player player, ClickType clickType) {
        handle(player);
    }

    public void clicked(Player player, int slot, ClickType clickType) {
        handle(player);
    }

    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
        handle(player);
    }

    public void onClick(Player player, ClickType clickType) {
        handle(player);
    }

    public void onClick(Player player, int slot, ClickType clickType) {
        handle(player);
    }
}
