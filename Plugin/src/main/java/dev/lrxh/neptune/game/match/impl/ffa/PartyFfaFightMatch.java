package dev.lrxh.neptune.game.match.impl.ffa;

import dev.lrxh.neptune.Neptune;
import dev.lrxh.neptune.game.arena.Arena;
import dev.lrxh.neptune.game.kit.Kit;
import dev.lrxh.neptune.game.match.impl.participant.Participant;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.List;

public class PartyFfaFightMatch extends FfaFightMatch {

    private final boolean respawnEnabled;

    public PartyFfaFightMatch(Arena arena, Kit kit, List<Participant> participants, boolean respawnEnabled) {
        super(arena, kit, participants);
        this.respawnEnabled = respawnEnabled;
    }

    @Override
    public void onDeath(Participant participant) {
        if (!respawnEnabled) {
            super.onDeath(participant);
            return;
        }

        if (isEnded()) return;

        hideParticipant(participant);
        participant.setDead(true);
        participant.setLoser(false);

        Player p = participant.getPlayer();
        if (p != null) {
            p.setGameMode(GameMode.SPECTATOR);
        }

        Bukkit.getScheduler().runTaskLater(Neptune.get(), () -> {
            if (isEnded()) return;
            if (participant.isLeft() || participant.isDisconnected()) return;

            Player player = participant.getPlayer();
            if (player == null) return;

            player.setGameMode(GameMode.SURVIVAL);
            player.teleportAsync(getSpawn(participant)).thenAccept(success -> {
                if (!success) return;
                setupPlayer(participant.getPlayerUUID());
                participant.setDead(false);
                showParticipant(participant);
            });

        }, 200L); // 10s
    }
}
