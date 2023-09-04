package austeretony.oxygen_mail.common.main;

import austeretony.oxygen_core.client.api.OxygenGUIHelper;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.command.CommandOxygenClient;
import austeretony.oxygen_core.client.gui.settings.SettingsScreen;
import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.api.OxygenHelperCommon;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.privilege.PrivilegeUtils;
import austeretony.oxygen_core.server.OxygenManagerServer;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.PrivilegesProviderServer;
import austeretony.oxygen_core.server.command.CommandOxygenOperator;
import austeretony.oxygen_core.server.network.NetworkRequestsRegistryServer;
import austeretony.oxygen_core.server.timeout.TimeOutRegistryServer;
import austeretony.oxygen_mail.client.MailDataSyncHandlerClient;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.client.MailStatusMessagesHandler;
import austeretony.oxygen_mail.client.command.MailArgumentClient;
import austeretony.oxygen_mail.client.event.MailEventsClient;
import austeretony.oxygen_mail.client.gui.mail.MailMenuScreen;
import austeretony.oxygen_mail.client.gui.settings.MailSettingsContainer;
import austeretony.oxygen_mail.client.settings.EnumMailClientSetting;
import austeretony.oxygen_mail.client.settings.gui.EnumMailGUISetting;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.network.client.CPAttachmentReceived;
import austeretony.oxygen_mail.common.network.client.CPMessageRemoved;
import austeretony.oxygen_mail.common.network.client.CPOpenMailMenu;
import austeretony.oxygen_mail.common.network.client.CPMailSent;
import austeretony.oxygen_mail.common.network.server.SPMessageOperation;
import austeretony.oxygen_mail.common.network.server.SPSendMessage;
import austeretony.oxygen_mail.server.MailDataSyncHandlerServer;
import austeretony.oxygen_mail.server.MailManagerServer;
import austeretony.oxygen_mail.server.command.MailArgumentOperator;
import austeretony.oxygen_mail.server.event.MailEventsServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(
        modid = MailMain.MODID, 
        name = MailMain.NAME, 
        version = MailMain.VERSION,
        dependencies = "required-after:oxygen_core@[0.11.0,);",
        certificateFingerprint = "@FINGERPRINT@",
        updateJSON = MailMain.VERSIONS_FORGE_URL)
public class MailMain {

    public static final String 
    MODID = "oxygen_mail",
    NAME = "Oxygen: Mail",
    VERSION = "0.11.2",
    VERSION_CUSTOM = VERSION + ":beta:0",
    GAME_VERSION = "1.12.2",
    VERSIONS_FORGE_URL = "https://raw.githubusercontent.com/AustereTony-MCMods/Oxygen-Mail/info/mod_versions_forge.json";

    public static final int 
    MAIL_MOD_INDEX = 8,

    MAIL_MENU_SCREEN_ID = 80,

    MAIL_DATA_ID = 80,

    INCOMING_MESSAGE_NOTIFICATION_ID = 80,

    MESSAGE_OPERATION_REQUEST_ID = 80,
    
    MAIL_TIMEOUT_ID = 80;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        OxygenHelperCommon.registerConfig(new MailConfig());
        if (event.getSide() == Side.CLIENT)
            CommandOxygenClient.registerArgument(new MailArgumentClient());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        this.initNetwork();
        MailManagerServer.create();
        CommonReference.registerEvent(new MailEventsServer());
        NetworkRequestsRegistryServer.registerRequest(MESSAGE_OPERATION_REQUEST_ID, 500);
        OxygenHelperServer.registerDataSyncHandler(new MailDataSyncHandlerServer());
        TimeOutRegistryServer.registerTimeOut(MAIL_TIMEOUT_ID, MailConfig.MAIL_MENU_OPERATIONS_TIMEOUT_MILLIS.asInt());
        CommandOxygenOperator.registerArgument(new MailArgumentOperator());
        EnumMailPrivilege.register();
        if (event.getSide() == Side.CLIENT) {
            MailManagerClient.create();
            CommonReference.registerEvent(new MailEventsClient());
            OxygenGUIHelper.registerOxygenMenuEntry(MailMenuScreen.MAIL_MENU_ENTRY);
            OxygenHelperClient.registerStatusMessagesHandler(new MailStatusMessagesHandler());
            OxygenHelperClient.registerSharedDataSyncListener(MAIL_MENU_SCREEN_ID, MailManagerClient.instance().getMenuManager()::sharedDataSynchronized);
            OxygenHelperClient.registerDataSyncHandler(new MailDataSyncHandlerClient());
            EnumMailClientSetting.register();
            EnumMailGUISetting.register();
            SettingsScreen.registerSettingsContainer(new MailSettingsContainer());
        }
    }

    public static void addDefaultPrivileges() {
        if (PrivilegesProviderServer.getRole(OxygenMain.OPERATOR_ROLE_ID).getPrivilege(EnumMailPrivilege.MAILBOX_SIZE.id()) == null) {
            PrivilegesProviderServer.getRole(OxygenMain.OPERATOR_ROLE_ID).addPrivileges(
                    PrivilegeUtils.getPrivilege(EnumMailPrivilege.MAILBOX_SIZE.id(), 150),
                    PrivilegeUtils.getPrivilege(EnumMailPrivilege.MAIL_SENDING_COOLDOWN_SECONDS.id(), 10),

                    PrivilegeUtils.getPrivilege(EnumMailPrivilege.REMITTANCE_MAX_VALUE.id(), 1_000_000L),
                    PrivilegeUtils.getPrivilege(EnumMailPrivilege.COD_MAX_VALUE.id(), 1_000_000L),

                    PrivilegeUtils.getPrivilege(EnumMailPrivilege.LETTER_POSTAGE_VALUE.id(), 0L),
                    PrivilegeUtils.getPrivilege(EnumMailPrivilege.REMITTANCE_POSTAGE_PERCENT.id(), 0),
                    PrivilegeUtils.getPrivilege(EnumMailPrivilege.PARCEL_POSTAGE_VALUE.id(), 0L),
                    PrivilegeUtils.getPrivilege(EnumMailPrivilege.COD_POSTAGE_PERCENT.id(), 0));
            OxygenManagerServer.instance().getPrivilegesContainer().markChanged();
            OxygenMain.LOGGER.info("[Mail] Default Operator role privileges added.");
        }
    }

    private void initNetwork() {
        OxygenMain.network().registerPacket(CPOpenMailMenu.class);
        OxygenMain.network().registerPacket(CPMailSent.class);
        OxygenMain.network().registerPacket(CPMessageRemoved.class);
        OxygenMain.network().registerPacket(CPAttachmentReceived.class);

        OxygenMain.network().registerPacket(SPMessageOperation.class);
        OxygenMain.network().registerPacket(SPSendMessage.class);
    }
}
