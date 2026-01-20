package dev.lrxh.neptune.game.match.impl.team;

import dev.lrxh.neptune.feature.party.Party;
import dev.lrxh.neptune.game.arena.Arena;
import dev.lrxh.neptune.game.kit.Kit;
import dev.lrxh.neptune.game.match.impl.participant.Participant;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PartyTeamFightMatch extends TeamFightMatch {

    private final Party party;
    private final Map<UUID, Scoreboard> oldBoards = new HashMap<>();
    private Scoreboard glowBoard;

    public PartyTeamFightMatch(
            Arena arena,
            Kit kit,
            List<Participant> participants,
            MatchTeam teamA,
            MatchTeam teamB,
            Party party
    ) {
        super(arena, kit, participants, teamA, teamB);
        this.party = party;
    }

    @Override
    public void startMatch() {
        super.startMatch();

        if (party != null && party.isGlowPlayersEnabled()) {
            applyGlow();
        }
    }

    @Override
    public void end(Participant loser) {
        clearGlow();
        super.end(loser);
    }

    @Override
    public void onLeave(Participant participant, boolean quit) {
        clearGlow();
        super.onLeave(participant, quit);
    }

    /* ===================== GLOW LOGIC ===================== */

    private void applyGlow() {
        glowBoard = Bukkit.getScoreboardManager().getNewScoreboard();

        Team red = glowBoard.registerNewTeam("party_red");
        red.setColor(org.bukkit.ChatColor.RED);

        Team blue = glowBoard.registerNewTeam("party_blue");
        blue.setColor(org.bukkit.ChatColor.BLUE);

        for (Participant participant : getParticipants()) {
            Player player = participant.getPlayer();
            if (player == null) continue;

            oldBoards.put(participant.getPlayerUUID(), player.getScoreboard());

            if (getParticipantTeam(participant).equals(getTeamA())) {
                red.addEntry(player.getName());
            } else {
                blue.addEntry(player.getName());
            }

            player.setScoreboard(glowBoard);
            player.setGlowing(true);
        }
    }

    private void clearGlow() {
        if (glowBoard == null) return;

        for (Participant participant : getParticipants()) {
            Player player = participant.getPlayer();
            if (player == null) continue;

            player.setGlowing(false);

            Scoreboard old = oldBoards.get(participant.getPlayerUUID());
            if (old != null) {
                player.setScoreboard(old);
            }
        }

        oldBoards.clear();
        glowBoard = null;
    }
}
