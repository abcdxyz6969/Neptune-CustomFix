package dev.lrxh.neptune.feature.party.menu.buttons.settings;

import dev.lrxh.neptune.feature.party.Party;
import dev.lrxh.neptune.utils.ItemBuilder;
import dev.lrxh.neptune.utils.menu.Button;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PartyGlowPlayersButton extends Button {

    private final Party party;

    public PartyGlowPlayersButton(int slot, Party party) {
        super(slot);
        this.party = party;
    }

    @Override
    public void onClick(ClickType type, Player player) {
        party.setGlowPlayersEnabled(!party.isGlowPlayersEnabled());
    }

    @Override
    public ItemStack getItemStack(Player player) {
        String status = party.isGlowPlayersEnabled() ? "&aON" : "&cOFF";

        return new ItemBuilder("GLOWSTONE_DUST")
                .name("&eGlow Players")
                .lore(List.of(
                        "&7Status: " + status,
                        "",
                        "&7Split party teams will glow:",
                        "&cRed team &7= red glow",
                        "&9Blue team &7= blue glow"
                ), player)
                .build();
    }
}
