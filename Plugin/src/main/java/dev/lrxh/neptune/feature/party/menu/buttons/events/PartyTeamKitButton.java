package dev.lrxh.neptune.feature.party.menu.buttons.events;

import dev.lrxh.neptune.Neptune;
import dev.lrxh.neptune.configs.impl.MenusLocale;
import dev.lrxh.neptune.feature.party.Party;
import dev.lrxh.neptune.feature.party.impl.EventType;
import dev.lrxh.neptune.game.kit.Kit;
import dev.lrxh.neptune.game.match.MatchService;
import dev.lrxh.neptune.game.match.impl.ffa.PartyFfaFightMatch;
import dev.lrxh.neptune.game.match.impl.participant.Participant;
import dev.lrxh.neptune.game.match.impl.participant.ParticipantColor;
import dev.lrxh.neptune.game.match.impl.team.MatchTeam;
import dev.lrxh.neptune.game.match.impl.team.PartyTeamFightMatch;
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

        // Party FFA Respawn mode
        if (eventType == EventType.FFA && party.isFfaRespawnEnabled()) {

            // IMPORTANT: set colors to avoid NPE in teleportToPositions()
            for (int i = 0; i < participants.size(); i++) {
                participants.get(i).setColor((i % 2 == 0) ? ParticipantColor.RED : ParticipantColor.BLUE);
            }

            kit.getRandomArena().thenAccept(arena -> {
                if (arena == null) return;
                if (!arena.isSetup() || !arena.isDoneLoading()) return;

                Bukkit.getScheduler().runTask(Neptune.get(), () -> {
                    MatchService.get().startMatch(new PartyFfaFightMatch(arena, kit, participants, true));
                });
            });

            return;
        }

        // Party Split (TEAM) with Glow Teams
        if (eventType == EventType.TEAM) {

            // split members into 2 teams
            List<Participant> teamAPlayers = new ArrayList<>();
            List<Participant> teamBPlayers = new ArrayList<>();

            for (int i = 0; i < participants.size(); i++) {
                Participant p = participants.get(i);
                if (i % 2 == 0) {
                    p.setColor(ParticipantColor.RED);
                    teamAPlayers.add(p);
                } else {
                    p.setColor(ParticipantColor.BLUE);
                    teamBPlayers.add(p);
                }
            }

            MatchTeam teamA = new MatchTeam(teamAPlayers);
            MatchTeam teamB = new MatchTeam(teamBPlayers);
            teamA.setOpponentTeam(teamB);
            teamB.setOpponentTeam(teamA);

            kit.getRandomArena().thenAccept(arena -> {
                if (arena == null) return;
                if (!arena.isSetup() || !arena.isDoneLoading()) return;

                Bukkit.getScheduler().runTask(Neptune.get(), () -> {
                    MatchService.get().startMatch(
                            new PartyTeamFightMatch(
                                    arena,
                                    kit,
                                    participants,
                                    teamA,
                                    teamB,
                                    party
                            )
                    );
                });
            });

            return;
        }

        // Default behavior
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
