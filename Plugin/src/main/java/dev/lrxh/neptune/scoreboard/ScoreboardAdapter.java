package dev.lrxh.neptune.scoreboard;

import dev.lrxh.neptune.API;
import dev.lrxh.neptune.configs.impl.ScoreboardLocale;
import dev.lrxh.neptune.game.kit.impl.KitRule;
import dev.lrxh.neptune.game.match.Match;
import dev.lrxh.neptune.game.match.impl.ffa.FfaFightMatch;
import dev.lrxh.neptune.game.match.impl.solo.SoloFightMatch;
import dev.lrxh.neptune.game.match.impl.team.TeamFightMatch;
import dev.lrxh.neptune.profile.impl.Profile;
import dev.lrxh.neptune.providers.placeholder.PlaceholderUtil;
import fr.mrmicky.fastboard.FastAdapter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScoreboardAdapter implements FastAdapter {

    @Override
    public String getTitle(Player player) {
        String raw = getAnimatedText();
        if (raw == null || raw.isEmpty()) raw = "Neptune";
        try {
            String formatted = PlaceholderUtil.format(raw, player);
            return (formatted == null || formatted.isEmpty()) ? raw : formatted;
        } catch (Throwable ignored) {
            return raw;
        }
    }

    @Override
    public List<String> getLines(Player player) {
        Profile profile = API.getProfile(player);
        if (profile == null) return Collections.singletonList(" ");

        Match match;

        switch (profile.getState()) {
            case IN_LOBBY:
                return fmt(safeLocaleList(ScoreboardLocale.LOBBY), player);

            case IN_KIT_EDITOR:
                return fmt(safeLocaleList(ScoreboardLocale.KIT_EDITOR), player);

            case IN_PARTY:
                return fmt(safeLocaleList(ScoreboardLocale.PARTY_LOBBY), player);

            case IN_QUEUE:
                return fmt(safeLocaleList(ScoreboardLocale.IN_QUEUE), player);

            case IN_GAME: {
                match = profile.getMatch();
                if (match == null) {
                    return fmt(safeLocaleList(ScoreboardLocale.LOBBY), player);
                }

                List<String> lines = null;
                try {
                    lines = match.getScoreboard(player.getUniqueId());
                } catch (Throwable ignored) {
                }

                List<String> matchLines = safeFormat(lines, player);
                if (!matchLines.isEmpty()) return matchLines;

                List<String> fallback = buildInGameFallback(match, player);
                if (fallback != null && !fallback.isEmpty()) {
                    return fallback;
                }

                return fmt(safeLocaleList(ScoreboardLocale.IN_GAME), player);
            }

            case IN_SPECTATOR:
                match = profile.getMatch();
                if (match == null) {
                    return fmt(safeLocaleList(ScoreboardLocale.IN_SPECTATOR), player);
                }

                if (match instanceof SoloFightMatch) {
                    if (match.getKit().is(KitRule.BED_WARS)) {
                        return fmt(safeLocaleList(ScoreboardLocale.IN_SPECTATOR_BEDWARS), player);
                    }
                    return fmt(safeLocaleList(ScoreboardLocale.IN_SPECTATOR), player);
                } else if (match instanceof TeamFightMatch) {
                    if (match.getKit().is(KitRule.BED_WARS)) {
                        return fmt(safeLocaleList(ScoreboardLocale.IN_SPECTATOR_BEDWARS), player);
                    }
                    return fmt(safeLocaleList(ScoreboardLocale.IN_SPECTATOR_TEAM), player);
                } else if (match instanceof FfaFightMatch) {
                    return fmt(safeLocaleList(ScoreboardLocale.IN_SPECTATOR_FFA), player);
                }

                return fmt(safeLocaleList(ScoreboardLocale.IN_SPECTATOR), player);

            case IN_CUSTOM:
                try {
                    return fmt(ScoreboardService.get().getScoreboardLines(profile.getCustomState(), profile), player);
                } catch (Throwable ignored) {
                    return Collections.singletonList(" ");
                }

            default:
                return Collections.singletonList(" ");
        }
    }

    private List<String> buildInGameFallback(Match match, Player player) {
        if (match.getKit() != null) {
            if (match.getKit().is(KitRule.BOXING)) {
                if (match instanceof TeamFightMatch) {
                    return fmt(safeLocaleList(ScoreboardLocale.IN_GAME_BOXING_TEAM), player);
                }
                return fmt(safeLocaleList(ScoreboardLocale.IN_GAME_BOXING), player);
            }

            if (match.getKit().is(KitRule.BED_WARS)) {
                if (match instanceof TeamFightMatch) {
                    return fmt(safeLocaleList(ScoreboardLocale.IN_GAME_BEDWARS_TEAM), player);
                }
                return fmt(safeLocaleList(ScoreboardLocale.IN_GAME_BEDWARS), player);
            }
        }

        if (match instanceof TeamFightMatch) {
            return fmt(safeLocaleList(ScoreboardLocale.IN_GAME_TEAM), player);
        }

        if (match instanceof FfaFightMatch) {
            return fmt(safeLocaleList(ScoreboardLocale.IN_GAME_FFA), player);
        }

        return fmt(safeLocaleList(ScoreboardLocale.IN_GAME), player);
    }

    private List<String> fmt(List<String> lines, Player player) {
        List<String> formatted = safeFormat(lines, player);
        return formatted.isEmpty() ? Collections.singletonList(" ") : formatted;
    }

    private List<String> safeFormat(List<String> lines, Player player) {
        if (lines == null || lines.isEmpty()) return Collections.emptyList();
        try {
            List<String> formatted = PlaceholderUtil.format(new ArrayList<>(lines), player);
            return formatted == null ? Collections.emptyList() : formatted;
        } catch (Throwable ignored) {
            return Collections.emptyList();
        }
    }

    private List<String> safeLocaleList(ScoreboardLocale locale) {
        List<String> list = locale.getStringList();
        if (list == null || list.isEmpty()) {
            List<String> def = locale.getDefaultValue();
            return def == null ? Collections.emptyList() : def;
        }
        return list;
    }

    private String getAnimatedText() {
        List<String> titles = safeLocaleList(ScoreboardLocale.TITLE);
        if (titles.isEmpty()) return "Neptune";
        int interval = ScoreboardLocale.UPDATE_INTERVAL.getInt();
        if (interval <= 0) interval = 300;
        int index = (int) ((System.currentTimeMillis() / interval) % titles.size());
        String t = titles.get(index);
        return (t == null || t.isEmpty()) ? "Neptune" : t;
    }
}
