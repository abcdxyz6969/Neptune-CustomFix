package dev.lrxh.neptune.scoreboard;

import dev.lrxh.neptune.Neptune;
import dev.lrxh.neptune.kit.Kit;
import dev.lrxh.neptune.match.Match;
import dev.lrxh.neptune.match.MatchType;
import dev.lrxh.neptune.profile.Profile;
import dev.lrxh.neptune.scoreboard.locale.ScoreboardLocale;
import fr.mrmicky.fastboard.adventure.FastBoard;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ScoreboardAdapter {

    private final Neptune plugin;

    public ScoreboardAdapter(Neptune plugin) {
        this.plugin = plugin;
    }

    public String getTitle(Player player) {
        try {
            Profile profile = plugin.getProfileService().getProfile(player.getUniqueId());
            if (profile == null) return " ";

            Match match = profile.getMatch();
            ScoreboardLocale locale = resolveLocale(profile, match);
            return locale != null ? locale.getTitle(profile) : " ";
        } catch (Exception ignored) {
            return " ";
        }
    }

    public List<String> getLines(Player player, FastBoard board) {
        try {
            Profile profile = plugin.getProfileService().getProfile(player.getUniqueId());
            if (profile == null) {
                return Collections.singletonList(" ");
            }

            Match match = profile.getMatch();
            ScoreboardLocale locale = resolveLocale(profile, match);

            if (locale == null) {
                return Collections.singletonList(" ");
            }

            List<String> lines = locale.getLines(profile);
            return (lines == null || lines.isEmpty())
                    ? Collections.singletonList(" ")
                    : lines;

        } catch (Exception ignored) {
            return Collections.singletonList(" ");
        }
    }

    private ScoreboardLocale resolveLocale(Profile profile, Match match) {
        ScoreboardService service = plugin.getScoreboardService();

        if (match == null) {
            return service.getLocale(ScoreboardLocale.IN_LOBBY);
        }

        Kit kit = match.getKit();
        MatchType type = match.getType();
        boolean teamMatch = match.isTeamMatch();

        try {
            ScoreboardLocale matchLocale = match.getScoreboard(profile);
            if (matchLocale != null && !matchLocale.getLines(profile).isEmpty()) {
                return matchLocale;
            }
        } catch (Exception ignored) {
        }

        if (kit != null) {
            switch (kit.getRule()) {
                case BOXING:
                    return service.getLocale(teamMatch
                            ? ScoreboardLocale.IN_GAME_BOXING_TEAM
                            : ScoreboardLocale.IN_GAME_BOXING);

                case BEDWARS:
                    return service.getLocale(teamMatch
                            ? ScoreboardLocale.IN_GAME_BEDWARS_TEAM
                            : ScoreboardLocale.IN_GAME_BEDWARS);
            }
        }

        switch (type) {
            case FFA:
                return service.getLocale(ScoreboardLocale.IN_GAME_FFA);

            case TEAM:
                return service.getLocale(ScoreboardLocale.IN_GAME_TEAM);

            default:
                return service.getLocale(ScoreboardLocale.IN_GAME);
        }
    }
}
