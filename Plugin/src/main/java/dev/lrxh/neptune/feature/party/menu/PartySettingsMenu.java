package dev.lrxh.neptune.feature.party.menu;

import dev.lrxh.neptune.configs.impl.MenusLocale;
import dev.lrxh.neptune.feature.party.Party;
import dev.lrxh.neptune.feature.party.menu.buttons.settings.PartyAdvertisementButton;
import dev.lrxh.neptune.feature.party.menu.buttons.settings.PartyLimitButton;
import dev.lrxh.neptune.feature.party.menu.buttons.settings.PartyPrivacyButton;
import dev.lrxh.neptune.feature.party.menu.buttons.settings.PartyRespawnButton;
import dev.lrxh.neptune.feature.party.menu.buttons.settings.PartyGlowPlayersButton;
import dev.lrxh.neptune.utils.menu.Button;
import dev.lrxh.neptune.utils.menu.Filter;
import dev.lrxh.neptune.utils.menu.Menu;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PartySettingsMenu extends Menu {
    private final Party party;

    public PartySettingsMenu(Party party) {
        super(MenusLocale.PARTY_SETTINGS_TITLE.getString(), MenusLocale.PARTY_SETTINGS_SIZE.getInt(),
                Filter.valueOf(MenusLocale.PARTY_SETTINGS_FILTER.getString()), true);
        this.party = party;
        setUpdateEveryTick(true);
    }

    @Override
    public List<Button> getButtons(Player player) {
        List<Button> buttons = new ArrayList<>();
        buttons.add(new PartyPrivacyButton(MenusLocale.PARTY_SETTINGS_PRIVACY_SLOT.getInt(), party));
        buttons.add(new PartyLimitButton(MenusLocale.PARTY_SETTINGS_MAX_SIZE_SLOT.getInt(), party));
        buttons.add(new PartyAdvertisementButton(MenusLocale.PARTY_SETTINGS_ADVERTISEMENTS_SLOT.getInt(), party));

        // slot bạn tự chọn (đừng đè slot khác). Ví dụ: max size + 2
        buttons.add(new PartyRespawnButton(MenusLocale.PARTY_SETTINGS_MAX_SIZE_SLOT.getInt() + 2, party));
        buttons.add(new PartyGlowPlayersButton(
                 MenusLocale.PARTY_SETTINGS_MAX_SIZE_SLOT.getInt() + 3, party
        ));

        return buttons;
    }
}
