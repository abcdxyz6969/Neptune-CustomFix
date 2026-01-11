package dev.lrxh.neptune.game.arena.menu;

import dev.lrxh.neptune.game.arena.Arena;
import dev.lrxh.neptune.game.arena.ArenaService;
import dev.lrxh.neptune.game.arena.menu.button.ArenaStatsButton;
import dev.lrxh.neptune.utils.menu.Button;
import dev.lrxh.neptune.utils.menu.Filter;
import dev.lrxh.neptune.utils.menu.Menu;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ArenaStatsMenu extends Menu {

    public ArenaStatsMenu() {
        super("&eArena Stats", 54, Filter.NONE);
    }

    @Override
    public List<Button> getButtons(Player player) {
        List<Button> buttons = new ArrayList<>();

        List<Arena> arenas = new ArrayList<>(ArenaService.get().arenas);
        arenas.removeIf(a -> a == null || a.getName() == null);

        arenas.sort(Comparator.comparing(a -> a.getName().toLowerCase()));

        int slot = 0;
        for (Arena arena : arenas) {
            if (slot >= 54) break; // 6 rows
            buttons.add(new ArenaStatsButton(slot, arena));
            slot++;
        }

        return buttons;
    }
}
