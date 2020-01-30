package austeretony.oxygen_trade.common.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import austeretony.oxygen_core.client.api.OxygenGUIHelper;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.command.CommandOxygenClient;
import austeretony.oxygen_core.client.gui.settings.SettingsScreen;
import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.api.OxygenHelperCommon;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.privilege.PrivilegeUtils;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.PrivilegesProviderServer;
import austeretony.oxygen_core.server.command.CommandOxygenOperator;
import austeretony.oxygen_core.server.network.NetworkRequestsRegistryServer;
import austeretony.oxygen_core.server.timeout.TimeOutRegistryServer;
import austeretony.oxygen_trade.client.OffersSyncHandlerClient;
import austeretony.oxygen_trade.client.SalesHistorySyncHandlerClient;
import austeretony.oxygen_trade.client.TradeManagerClient;
import austeretony.oxygen_trade.client.TradeStatusMessagesHandler;
import austeretony.oxygen_trade.client.command.TradeArgumentClient;
import austeretony.oxygen_trade.client.event.TradeEventsClient;
import austeretony.oxygen_trade.client.gui.settings.TradeSettingsContainer;
import austeretony.oxygen_trade.client.gui.trade.TradeMenuScreen;
import austeretony.oxygen_trade.client.settings.EnumTradeClientSetting;
import austeretony.oxygen_trade.client.settings.gui.EnumTradeGUISetting;
import austeretony.oxygen_trade.common.config.TradeConfig;
import austeretony.oxygen_trade.common.network.client.CPOfferAction;
import austeretony.oxygen_trade.common.network.client.CPOpenTradeMenu;
import austeretony.oxygen_trade.common.network.server.SPCreateOffer;
import austeretony.oxygen_trade.common.network.server.SPPurchaseOrCancelOffer;
import austeretony.oxygen_trade.server.OffersSyncHandlerServer;
import austeretony.oxygen_trade.server.SalesHistorySyncHandlerServer;
import austeretony.oxygen_trade.server.TradeManagerServer;
import austeretony.oxygen_trade.server.command.TradeArgumentOperator;
import austeretony.oxygen_trade.server.event.TradeEventsServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(
        modid = TradeMain.MODID, 
        name = TradeMain.NAME, 
        version = TradeMain.VERSION,
        dependencies = "required-after:oxygen_core@[0.10.1,);required-after:oxygen_mail@[0.10.1,);",
        certificateFingerprint = "@FINGERPRINT@",
        updateJSON = TradeMain.VERSIONS_FORGE_URL)
public class TradeMain {

    public static final String 
    MODID = "oxygen_trade",
    NAME = "Oxygen: Trade",
    VERSION = "0.10.1",
    VERSION_CUSTOM = VERSION + ":beta:0",
    GAME_VERSION = "1.12.2",
    VERSIONS_FORGE_URL = "https://raw.githubusercontent.com/AustereTony-MCMods/Oxygen-Trade/info/mod_versions_forge.json";

    public static final int 
    TRADE_MOD_INDEX = 10,

    OFFERS_DATA_ID = 100,
    SALES_HISTORY_DATA_ID = 101,

    TRADE_MENU_SCREEN_ID = 100,

    OFFER_OPERATION_REQUEST_ID = 105,

    TRADE_MENU_TIMEOUT_ID = 100;

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        OxygenHelperCommon.registerConfig(new TradeConfig());
        if (event.getSide() == Side.CLIENT)
            CommandOxygenClient.registerArgument(new TradeArgumentClient());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        this.initNetwork();
        TradeManagerServer.create();
        CommonReference.registerEvent(new TradeEventsServer());
        OxygenHelperServer.registerDataSyncHandler(new OffersSyncHandlerServer());
        OxygenHelperServer.registerDataSyncHandler(new SalesHistorySyncHandlerServer());
        NetworkRequestsRegistryServer.registerRequest(OFFER_OPERATION_REQUEST_ID, 1000);
        TimeOutRegistryServer.registerTimeOut(TRADE_MENU_TIMEOUT_ID, TradeConfig.TRADE_MENU_OPERATIONS_TIMEOUT_MILLIS.asInt());
        CommandOxygenOperator.registerArgument(new TradeArgumentOperator());
        EnumTradePrivilege.register();
        if (event.getSide() == Side.CLIENT) {
            TradeManagerClient.create();
            CommonReference.registerEvent(new TradeEventsClient());
            OxygenGUIHelper.registerOxygenMenuEntry(TradeMenuScreen.TRADE_MENU_ENTRY);
            OxygenHelperClient.registerStatusMessagesHandler(new TradeStatusMessagesHandler());
            OxygenHelperClient.registerDataSyncHandler(new OffersSyncHandlerClient());
            OxygenHelperClient.registerDataSyncHandler(new SalesHistorySyncHandlerClient());
            EnumTradeClientSetting.register();
            EnumTradeGUISetting.register();
            SettingsScreen.registerSettingsContainer(new TradeSettingsContainer());
        }
    }

    public static void addDefaultPrivileges() {
        if (PrivilegesProviderServer.getRole(OxygenMain.OPERATOR_ROLE_ID).getPrivilege(EnumTradePrivilege.SALES_HISTORY_ACCESS.id()) == null) {
            PrivilegesProviderServer.getRole(OxygenMain.OPERATOR_ROLE_ID).addPrivileges(
                    PrivilegeUtils.getPrivilege(EnumTradePrivilege.SALES_HISTORY_ACCESS.id(), true),
                    PrivilegeUtils.getPrivilege(EnumTradePrivilege.TRADE_MENU_OPERATOR_OPTIONS.id(), true));
            LOGGER.info("Default Operator role privileges added.");
        }
    }

    private void initNetwork() {
        OxygenMain.network().registerPacket(CPOpenTradeMenu.class);
        OxygenMain.network().registerPacket(CPOfferAction.class);

        OxygenMain.network().registerPacket(SPCreateOffer.class);
        OxygenMain.network().registerPacket(SPPurchaseOrCancelOffer.class);
    }
}
