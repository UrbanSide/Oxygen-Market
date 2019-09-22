package austeretony.oxygen_trade.common.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import austeretony.oxygen_core.client.api.OxygenGUIHelper;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.command.CommandOxygenClient;
import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.api.OxygenHelperCommon;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.privilege.PrivilegeImpl;
import austeretony.oxygen_core.common.privilege.PrivilegedGroupImpl;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.PrivilegeProviderServer;
import austeretony.oxygen_core.server.api.RequestsFilterHelper;
import austeretony.oxygen_mail.common.main.EnumMailPrivilege;
import austeretony.oxygen_trade.client.OffersSyncHandlerClient;
import austeretony.oxygen_trade.client.SalesHistorySyncHandlerClient;
import austeretony.oxygen_trade.client.TradeManagerClient;
import austeretony.oxygen_trade.client.TradeStatusMessagesHandler;
import austeretony.oxygen_trade.client.command.TradeArgumentExecutorClient;
import austeretony.oxygen_trade.client.event.TradeEventsClient;
import austeretony.oxygen_trade.client.gui.trade.TradeMenuGUIScreen;
import austeretony.oxygen_trade.client.input.TradeKeyHandler;
import austeretony.oxygen_trade.common.config.TradeConfig;
import austeretony.oxygen_trade.common.network.client.CPOfferAction;
import austeretony.oxygen_trade.common.network.server.SPCreateOffer;
import austeretony.oxygen_trade.common.network.server.SPPurchaseOrCancelOffer;
import austeretony.oxygen_trade.server.OffersSyncHandlerServer;
import austeretony.oxygen_trade.server.SalesHistorySyncHandlerServer;
import austeretony.oxygen_trade.server.TradeManagerServer;
import austeretony.oxygen_trade.server.category.ItemCategoriesPresetServer;
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
        dependencies = "required-after:oxygen_core@[0.9.0,);required-after:oxygen_mail@[0.9.0,);",
        certificateFingerprint = "@FINGERPRINT@",
        updateJSON = TradeMain.VERSIONS_FORGE_URL)
public class TradeMain {

    public static final String 
    MODID = "oxygen_trade",
    NAME = "Oxygen: Trade",
    VERSION = "0.9.1",
    VERSION_CUSTOM = VERSION + ":beta:0",
    GAME_VERSION = "1.12.2",
    VERSIONS_FORGE_URL = "https://raw.githubusercontent.com/AustereTony-MCMods/Oxygen-Trade/info/mod_versions_forge.json";

    public static final int 
    TRADE_MOD_INDEX = 10,

    OFFERS_DATA_ID = 100,
    SALES_HISTORY_DATA_ID = 101,

    ITEM_CATEGORIES_PRESET_ID = 100,

    TRADE_MENU_SCREEN_ID = 100,

    CREATE_OFFER_REQUEST_ID = 105, 
    PURCHASE_OR_CANCEL_REQUEST_ID = 106;

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        OxygenHelperCommon.registerConfig(new TradeConfig());
        if (event.getSide() == Side.CLIENT)
            CommandOxygenClient.registerArgumentExecutor(new TradeArgumentExecutorClient("trade", true));
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        this.initNetwork();
        TradeManagerServer.create();
        CommonReference.registerEvent(new TradeEventsServer());
        OxygenHelperServer.registerDataSyncHandler(new OffersSyncHandlerServer());
        OxygenHelperServer.registerDataSyncHandler(new SalesHistorySyncHandlerServer());
        OxygenHelperServer.registerPreset(new ItemCategoriesPresetServer());
        RequestsFilterHelper.registerNetworkRequest(CREATE_OFFER_REQUEST_ID, 1);
        RequestsFilterHelper.registerNetworkRequest(PURCHASE_OR_CANCEL_REQUEST_ID, 1);
        if (event.getSide() == Side.CLIENT) {
            TradeManagerClient.create();
            CommonReference.registerEvent(new TradeEventsClient());
            if (!OxygenGUIHelper.isOxygenMenuEnabled())
                CommonReference.registerEvent(new TradeKeyHandler());
            OxygenGUIHelper.registerOxygenMenuEntry(TradeMenuGUIScreen.TRADE_MENU_ENTRY);
            OxygenHelperClient.registerStatusMessagesHandler(new TradeStatusMessagesHandler());
            OxygenHelperClient.registerDataSyncHandler(new OffersSyncHandlerClient());
            OxygenHelperClient.registerDataSyncHandler(new SalesHistorySyncHandlerClient());
        }
        EnumTradePrivilege.register();
    }

    public static void addDefaultPrivileges() {
        if (!PrivilegeProviderServer.getGroup(PrivilegedGroupImpl.OPERATORS_GROUP.groupName).hasPrivilege(EnumMailPrivilege.MAILBOX_SIZE.toString())) {
            PrivilegeProviderServer.addPrivileges(PrivilegedGroupImpl.OPERATORS_GROUP.groupName, true,  
                    new PrivilegeImpl(EnumTradePrivilege.ITEMS_PER_OFFER_MAX_AMOUNT.toString(), 1000),
                    new PrivilegeImpl(EnumTradePrivilege.PRICE_MAX_VALUE.toString(), 1_000_000L));
            LOGGER.info("Default <{}> group privileges added.", PrivilegedGroupImpl.OPERATORS_GROUP.groupName);
        }
    }

    private void initNetwork() {
        OxygenMain.network().registerPacket(CPOfferAction.class);

        OxygenMain.network().registerPacket(SPCreateOffer.class);
        OxygenMain.network().registerPacket(SPPurchaseOrCancelOffer.class);
    }
}
