package dev.lrxh.neptune.game.arena.menu;

import dev.lrxh.neptune.game.arena.Arena;
import dev.lrxh.neptune.game.arena.ArenaService;
import dev.lrxh.neptune.game.arena.menu.button.ArenaStatsButton;
import dev.lrxh.neptune.utils.CC;
import dev.lrxh.neptune.utils.menu.Button;
import dev.lrxh.neptune.utils.menu.Filter;
import dev.lrxh.neptune.utils.menu.Menu;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ArenaStatsMenu extends Menu {

    public ArenaStatsMenu() {
        super(CC.color("&bArena Stats &7(All Maps)").content(), 54, Filter.NONE);
    }

    @Override
    public List<Button> getButtons(Player player) {
        List<Button> list = new ArrayList<>();

        int slot = 0;
        for (Arena arena : ArenaService.get().getArenas()) {
            if (arena == null) continue;
            if (slot >= 54) break;

            list.add(new ArenaStatsButton(slot, arena));
            slot++;
        }

        return list;
    }
}
