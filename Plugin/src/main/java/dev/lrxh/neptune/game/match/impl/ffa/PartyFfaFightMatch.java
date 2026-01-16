package dev.lrxh.neptune.game.match.impl.ffa;

import dev.lrxh.neptune.API;
import dev.lrxh.neptune.Neptune;
import dev.lrxh.neptune.game.arena.Arena;
import dev.lrxh.neptune.game.kit.Kit;
import dev.lrxh.neptune.game.match.impl.participant.DeathCause;
import dev.lrxh.neptune.game.match.impl.participant.Participant;
import dev.lrxh.neptune.profile.data.ProfileState;
import dev.lrxh.neptune.profile.impl.Profile;
import dev.lrxh.neptune.utils.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PartyFfaFightMatch extends FfaFightMatch {

    private final boolean respawnEnabled;
    private final Map<UUID, Integer> respawnTasks = new HashMap<>();

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
        if (participant == null) return;

        cancelRespawn(participant.getPlayerUUID());

        hideParticipant(participant);
        participant.setDead(true);
        participant.setLoser(false);

        Player p = participant.getPlayer();
        if (p != null) {
            p.setGameMode(GameMode.SPECTATOR);
        }

        final UUID uuid = participant.getPlayerUUID();
        final int taskId = Bukkit.getScheduler().runTaskTimer(Neptune.get(), new Runnable() {
            int seconds = 10;

            @Override
            public void run() {
                if (isEnded() || participant.isLeft() || participant.isDisconnected()) {
                    cancelRespawn(uuid);
                    return;
                }

                Player player = participant.getPlayer();
                if (player == null) {
                    cancelRespawn(uuid);
                    return;
                }

                if (seconds <= 0) {
                    player.setGameMode(GameMode.SURVIVAL);
                    player.teleportAsync(getSpawn(participant)).thenAccept(success -> {
                        if (!success || isEnded() || participant.isLeft() || participant.isDisconnected()) return;

                        setupPlayer(uuid);
                        participant.setDead(false);
                        showParticipant(participant);
                    });

                    cancelRespawn(uuid);
                    return;
                }

                player.sendTitle(String.valueOf(seconds), "/leave to leave.", 0, 22, 0);
                seconds--;
            }
        }, 0L, 20L).getTaskId();

        respawnTasks.put(uuid, taskId);
    }

    @Override
    public void onLeave(Participant participant, boolean quit) {
        if (isEnded()) return;
        if (participant == null) return;

        UUID uuid = participant.getPlayerUUID();
        cancelRespawn(uuid);

        participant.setDeathCause(DeathCause.DISCONNECT);
        Profile profile = API.getProfile(uuid);

        if (quit) {
            participant.setDisconnected(true);
        } else {
            participant.setLeft(true);

            Player p = participant.getPlayer();
            if (p != null) {
                p.setGameMode(GameMode.SURVIVAL);
            }

            PlayerUtil.reset(p);
            PlayerUtil.teleportToSpawn(uuid);

            if (profile != null) {
                profile.setState(profile.getGameData().getParty() == null ? ProfileState.IN_LOBBY : ProfileState.IN_PARTY);
                profile.setMatch(null);
            }
        }

        // IMPORTANT: gọi logic gốc để end match đúng khi còn 1 người
        super.onDeath(participant);
    }

    private void cancelRespawn(UUID uuid) {
        Integer taskId = respawnTasks.remove(uuid);
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }
}
