package dev.lrxh.neptune.game.match.impl.ffa;

import dev.lrxh.neptune.Neptune;
import dev.lrxh.neptune.game.arena.Arena;
import dev.lrxh.neptune.game.kit.Kit;
import dev.lrxh.neptune.game.match.MatchService;
import dev.lrxh.neptune.game.match.impl.participant.Participant;
import dev.lrxh.neptune.utils.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;

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
        if (!MatchService.get().matches.contains(this)) return;

        hideParticipant(participant);
        participant.setDead(true);
        participant.setLoser(false);

        if (participant.getPlayer() != null) {
            PlayerUtil.reset(participant.getPlayer());
            participant.getPlayer().setGameMode(GameMode.SPECTATOR);
        }

        Bukkit.getScheduler().runTaskTimer(Neptune.get(), new Runnable() {
            int timer = 10;

            @Override
            public void run() {
                if (isEnded() || participant.isLeft() || participant.isDisconnected() || !MatchService.get().matches.contains(PartyFfaFightMatch.this)) {
                    Bukkit.getScheduler().cancelTask(taskId);
                    return;
                }

                if (timer <= 0) {
                    if (participant.getPlayer() != null) {
                        participant.getPlayer().setGameMode(GameMode.SURVIVAL);
                    }

                    participant.teleport(getArena().getRedSpawn());
                    setupPlayer(participant.getPlayerUUID());
                    participant.setDead(false);
                    showParticipant(participant);

                    Bukkit.getScheduler().cancelTask(taskId);
                    return;
                }

                timer--;
            }

            int taskId;
            Runnable init() { return this; }
        }.init(), 0L, 20L);
    }
}
