package dev.lrxh.neptune.game.queue;

import dev.lrxh.neptune.API;
import dev.lrxh.neptune.Neptune;
import dev.lrxh.neptune.configs.impl.MessagesLocale;
import dev.lrxh.neptune.configs.impl.SettingsLocale;
import dev.lrxh.neptune.game.kit.Kit;
import dev.lrxh.neptune.game.kit.data.KitRule;
import dev.lrxh.neptune.game.queue.event.QueueJoinEvent;
import dev.lrxh.neptune.game.queue.event.QueueLeaveEvent;
import dev.lrxh.neptune.profile.data.ProfileState;
import dev.lrxh.neptune.profile.impl.Profile;
import dev.lrxh.neptune.profile.impl.SettingData;
import dev.lrxh.neptune.providers.manager.IService;
import dev.lrxh.neptune.utils.PlayerUtil;
import dev.lrxh.neptune.utils.Replacement;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Getter
public class QueueService extends IService {

    private static QueueService instance;

    private final Map<Kit, Queue<QueueEntry>> kitQueues = new ConcurrentHashMap<>();

    public static QueueService get() {
        if (instance == null) instance = new QueueService();
        return instance;
    }

    public QueueEntry get(UUID uuid) {
        for (Queue<QueueEntry> queue : kitQueues.values()) {
            for (QueueEntry entry : queue) {
                if (entry.getUuid().equals(uuid)) return entry;
            }
        }
        return null;
    }

    public QueueEntry poll(Kit kit) {
        Queue<QueueEntry> queue = kitQueues.get(kit);
        if (queue == null) return null;
        return queue.poll();
    }

    public void add(QueueEntry queueEntry, boolean add) {
        UUID playerUUID = queueEntry.getUuid();
        Kit kit = queueEntry.getKit();

        if (get(playerUUID) != null) return;

        Profile profile = API.getProfile(playerUUID);

        if (profile.getGameData().getParty() != null) return;
        if (kit.is(KitRule.HIDDEN)) return;

        kitQueues.computeIfAbsent(kit, k -> new ConcurrentLinkedQueue<>()).offer(queueEntry);

        if (!profile.hasState(ProfileState.IN_QUEUE)) {
            profile.setState(ProfileState.IN_QUEUE);
        }

        if (add) {
            QueueJoinEvent event = new QueueJoinEvent(queueEntry);
            Bukkit.getScheduler().runTask(Neptune.get(), () -> Bukkit.getPluginManager().callEvent(event));
            if (event.isCancelled()) return;

            kit.addQueue();

            MessagesLocale.QUEUE_JOIN.send(playerUUID,
                    new Replacement("<kit>", kit.getDisplayName()),
                    new Replacement("<maxPing>", String.valueOf(profile.getSettingData().getMaxPing())));
        }
    }

    public void remove(UUID playerUUID, boolean message, boolean event) {
        QueueEntry entry = get(playerUUID);
        if (entry == null) return;

        Kit kit = entry.getKit();
        Queue<QueueEntry> queue = kitQueues.get(kit);
        if (queue != null) {
            queue.remove(entry);
        }

        Profile profile = API.getProfile(playerUUID);
        if (profile != null && profile.hasState(ProfileState.IN_QUEUE)) {
            profile.setState(profile.getGameData().getParty() == null ? ProfileState.IN_LOBBY : ProfileState.IN_PARTY);
        }

        if (event) {
            QueueLeaveEvent leaveEvent = new QueueLeaveEvent(entry);
            Bukkit.getScheduler().runTask(Neptune.get(), () -> Bukkit.getPluginManager().callEvent(leaveEvent));
        }

        if (message) {
            MessagesLocale.QUEUE_LEAVE.send(playerUUID, new Replacement("<kit>", kit.getDisplayName()));
        }

        kit.removeQueue();
    }

    public void clear() {
        kitQueues.clear();
    }

    public int getQueueSize(Kit kit) {
        Queue<QueueEntry> queue = kitQueues.get(kit);
        return queue == null ? 0 : queue.size();
    }

    public boolean canQueue(UUID uuid, Kit kit) {
        Profile profile = API.getProfile(uuid);
        if (profile == null) return false;

        if (!profile.hasState(ProfileState.IN_LOBBY) && !profile.hasState(ProfileState.IN_QUEUE)) return false;
        if (profile.getGameData().getParty() != null) return false;
        if (kit.is(KitRule.HIDDEN)) return false;

        SettingData settingData = profile.getSettingData();
        if (settingData.isQueueNotifications() && kit.isRanked()) {
            return profile.getSettingData().getMinElo() <= profile.getGameData().get(kit).getElo()
                    && profile.getSettingData().getMaxElo() >= profile.getGameData().get(kit).getElo();
        }

        return true;
    }

    @Override
    public void load() {
    }

    @Override
    public void save() {
    }
}
