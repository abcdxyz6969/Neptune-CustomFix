package dev.lrxh.neptune.game.arena.menu;

import dev.lrxh.neptune.game.arena.Arena;
import dev.lrxh.neptune.game.arena.ArenaService;
import dev.lrxh.neptune.game.arena.menu.button.ArenaStatsButton;
import dev.lrxh.neptune.utils.CC;
import dev.lrxh.neptune.utils.menu.Menu;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ArenaStatsMenu extends Menu {

    public ArenaStatsMenu() {
        setAutoUpdate(true);
        setUpdateAfterClick(true);
    }

    @Override
    public String getTitle(Player player) {
        return CC.color("&bArena Stats &7(All Maps)");
    }

    @Override
    public int getSize() {
        return 54;
    }

    @Override
    public void update(Player player) {
        this.buttons.clear();

        List<Arena> arenas = new ArrayList<>(ArenaService.get().getArenas());
        int slot = 0;

        for (Arena arena : arenas) {
            if (arena == null) continue;
            if (slot >= 54) break;

            buttons.put(slot, new ArenaStatsButton(slot, arena));
            slot++;
        }
    }
}
