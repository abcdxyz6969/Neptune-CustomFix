package dev.lrxh.neptune.commands;

import com.jonahseguin.drink.annotation.Command;
import com.jonahseguin.drink.annotation.Sender;
import dev.lrxh.neptune.game.arena.menu.ArenaStatsMenu;
import org.bukkit.entity.Player;

public class ArenaStatsCommand {

    @Command(name = "arenastats", desc = "")
    public void arenastats(@Sender Player player) {
        new ArenaStatsMenu().open(player);
    }
}
