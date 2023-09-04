package austeretony.oxygen_permissions.client.gui.permissions.management;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.gui.base.Texts;
import austeretony.oxygen_core.client.gui.base.background.Background;
import austeretony.oxygen_core.client.gui.base.button.VerticalSlider;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_core.client.gui.base.list.ScrollableList;
import austeretony.oxygen_core.client.gui.base.special.SectionSwitcher;
import austeretony.oxygen_core.client.gui.base.text.TextField;
import austeretony.oxygen_core.client.gui.base.text.TextLabel;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.player.shared.PlayerSharedData;
import austeretony.oxygen_core.common.sync.SyncMeta;
import austeretony.oxygen_permissions.client.PermissionsManagerClient;
import austeretony.oxygen_permissions.client.gui.permissions.management.context.AddRoleToPlayerContextAction;
import austeretony.oxygen_permissions.client.gui.permissions.management.context.RemoveRoleFromPlayerContextAction;
import austeretony.oxygen_permissions.common.permissions.sync.SyncReason;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayersSection extends Section {

    private ScrollableList<UUID> playersList;
    private TextField usernameField;
    private TextLabel playersAmountLabel;

    public PlayersSection(PermissionsManagementScreen screen) {
        super(screen, localize("oxygen_permissions.gui.permissions_management.players"), true);
    }

    @Override
    public void init() {
        addWidget(new Background.UnderlinedTitle(this));
        addWidget(new TextLabel(4, 12, Texts.title("oxygen_permissions.gui.permissions_management.title")));
        addWidget(new SectionSwitcher(this));

        addWidget(playersAmountLabel = new TextLabel(0, 22, Texts.additionalDark("")));
        addWidget(playersList = new ScrollableList<>(6, 25, 13, getWidth() - 6 * 2 - 3, 10));
        VerticalSlider slider = new VerticalSlider(6 + playersList.getWidth() + 1, 25, 2, 142);
        addWidget(slider);
        playersList.setSlider(slider);
        addWidget(usernameField = new TextField(6, 15, 70, OxygenMain.USERNAME_FIELD_LENGTH));
        playersList.setSearchField(usernameField);

        playersList.createContextMenu(Arrays.asList(
                new AddRoleToPlayerContextAction(),
                new RemoveRoleFromPlayerContextAction()));
    }

    private void initPlayersList() {
        playersList.clear();
        List<PlayerSharedData> sortedData = PermissionsManagerClient.instance().getPlayersSharedData()
                .stream()
                .sorted(Comparator.comparing(PlayerSharedData::getUsername))
                .collect(Collectors.toList());

        for (PlayerSharedData sharedData : sortedData) {
            playersList.addElement(new PlayerListEntry(sharedData));
        }

        String amountStr = sortedData.size() + "/" + OxygenClient.getMaxPlayers();
        playersAmountLabel.getText().setText(amountStr);
        playersAmountLabel.setX(getWidth() - 6 - (int) playersAmountLabel.getText().getWidth());
    }

    protected void dataSynchronized(SyncReason reason, @Nullable SyncMeta meta) {
        switch (reason) {
            case MENU_OPEN:
                initPlayersList();
                break;
            case PLAYER_ROLES_ADDED:
            case PLAYER_ROLES_REMOVED:
                int position = playersList.getScrollPosition();
                initPlayersList();
                playersList.setScrollPosition(position);
                break;
        }
    }
}
