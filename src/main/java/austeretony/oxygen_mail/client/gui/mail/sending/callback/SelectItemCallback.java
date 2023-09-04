package austeretony.oxygen_mail.client.gui.mail.sending.callback;

import java.util.HashSet;
import java.util.Set;

import org.lwjgl.input.Keyboard;

import austeretony.alternateui.screen.callback.AbstractGUICallback;
import austeretony.alternateui.screen.core.AbstractGUISection;
import austeretony.alternateui.screen.core.GUIBaseElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.gui.elements.OxygenCallbackBackgroundFiller;
import austeretony.oxygen_core.client.gui.elements.OxygenKeyButton;
import austeretony.oxygen_core.client.gui.elements.OxygenScrollablePanel;
import austeretony.oxygen_core.client.gui.elements.OxygenTextLabel;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_mail.client.gui.mail.MailMenuScreen;
import austeretony.oxygen_mail.client.gui.mail.SendingSection;
import austeretony.oxygen_mail.client.gui.mail.sending.InventoryItemPanelEntry;

public class SelectItemCallback extends AbstractGUICallback {

    private final MailMenuScreen screen;

    private final SendingSection section;

    private OxygenScrollablePanel inventoryContentPanel;

    private OxygenKeyButton closeButton;

    public SelectItemCallback(MailMenuScreen screen, SendingSection section, int width, int height) {
        super(screen, section, width, height);
        this.screen = screen;
        this.section = section;
    }

    @Override
    public void init() {
        this.enableDefaultBackground(EnumBaseGUISetting.FILL_CALLBACK_COLOR.get().asInt());
        this.addElement(new OxygenCallbackBackgroundFiller(0, 0, this.getWidth(), this.getHeight()));
        this.addElement(new OxygenTextLabel(4, 12, ClientReference.localize("oxygen_mail.gui.mail.callback.selectItem"), EnumBaseGUISetting.TEXT_SCALE.get().asFloat(), EnumBaseGUISetting.TEXT_ENABLED_COLOR.get().asInt()));

        this.addElement(this.inventoryContentPanel = new OxygenScrollablePanel(this.screen, 6, 15, 128, 16, 1, 36, 4, EnumBaseGUISetting.TEXT_PANEL_SCALE.get().asFloat(), false));
        this.loadInventoryContent();

        this.inventoryContentPanel.<InventoryItemPanelEntry>setElementClickListener((previous, clicked, mouseX, mouseY, mouseButton)->this.section.itemSelected(clicked));

        this.addElement(this.closeButton = new OxygenKeyButton(this.getWidth() - 55, this.getHeight() - 10, ClientReference.localize("oxygen_core.gui.close"), Keyboard.KEY_X, this::close));
    }

    public void loadInventoryContent() {
        if (this.inventoryContentPanel != null) {
            this.inventoryContentPanel.reset();
            Set<String> added = new HashSet<>();
            String key;
            for (ItemStackWrapper stackWrapper : this.screen.getInventoryContent().keySet()) {
                key = stackWrapper.toString();
                if (!added.contains(key)) {
                    this.inventoryContentPanel.addEntry(new InventoryItemPanelEntry(stackWrapper, this.screen.getEqualStackAmount(stackWrapper)));
                    added.add(key);
                }              
            }

            this.inventoryContentPanel.getScroller().reset();
            this.inventoryContentPanel.getScroller().updateRowsAmount(MathUtils.clamp(this.screen.getInventoryContent().size(), 4, ClientReference.getClientPlayer().inventory.mainInventory.size()));
        }
    }

    @Override
    public void handleElementClick(AbstractGUISection section, GUIBaseElement element, int mouseButton) {
        if (mouseButton == 0) 
            if (element == this.closeButton)
                this.close();
    }
}
