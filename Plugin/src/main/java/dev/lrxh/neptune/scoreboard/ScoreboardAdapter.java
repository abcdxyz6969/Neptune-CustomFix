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
                    return fromLocale(ScoreboardLocale.LOBBY, player);

                case IN_KIT_EDITOR:
                    return fromLocale(ScoreboardLocale.KIT_EDITOR, player);

                case IN_PARTY:
                    return fromLocale(ScoreboardLocale.PARTY_LOBBY, player);

                case IN_QUEUE:
                    return fromLocale(ScoreboardLocale.IN_QUEUE, player);

                case IN_GAME: {
                    match = profile.getMatch();
                    if (match == null) {
                        return fromLocale(ScoreboardLocale.LOBBY, player);
                    }

                    List<String> lines = null;
                    try {
                        lines = match.getScoreboard(player.getUniqueId());
                    } catch (Throwable ignored) {
                    }

                    List<String> formattedMatch = safeFormat(lines, player);
                    if (!isEmpty(formattedMatch)) {
                        return formattedMatch;
                    }

                    List<String> fallback = buildInGameFallback(match, player);
                    if (!isEmpty(fallback)) {
                        return fallback;
                    }

                    // FIX: không trả LOBBY ở đây nữa
                    return fromLocale(ScoreboardLocale.IN_GAME, player);
                }

                case IN_SPECTATOR:
                    match = profile.getMatch();
                    if (match == null) {
                        return fromLocale(ScoreboardLocale.IN_SPECTATOR, player);
                    }

                    if (match instanceof SoloFightMatch) {
                        if (match.getKit().is(KitRule.BED_WARS)) {
                            return fromLocale(ScoreboardLocale.IN_SPECTATOR_BEDWARS, player);
                        }
                        return fromLocale(ScoreboardLocale.IN_SPECTATOR, player);
                    } else if (match instanceof TeamFightMatch) {
                        if (match.getKit().is(KitRule.BED_WARS)) {
                            return fromLocale(ScoreboardLocale.IN_SPECTATOR_BEDWARS, player);
                        }
                        return fromLocale(ScoreboardLocale.IN_SPECTATOR_TEAM, player);
                    } else if (match instanceof FfaFightMatch) {
                        return fromLocale(ScoreboardLocale.IN_SPECTATOR_FFA, player);
                    }

                    return fromLocale(ScoreboardLocale.IN_SPECTATOR, player);

                case IN_CUSTOM:
                    try {
                        List<String> custom = ScoreboardService.get().getScoreboardLines(profile.getCustomState(), profile);
                        List<String> formatted = safeFormat(custom, player);
                        return isEmpty(formatted) ? Collections.singletonList(" ") : formatted;
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
                    return fromLocale(ScoreboardLocale.IN_GAME_BOXING_TEAM, player);
                }
                return fromLocale(ScoreboardLocale.IN_GAME_BOXING, player);
            }

            if (match.getKit().is(KitRule.BED_WARS)) {
                if (match instanceof TeamFightMatch) {
                    return fromLocale(ScoreboardLocale.IN_GAME_BEDWARS_TEAM, player);
                }
                return fromLocale(ScoreboardLocale.IN_GAME_BEDWARS, player);
            }
        }

        if (match instanceof TeamFightMatch) {
            return fromLocale(ScoreboardLocale.IN_GAME_TEAM, player);
        }

        if (match instanceof FfaFightMatch) {
            return fromLocale(ScoreboardLocale.IN_GAME_FFA, player);
        }

        return fromLocale(ScoreboardLocale.IN_GAME, player);
    }

    private List<String> fromLocale(ScoreboardLocale locale, Player player) {
        List<String> raw = locale.getStringList();

        // quan trọng: nếu config trả rỗng, dùng defaultValue trong enum
        if (raw == null || raw.isEmpty()) {
            Object def = locale.getDefaultValue();
            if (def instanceof List) {
                //noinspection unchecked
                raw = (List<String>) def;
            }
        }

        List<String> formatted = safeFormat(raw, player);
        return isEmpty(formatted) ? Collections.singletonList(" ") : formatted;
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

    private boolean isEmpty(List<String> lines) {
        return lines == null || lines.isEmpty();
    }

    private String getAnimatedText() {
        List<String> titles = ScoreboardLocale.TITLE.getStringList();
        if (titles == null || titles.isEmpty()) {
            Object def = ScoreboardLocale.TITLE.getDefaultValue();
            if (def instanceof List) {
                //noinspection unchecked
                titles = (List<String>) def;
            }
        }

        if (titles == null || titles.isEmpty()) return "Neptune";

        int interval = ScoreboardLocale.UPDATE_INTERVAL.getInt();
        if (interval <= 0) interval = 300;

        int index = (int) ((System.currentTimeMillis() / interval) % titles.size());
        String t = titles.get(index);
        return (t == null || t.isEmpty()) ? "Neptune" : t;
    }
}
