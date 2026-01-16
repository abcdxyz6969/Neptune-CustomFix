package dev.lrxh.neptune.feature.party.menu.buttons.settings;

import dev.lrxh.neptune.feature.party.Party;
import dev.lrxh.neptune.utils.ItemBuilder;
import dev.lrxh.neptune.utils.menu.Button;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class PartyRespawnButton extends Button {
    private final Party party;

    public PartyRespawnButton(int slot, Party party) {
        super(slot);
        this.party = party;
    }

    @Override
    public void onClick(ClickType type, Player player) {
        party.setFfaRespawnEnabled(!party.isFfaRespawnEnabled());
    }

    @Override
    public ItemStack getItemStack(Player player) {
        String status = party.isFfaRespawnEnabled() ? "&aON" : "&cOFF";

        List<String> lore = Arrays.asList(
                "&7Status: " + status,
                "",
                "&7When enabled:",
                "&f- Party FFA respawns after &e10s"
        );

        return new ItemBuilder("TOTEM_OF_UNDYING")
                .name("&eParty FFA Respawn")
                .lore(lore, player)
                .build();
    }
}
