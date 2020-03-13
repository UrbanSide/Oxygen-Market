package austeretony.oxygen_market.common.main;

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
import austeretony.oxygen_market.client.MarketManagerClient;
import austeretony.oxygen_market.client.MarketStatusMessagesHandler;
import austeretony.oxygen_market.client.OffersSyncHandlerClient;
import austeretony.oxygen_market.client.SalesHistorySyncHandlerClient;
import austeretony.oxygen_market.client.command.MarketArgumentClient;
import austeretony.oxygen_market.client.event.MarketEventsClient;
import austeretony.oxygen_market.client.gui.market.MarketMenuScreen;
import austeretony.oxygen_market.client.gui.settings.MarketSettingsContainer;
import austeretony.oxygen_market.client.settings.EnumMarketClientSetting;
import austeretony.oxygen_market.client.settings.gui.EnumMarketGUISetting;
import austeretony.oxygen_market.common.config.MarketConfig;
import austeretony.oxygen_market.common.network.client.CPOfferAction;
import austeretony.oxygen_market.common.network.client.CPOpenMarketMenu;
import austeretony.oxygen_market.common.network.server.SPCreateOffer;
import austeretony.oxygen_market.common.network.server.SPPurchaseOrCancelOffer;
import austeretony.oxygen_market.server.MarketManagerServer;
import austeretony.oxygen_market.server.OffersSyncHandlerServer;
import austeretony.oxygen_market.server.SalesHistorySyncHandlerServer;
import austeretony.oxygen_market.server.command.MarketArgumentOperator;
import austeretony.oxygen_market.server.event.MarketEventsServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(
        modid = MarketMain.MODID, 
        name = MarketMain.NAME, 
        version = MarketMain.VERSION,
        dependencies = "required-after:oxygen_core@[0.11.0,);required-after:oxygen_mail@[0.11.0,);",
        certificateFingerprint = "@FINGERPRINT@",
        updateJSON = MarketMain.VERSIONS_FORGE_URL)
public class MarketMain {

    public static final String 
    MODID = "oxygen_market",
    NAME = "Oxygen: Market",
    VERSION = "0.11.0",
    VERSION_CUSTOM = VERSION + ":beta:0",
    GAME_VERSION = "1.12.2",
    VERSIONS_FORGE_URL = "https://raw.githubusercontent.com/AustereTony-MCMods/Oxygen-Market/info/mod_versions_forge.json";

    public static final int 
    MARKET_MOD_INDEX = 10,

    OFFERS_DATA_ID = 100,
    SALES_HISTORY_DATA_ID = 101,

    MARKET_MENU_SCREEN_ID = 100,

    OFFER_OPERATION_REQUEST_ID = 105,

    MARKET_MENU_TIMEOUT_ID = 100;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        OxygenHelperCommon.registerConfig(new MarketConfig());
        if (event.getSide() == Side.CLIENT)
            CommandOxygenClient.registerArgument(new MarketArgumentClient());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        this.initNetwork();
        MarketManagerServer.create();
        CommonReference.registerEvent(new MarketEventsServer());
        OxygenHelperServer.registerDataSyncHandler(new OffersSyncHandlerServer());
        OxygenHelperServer.registerDataSyncHandler(new SalesHistorySyncHandlerServer());
        NetworkRequestsRegistryServer.registerRequest(OFFER_OPERATION_REQUEST_ID, 1000);
        TimeOutRegistryServer.registerTimeOut(MARKET_MENU_TIMEOUT_ID, MarketConfig.MARKET_MENU_OPERATIONS_TIMEOUT_MILLIS.asInt());
        CommandOxygenOperator.registerArgument(new MarketArgumentOperator());
        EnumMarketPrivilege.register();
        if (event.getSide() == Side.CLIENT) {
            MarketManagerClient.create();
            CommonReference.registerEvent(new MarketEventsClient());
            OxygenGUIHelper.registerOxygenMenuEntry(MarketMenuScreen.MARKET_MENU_ENTRY);
            OxygenHelperClient.registerStatusMessagesHandler(new MarketStatusMessagesHandler());
            OxygenHelperClient.registerDataSyncHandler(new OffersSyncHandlerClient());
            OxygenHelperClient.registerDataSyncHandler(new SalesHistorySyncHandlerClient());
            EnumMarketClientSetting.register();
            EnumMarketGUISetting.register();
            SettingsScreen.registerSettingsContainer(new MarketSettingsContainer());
        }
    }

    public static void addDefaultPrivileges() {
        if (PrivilegesProviderServer.getRole(OxygenMain.OPERATOR_ROLE_ID).getPrivilege(EnumMarketPrivilege.SALES_HISTORY_ACCESS.id()) == null) {
            PrivilegesProviderServer.getRole(OxygenMain.OPERATOR_ROLE_ID).addPrivileges(
                    PrivilegeUtils.getPrivilege(EnumMarketPrivilege.SALES_HISTORY_ACCESS.id(), true),
                    PrivilegeUtils.getPrivilege(EnumMarketPrivilege.MARKET_MENU_OPERATOR_OPTIONS.id(), true));
            OxygenManagerServer.instance().getPrivilegesContainer().markChanged();
            OxygenMain.LOGGER.info("[Market] Default Operator role privileges added.");
        }
    }

    private void initNetwork() {
        OxygenMain.network().registerPacket(CPOpenMarketMenu.class);
        OxygenMain.network().registerPacket(CPOfferAction.class);

        OxygenMain.network().registerPacket(SPCreateOffer.class);
        OxygenMain.network().registerPacket(SPPurchaseOrCancelOffer.class);
    }
}
