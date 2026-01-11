package dev.lrxh.neptune.feature.queue.tasks;

import dev.lrxh.neptune.API;
import dev.lrxh.neptune.Neptune;
import dev.lrxh.neptune.configs.impl.MessagesLocale;
import dev.lrxh.neptune.feature.queue.QueueEntry;
import dev.lrxh.neptune.feature.queue.QueueService;
import dev.lrxh.neptune.game.kit.Kit;
import dev.lrxh.neptune.game.kit.data.KitRule;
import dev.lrxh.neptune.game.match.MatchService;
import dev.lrxh.neptune.game.match.impl.participant.Participant;
import dev.lrxh.neptune.profile.data.ProfileState;
import dev.lrxh.neptune.profile.impl.Profile;
import dev.lrxh.neptune.profile.impl.SettingData;
import dev.lrxh.neptune.utils.CC;
import dev.lrxh.neptune.utils.PlayerUtil;
import dev.lrxh.neptune.utils.Replacement;
import dev.lrxh.neptune.utils.tasks.NeptuneRunnable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class QueueCheckTask extends NeptuneRunnable {

    @Override
    public void run() {
        for (Kit kit : API.getKits()) {
            if (kit == null) continue;

            if (QueueService.get().getQueueSize(kit) < 2) continue;

            QueueEntry queueEntry1 = QueueService.get().poll(kit);
            QueueEntry queueEntry2 = QueueService.get().poll(kit);

            if (queueEntry1 == null || queueEntry2 == null) continue;

            if (!queueEntry1.getKit().equals(queueEntry2.getKit())) {
                QueueService.get().add(queueEntry1, false);
                QueueService.get().add(queueEntry2, false);
                continue;
            }

            Player player1 = Bukkit.getPlayer(queueEntry1.getUuid());
            Player player2 = Bukkit.getPlayer(queueEntry2.getUuid());

            if (player1 == null || player2 == null) {
                continue;
            }

            Profile profile1 = API.getProfile(queueEntry1.getUuid());
            Profile profile2 = API.getProfile(queueEntry2.getUuid());

            if (profile1 == null || profile2 == null) {
                continue;
            }

            if (!profile1.hasState(ProfileState.IN_QUEUE) || !profile2.hasState(ProfileState.IN_QUEUE)) {
                continue;
            }

            SettingData settings1 = profile1.getSettingData();
            SettingData settings2 = profile2.getSettingData();

            int ping1 = PlayerUtil.getPing(queueEntry1.getUuid());
            int ping2 = PlayerUtil.getPing(queueEntry2.getUuid());

            if (!(ping2 <= settings1.getMaxPing() && ping1 <= settings2.getMaxPing())) {
                QueueService.get().add(queueEntry1, false);
                QueueService.get().add(queueEntry2, false);
                continue;
            }

            kit.getRandomArena().thenAccept(arena -> {
                if (arena == null) {
                    PlayerUtil.sendMessage(queueEntry1.getUuid(), CC.error("No valid arena was found for this kit!"));
                    PlayerUtil.sendMessage(queueEntry2.getUuid(), CC.error("No valid arena was found for this kit!"));

                    QueueService.get().add(queueEntry1, false);
                    QueueService.get().add(queueEntry2, false);
                    return;
                }

                Participant participant1 = new Participant(player1);
                Participant participant2 = new Participant(player2);

                MessagesLocale.MATCH_FOUND.send(queueEntry1.getUuid(),
                        new Replacement("<opponent>", participant2.getNameUnColored()),
                        new Replacement("<kit>", kit.getDisplayName()),
                        new Replacement("<arena>", arena.getDisplayName()),
                        new Replacement("<opponent-ping>", String.valueOf(ping2)),
                        new Replacement("<opponent-elo>", String.valueOf(profile2.getGameData().get(kit).getElo())),
                        new Replacement("<elo>", String.valueOf(profile1.getGameData().get(kit).getElo())),
                        new Replacement("<ping>", String.valueOf(ping1)));

                MessagesLocale.MATCH_FOUND.send(queueEntry2.getUuid(),
                        new Replacement("<opponent>", participant1.getNameUnColored()),
                        new Replacement("<kit>", kit.getDisplayName()),
                        new Replacement("<arena>", arena.getDisplayName()),
                        new Replacement("<opponent-ping>", String.valueOf(ping1)),
                        new Replacement("<opponent-elo>", String.valueOf(profile1.getGameData().get(kit).getElo())),
                        new Replacement("<elo>", String.valueOf(profile2.getGameData().get(kit).getElo())),
                        new Replacement("<ping>", String.valueOf(ping2)));

                Bukkit.getScheduler().runTask(Neptune.get(), () -> {
                    profile1.setState(ProfileState.IN_LOBBY);
                    profile2.setState(ProfileState.IN_LOBBY);

                    MatchService.get().startMatch(participant1, participant2, kit, arena, false,
                            kit.is(KitRule.BEST_OF_THREE) ? 3 : 1);
                });
            });
        }
    }
}
