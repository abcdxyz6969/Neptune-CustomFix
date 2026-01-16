package dev.lrxh.neptune.feature.party.menu.buttons.events;

import dev.lrxh.neptune.Neptune;
import dev.lrxh.neptune.configs.impl.MenusLocale;
import dev.lrxh.neptune.feature.party.Party;
import dev.lrxh.neptune.feature.party.impl.EventType;
import dev.lrxh.neptune.game.kit.Kit;
import dev.lrxh.neptune.game.match.MatchService;
import dev.lrxh.neptune.game.match.impl.ffa.PartyFfaFightMatch;
import dev.lrxh.neptune.game.match.impl.participant.Participant;
import dev.lrxh.neptune.utils.CC;
import dev.lrxh.neptune.utils.ItemBuilder;
import dev.lrxh.neptune.utils.menu.Button;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PartyTeamKitButton extends Button {
    private final Party party;
    private final Kit kit;
    private final EventType eventType;

    public PartyTeamKitButton(int slot, Party party, Kit kit, EventType eventType) {
        super(slot, false);
        this.party = party;
        this.kit = kit;
        this.eventType = eventType;
    }

    @Override
    public void onClick(ClickType type, Player player) {
        List<Participant> participants = new ArrayList<>();
        for (UUID uuid : party.getUsers()) {
            Player user = Bukkit.getPlayer(uuid);
            if (user == null) continue;
            participants.add(new Participant(user));
        }

        if (eventType.equals(EventType.FFA) && party.isFfaRespawnEnabled()) {
            kit.getRandomArena().thenAccept(arena -> {
                if (arena == null) {
                    for (Participant p : participants) p.sendMessage(CC.error("No arenas were found!"));
                    return;
                }
                if (!arena.isSetup() || !arena.isDoneLoading()) {
                    for (Participant p : participants) p.sendMessage(CC.error("Arena wasn't setup up properly! Please contact an admin if you see this."));
                    return;
                }

                Bukkit.getScheduler().runTask(Neptune.get(), () -> {
                    MatchService.get().startMatch(new PartyFfaFightMatch(arena, kit, participants, true));
                });
            });
            return;
        }

        eventType.start(participants, kit);
    }

    @Override
    public ItemStack getItemStack(Player player) {
        return new ItemBuilder(kit.getIcon())
                .name(MenusLocale.PARTY_EVENTS_KIT_SELECT_NAME.getString().replace("<kit>", kit.getDisplayName()))
                .lore(MenusLocale.PARTY_EVENTS_KIT_SELECT_LORE.getStringList(), player)
                .build();
    }
}
