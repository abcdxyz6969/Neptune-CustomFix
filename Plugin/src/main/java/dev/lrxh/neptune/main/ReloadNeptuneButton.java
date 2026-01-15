package dev.lrxh.neptune.main;

import dev.lrxh.neptune.utils.CC;
import dev.lrxh.neptune.utils.ItemBuilder;
import dev.lrxh.neptune.utils.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class ReloadNeptuneButton extends Button {

    public ReloadNeptuneButton(int slot) {
        super(slot);
    }

    @Override
    public ItemStack getItem(Player player) {
        return new ItemBuilder(Material.NETHER_STAR)
                .setName(CC.color("&aReload Neptune"))
                .setLore(Arrays.asList(
                        CC.color("&7Reload config & services"),
                        CC.color("&7Click to run: &f/neptune reload")
                ))
                .build();
    }

    @Override
    public void onClick(Player player, ClickType clickType) {
        player.performCommand("neptune reload");
    }
}
