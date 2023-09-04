package austeretony.oxygen_mail.client.gui.mail;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.gui.base.Alignment;
import austeretony.oxygen_core.client.gui.base.core.OxygenScreen;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_core.client.gui.base.core.Workspace;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuEntry;
import austeretony.oxygen_core.client.preset.CurrencyProperties;
import austeretony.oxygen_core.client.util.MinecraftClient;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_mail.client.gui.menu.MailScreenMenuEntry;
import austeretony.oxygen_mail.client.settings.MailSettings;
import austeretony.oxygen_mail.common.main.MailMain;
import net.minecraft.client.gui.GuiScreen;

public class MailScreen extends OxygenScreen {

    public static final OxygenMenuEntry MAIL_SCREEN_MENU_ENTRY = new MailScreenMenuEntry();

    private final CurrencyProperties currencyProperties;

    private IncomingMailSection incomingMailSection;
    private SendMailSection sendMailSection;

    private MailScreen() {
        currencyProperties = OxygenClient.getCurrencyProperties(OxygenMain.CURRENCY_COINS);
    }

    @Override
    public void initGui() {
        super.initGui();
        OxygenClient.requestSharedDataSync(MailMain.SCREEN_ID_MAIL_SCREEN, false);
        OxygenClient.requestDataSync(MailMain.DATA_ID_MAIL);
    }

    @Override
    public int getScreenId() {
        return MailMain.SCREEN_ID_MAIL_SCREEN;
    }

    @Override
    public Workspace createWorkspace() {
        Workspace workspace = new Workspace(this, 220, 194);
        workspace.setAlignment(Alignment.valueOf(MailSettings.MAIL_SCREEN_ALIGNMENT.asString()), 0, 0);
        return workspace;
    }

    @Override
    public void addSections() {
        getWorkspace().addSection(incomingMailSection = new IncomingMailSection(this));
        getWorkspace().addSection(sendMailSection = new SendMailSection(this));
    }

    @Override
    public Section getDefaultSection() {
        return incomingMailSection;
    }

    public CurrencyProperties getCurrencyProperties() {
        return currencyProperties;
    }

    public static void open() {
        MinecraftClient.displayGuiScreen(new MailScreen());
    }

    public static void sharedDataSynchronized() {
        GuiScreen screen = MinecraftClient.getCurrentScreen();
        if (screen instanceof MailScreen) {
            ((MailScreen) screen).incomingMailSection.sharedDataSynchronized();
            ((MailScreen) screen).sendMailSection.sharedDataSynchronized();
        }
    }

    public static void dataSynchronized() {
        GuiScreen screen = MinecraftClient.getCurrentScreen();
        if (screen instanceof MailScreen) {
            ((MailScreen) screen).incomingMailSection.dataSynchronized();
            ((MailScreen) screen).sendMailSection.dataSynchronized();
        }
    }

    public static void mailSent(int currencyIndex, long balance) {
        GuiScreen screen = MinecraftClient.getCurrentScreen();
        if (screen instanceof MailScreen) {
            ((MailScreen) screen).incomingMailSection.mailSent(currencyIndex, balance);
            ((MailScreen) screen).sendMailSection.mailSent(currencyIndex, balance);
        }
    }

    public static void mailRemoved(long entryId) {
        GuiScreen screen = MinecraftClient.getCurrentScreen();
        if (screen instanceof MailScreen) {
            ((MailScreen) screen).incomingMailSection.mailRemoved(entryId);
            ((MailScreen) screen).sendMailSection.mailRemoved(entryId);
        }
    }

    public static void attachmentReceived(long oldEntryId, long newEntryId, int currencyIndex, long balance) {
        GuiScreen screen = MinecraftClient.getCurrentScreen();
        if (screen instanceof MailScreen) {
            ((MailScreen) screen).incomingMailSection.attachmentReceived(oldEntryId, newEntryId, currencyIndex, balance);
            ((MailScreen) screen).sendMailSection.attachmentReceived(oldEntryId, newEntryId, currencyIndex, balance);
        }
    }
}
