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
        try {
            Profile profile = API.getProfile(player);
            if (profile == null) return Collections.singletonList(" ");

            Match match;

            switch (profile.getState()) {
                case IN_LOBBY:
                    return fmt(ScoreboardLocale.LOBBY.getStringList(), player);

                case IN_KIT_EDITOR:
                    return fmt(ScoreboardLocale.KIT_EDITOR.getStringList(), player);

                case IN_PARTY:
                    return fmt(ScoreboardLocale.PARTY_LOBBY.getStringList(), player);

                case IN_QUEUE:
                    return fmt(ScoreboardLocale.IN_QUEUE.getStringList(), player);

                case IN_GAME: {
                    match = profile.getMatch();
                    if (match == null) {
                        return fmt(ScoreboardLocale.LOBBY.getStringList(), player);
                    }

                    List<String> lines = null;
                    try {
                        lines = match.getScoreboard(player.getUniqueId());
                    } catch (Throwable ignored) {
                    }

                    if (lines != null && !lines.isEmpty()) {
                        try {
                            List<String> formatted = PlaceholderUtil.format(new ArrayList<>(lines), player);
                            if (formatted != null && !formatted.isEmpty()) {
                                return formatted;
                            }
                        } catch (Throwable ignored) {
                        }
                    }

                    List<String> fallback = buildInGameFallback(match, player);
                    if (fallback != null && !fallback.isEmpty()) {
                        return fallback;
                    }

                    return fmt(ScoreboardLocale.IN_GAME.getStringList(), player);
                }

                case IN_SPECTATOR:
                    match = profile.getMatch();
                    if (match == null) {
                        return fmt(ScoreboardLocale.IN_SPECTATOR.getStringList(), player);
                    }

                    if (match instanceof SoloFightMatch) {
                        if (match.getKit().is(KitRule.BED_WARS)) {
                            return fmt(ScoreboardLocale.IN_SPECTATOR_BEDWARS.getStringList(), player);
                        }
                        return fmt(ScoreboardLocale.IN_SPECTATOR.getStringList(), player);
                    } else if (match instanceof TeamFightMatch) {
                        if (match.getKit().is(KitRule.BED_WARS)) {
                            return fmt(ScoreboardLocale.IN_SPECTATOR_BEDWARS.getStringList(), player);
                        }
                        return fmt(ScoreboardLocale.IN_SPECTATOR_TEAM.getStringList(), player);
                    } else if (match instanceof FfaFightMatch) {
                        return fmt(ScoreboardLocale.IN_SPECTATOR_FFA.getStringList(), player);
                    }

                    return fmt(ScoreboardLocale.IN_SPECTATOR.getStringList(), player);

                case IN_CUSTOM:
                    try {
                        List<String> custom = ScoreboardService.get().getScoreboardLines(profile.getCustomState(), profile);
                        return safeLines(custom, player);
                    } catch (Throwable ignored) {
                        return Collections.singletonList(" ");
                    }

                default:
                    return Collections.singletonList(" ");
            }
        } catch (Throwable ignored) {
            return Collections.singletonList(" ");
        }
    }

    private List<String> buildInGameFallback(Match match, Player player) {
        if (match.getKit() != null) {
            if (match.getKit().is(KitRule.BOXING)) {
                if (match instanceof TeamFightMatch) {
                    return fmt(ScoreboardLocale.IN_GAME_BOXING_TEAM.getStringList(), player);
                }
                return fmt(ScoreboardLocale.IN_GAME_BOXING.getStringList(), player);
            }

            if (match.getKit().is(KitRule.BED_WARS)) {
                if (match instanceof TeamFightMatch) {
                    return fmt(ScoreboardLocale.IN_GAME_BEDWARS_TEAM.getStringList(), player);
                }
                return fmt(ScoreboardLocale.IN_GAME_BEDWARS.getStringList(), player);
            }
        }

        if (match instanceof TeamFightMatch) {
            return fmt(ScoreboardLocale.IN_GAME_TEAM.getStringList(), player);
        }

        if (match instanceof FfaFightMatch) {
            return fmt(ScoreboardLocale.IN_GAME_FFA.getStringList(), player);
        }

        return fmt(ScoreboardLocale.IN_GAME.getStringList(), player);
    }

    private List<String> safeLines(List<String> lines, Player player) {
        if (lines == null || lines.isEmpty()) return Collections.singletonList(" ");
        try {
            List<String> formatted = PlaceholderUtil.format(new ArrayList<>(lines), player);
            return (formatted == null || formatted.isEmpty()) ? Collections.singletonList(" ") : formatted;
        } catch (Throwable ignored) {
            return Collections.singletonList(" ");
        }
    }

    private List<String> fmt(List<String> lines, Player player) {
        return safeLines(lines, player);
    }

    private String getAnimatedText() {
        List<String> titles = ScoreboardLocale.TITLE.getStringList();
        if (titles == null || titles.isEmpty()) return "Neptune";

        int interval = ScoreboardLocale.UPDATE_INTERVAL.getInt();
        if (interval <= 0) interval = 300;

        int index = (int) ((System.currentTimeMillis() / interval) % titles.size());
        String t = titles.get(index);
        return (t == null || t.isEmpty()) ? "Neptune" : t;
    }
}
