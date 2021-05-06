package austeretony.oxygen_market.common.main;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.command.CommandOxygenClient;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuHelper;
import austeretony.oxygen_core.common.api.OxygenCommon;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.util.MinecraftCommon;
import austeretony.oxygen_core.server.api.OxygenServer;
import austeretony.oxygen_core.server.command.CommandOxygenOperator;
import austeretony.oxygen_market.client.MarketManagerClient;
import austeretony.oxygen_market.client.command.MarketArgumentClient;
import austeretony.oxygen_market.client.event.MarketEventsClient;
import austeretony.oxygen_market.client.gui.market.MarketScreen;
import austeretony.oxygen_market.client.network.operation.MarketNetworkOperationsHandlerClient;
import austeretony.oxygen_market.client.settings.MarketSettings;
import austeretony.oxygen_market.client.sync.MarketDealsSyncHandlerClient;
import austeretony.oxygen_market.client.sync.SalesHistorySyncHandlerClient;
import austeretony.oxygen_market.common.config.MarketConfig;
import austeretony.oxygen_market.server.MarketManagerServer;
import austeretony.oxygen_market.server.command.MarketArgumentOperator;
import austeretony.oxygen_market.server.event.MarketEventsServer;
import austeretony.oxygen_market.server.network.operation.MarketNetworkOperationsHandlerServer;
import austeretony.oxygen_market.server.sync.MarketDealsSyncHandlerServer;
import austeretony.oxygen_market.server.sync.SalesHistorySyncHandlerServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(
        modid = MarketMain.MOD_ID,
        name = MarketMain.NAME,
        version = MarketMain.VERSION,
        dependencies = "required-after:oxygen_core@[0.12.0,);required-after:oxygen_mail@[0.12.0,);",
        certificateFingerprint = "@FINGERPRINT@",
        updateJSON = MarketMain.VERSIONS_FORGE_URL)
public class MarketMain {

    public static final String
            MOD_ID = "oxygen_market",
            NAME = "Oxygen: Market",
            VERSION = "0.12.0",
            VERSION_CUSTOM = VERSION + ":beta:0",
            VERSIONS_FORGE_URL = "https://raw.githubusercontent.com/AustereTony-MCMods/Oxygen-Market/info/versions.json";

    //oxygen module index
    public static final int MODULE_INDEX = 12;

    //screen id
    public static final int SCREEN_ID_MARKET = 120;

    //timeout ids
    public static final int TIMEOUT_MARKET_OPERATIONS = 120;

    //data id
    public static final int
            DATA_ID_MARKET_DEALS = 120,
            DATA_ID_MARKET_HISTORY = 121;

    //key binding id
    public static final int KEYBINDING_ID_OPEN_MARKET_SCREEN = 120;

    //items blacklist name
    public static final String ITEMS_BLACKLIST_MARKET = "market";

    //operations handler id
    public static final int
            MARKET_OPERATIONS_HANDLER_ID = 120;

    //operations
    public static String
            OPERATION_DEAL_CREATION = "market:deal_creation",
            OPERATION_PURCHASE = "market:purchase";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        OxygenCommon.registerConfig(new MarketConfig());
        if (event.getSide() == Side.CLIENT) {
            CommandOxygenClient.registerArgument(new MarketArgumentClient());
            OxygenClient.registerKeyBind(
                    KEYBINDING_ID_OPEN_MARKET_SCREEN,
                    "key.oxygen_market.open_market_screen",
                    OxygenMain.KEY_BINDINGS_CATEGORY,
                    MarketConfig.MARKET_SCREEN_KEY::asInt,
                    MarketConfig.ENABLE_MARKET_SCREEN_KEY::asBoolean,
                    true,
                    () -> OxygenClient.openScreen(SCREEN_ID_MARKET));
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MarketManagerServer.instance();
        OxygenServer.registerItemsBlacklist(ITEMS_BLACKLIST_MARKET);
        MinecraftCommon.registerEventHandler(new MarketEventsServer());
        OxygenServer.registerTimeout(TIMEOUT_MARKET_OPERATIONS, MarketConfig.MARKET_SCREEN_OPERATIONS_TIMEOUT_MILLIS.asInt());
        OxygenServer.registerDataSyncHandler(new MarketDealsSyncHandlerServer());
        OxygenServer.registerDataSyncHandler(new SalesHistorySyncHandlerServer());
        OxygenServer.registerOperationsHandler(new MarketNetworkOperationsHandlerServer());
        MarketPrivileges.register();
        CommandOxygenOperator.registerArgument(new MarketArgumentOperator());
        if (event.getSide() == Side.CLIENT) {
            MarketManagerClient.instance();
            MinecraftCommon.registerEventHandler(new MarketEventsClient());
            OxygenClient.registerDataSyncHandler(new MarketDealsSyncHandlerClient());
            OxygenClient.registerDataSyncHandler(new SalesHistorySyncHandlerClient());
            OxygenClient.registerOperationsHandler(new MarketNetworkOperationsHandlerClient());
            MarketSettings.register();
            OxygenMenuHelper.addMenuEntry(MarketScreen.MARKET_SCREEN_MENU_ENTRY);
            OxygenClient.registerScreen(SCREEN_ID_MARKET, MarketScreen::open,
                    MarketConfig.ENABLE_MARKET_ACCESS_CLIENT_SIDE::asBoolean);
        }
    }
}
