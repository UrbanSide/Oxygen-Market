package austeretony.oxygen_core.common.main;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

import org.apache.logging.log4j.Logger;

import austeretony.oxygen_core.client.OxygenManagerClient;
import austeretony.oxygen_core.client.OxygenStatusMessagesHandler;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.EnumBaseClientSetting;
import austeretony.oxygen_core.client.api.EnumBaseGUISetting;
import austeretony.oxygen_core.client.api.InventoryProviderClient;
import austeretony.oxygen_core.client.api.OxygenGUIHelper;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.api.OxygenOverlayHandler;
import austeretony.oxygen_core.client.command.CommandOxygenClient;
import austeretony.oxygen_core.client.command.CoreArgumentClient;
import austeretony.oxygen_core.client.currency.OxygenCoinsCurrencyProperties;
import austeretony.oxygen_core.client.currency.OxygenShardsCurrencyProperties;
import austeretony.oxygen_core.client.currency.OxygenVouchersCurrencyProperties;
import austeretony.oxygen_core.client.event.OxygenEventsClient;
import austeretony.oxygen_core.client.gui.notifications.NotificationsScreen;
import austeretony.oxygen_core.client.gui.overlay.RequestOverlay;
import austeretony.oxygen_core.client.gui.privileges.information.PrivilegesScreen;
import austeretony.oxygen_core.client.gui.settings.BaseSettingsContainer;
import austeretony.oxygen_core.client.gui.settings.CoreSettingsContainer;
import austeretony.oxygen_core.client.gui.settings.SettingsScreen;
import austeretony.oxygen_core.client.settings.EnumCoreClientSetting;
import austeretony.oxygen_core.client.settings.gui.EnumCoreGUISetting;
import austeretony.oxygen_core.common.InstantDataAbsorption;
import austeretony.oxygen_core.common.InstantDataArmor;
import austeretony.oxygen_core.common.InstantDataHealth;
import austeretony.oxygen_core.common.InstantDataMaxHealth;
import austeretony.oxygen_core.common.InstantDataPotionEffects;
import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.api.OxygenHelperCommon;
import austeretony.oxygen_core.common.condition.ConditionsRegistry;
import austeretony.oxygen_core.common.condition.conditions.minecraft.ConditionWorldIsDaytime;
import austeretony.oxygen_core.common.condition.conditions.minecraft.player.ConditionPlayerDimension;
import austeretony.oxygen_core.common.condition.conditions.minecraft.player.ConditionPlayerExperience;
import austeretony.oxygen_core.common.condition.conditions.minecraft.player.ConditionPlayerHaveItem;
import austeretony.oxygen_core.common.condition.conditions.minecraft.player.ConditionPlayerHealth;
import austeretony.oxygen_core.common.condition.conditions.minecraft.player.ConditionPlayerHeldItem;
import austeretony.oxygen_core.common.condition.conditions.minecraft.player.ConditionPlayerUsername;
import austeretony.oxygen_core.common.condition.conditions.oxygen.ConditionPlayerCurrency;
import austeretony.oxygen_core.common.config.ConfigManager;
import austeretony.oxygen_core.common.config.OxygenConfig;
import austeretony.oxygen_core.common.config.PrivilegesConfig;
import austeretony.oxygen_core.common.inventory.VanillaPlayerInventoryProvider;
import austeretony.oxygen_core.common.network.Network;
import austeretony.oxygen_core.common.network.client.CPAddSharedData;
import austeretony.oxygen_core.common.network.client.CPDefaultPrivilegeOperation;
import austeretony.oxygen_core.common.network.client.CPPlaySoundEvent;
import austeretony.oxygen_core.common.network.client.CPPlayerRolesChanged;
import austeretony.oxygen_core.common.network.client.CPRemoveSharedData;
import austeretony.oxygen_core.common.network.client.CPRoleAction;
import austeretony.oxygen_core.common.network.client.CPRolePrivilegeOperation;
import austeretony.oxygen_core.common.network.client.CPShowStatusMessage;
import austeretony.oxygen_core.common.network.client.CPSyncAbsentData;
import austeretony.oxygen_core.common.network.client.CPSyncConfigs;
import austeretony.oxygen_core.common.network.client.CPSyncInstantData;
import austeretony.oxygen_core.common.network.client.CPSyncMainData;
import austeretony.oxygen_core.common.network.client.CPSyncNotification;
import austeretony.oxygen_core.common.network.client.CPSyncObservedPlayersData;
import austeretony.oxygen_core.common.network.client.CPSyncPlayerRoles;
import austeretony.oxygen_core.common.network.client.CPSyncPreset;
import austeretony.oxygen_core.common.network.client.CPSyncPresetsVersions;
import austeretony.oxygen_core.common.network.client.CPSyncPrivilegesManagementData;
import austeretony.oxygen_core.common.network.client.CPSyncRolesData;
import austeretony.oxygen_core.common.network.client.CPSyncSharedData;
import austeretony.oxygen_core.common.network.client.CPSyncValidDataIds;
import austeretony.oxygen_core.common.network.client.CPSyncWatchedValue;
import austeretony.oxygen_core.common.network.server.SPAbsentDataIds;
import austeretony.oxygen_core.common.network.server.SPDefaultPrivilegeOperation;
import austeretony.oxygen_core.common.network.server.SPPlayerRoleOperation;
import austeretony.oxygen_core.common.network.server.SPRemoveRole;
import austeretony.oxygen_core.common.network.server.SPRequestPresetSync;
import austeretony.oxygen_core.common.network.server.SPRequestPrivilegesData;
import austeretony.oxygen_core.common.network.server.SPRequestReply;
import austeretony.oxygen_core.common.network.server.SPRequestSharedDataSync;
import austeretony.oxygen_core.common.network.server.SPRoleOperation;
import austeretony.oxygen_core.common.network.server.SPRolePrivilegeOperation;
import austeretony.oxygen_core.common.network.server.SPSetActivityStatus;
import austeretony.oxygen_core.common.network.server.SPSetChatFormattingRole;
import austeretony.oxygen_core.common.network.server.SPStartDataSync;
import austeretony.oxygen_core.common.privilege.PrivilegeUtils;
import austeretony.oxygen_core.common.scripting.ScriptingProvider;
import austeretony.oxygen_core.common.scripting.adapter.DummyScriptingAdapter;
import austeretony.oxygen_core.common.scripting.adapter.ECMAScriptAdapter;
import austeretony.oxygen_core.common.sound.OxygenSoundEffects;
import austeretony.oxygen_core.common.util.OxygenUtils;
import austeretony.oxygen_core.server.OxygenManagerServer;
import austeretony.oxygen_core.server.api.CurrencyHelperServer;
import austeretony.oxygen_core.server.api.InventoryProviderServer;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.PrivilegesProviderServer;
import austeretony.oxygen_core.server.api.event.OxygenWorldLoadedEvent;
import austeretony.oxygen_core.server.command.CommandOxygenOperator;
import austeretony.oxygen_core.server.command.CommandOxygenServer;
import austeretony.oxygen_core.server.command.CoreArgumentOperator;
import austeretony.oxygen_core.server.currency.OxygenCoinsCurrencyProvider;
import austeretony.oxygen_core.server.currency.OxygenShardsCurrencyProvider;
import austeretony.oxygen_core.server.currency.OxygenVouchersCurrencyProvider;
import austeretony.oxygen_core.server.event.OxygenEventsServer;
import austeretony.oxygen_core.server.event.PlayerVersusPlayerEvents;
import austeretony.oxygen_core.server.event.PrivilegesEventsServer;
import austeretony.oxygen_core.server.instant.InstantDataRegistryServer;
import austeretony.oxygen_core.server.item.ItemsBlackList;
import austeretony.oxygen_core.server.network.NetworkRequestsRegistryServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(
        modid = OxygenMain.MODID, 
        name = OxygenMain.NAME, 
        version = OxygenMain.VERSION,
        certificateFingerprint = "@FINGERPRINT@",
        updateJSON = OxygenMain.VERSIONS_FORGE_URL)
public class OxygenMain {

    public static final String 
    MODID = "oxygen_core", 
    NAME = "Oxygen Core", 
    VERSION = "0.11.4", 
    VERSION_CUSTOM = VERSION + ":beta:0",
    GAME_VERSION = "1.12.2",
    VERSIONS_FORGE_URL = "https://raw.githubusercontent.com/AustereTony-MCMods/Oxygen-Core/info/mod_versions_forge.json";

    public static final Logger LOGGER = OxygenUtils.getLogger("common", "oxygen", "Oxygen");

    private static Network network;

    /*
     * Indexes:
     * 
     * Core - 0
     * Teleportation - 1 
     * Groups - 2
     * Exchange - 3 
     * Merchants - 4 
     * Players List - 5 
     * Friends List - 6
     * Player Interaction - 7
     * Mail - 8
     * Chat - 9 (WIP)
     * Market (Trade) - 10
     * Guilds - 11 (WIP)
     * Interaction - 12 (WIP)
     * Regions - 13 (WIP)
     * Daily Rewards - 14
     * Shop - 15 
     * Essentials - 16 (WIP)
     * Store - 17
     * Duels - 18 (WIP)
     */

    //Shared Constants

    public static final int 
    OXYGEN_CORE_MOD_INDEX = 0,

    NOTIFICATIONS_SCREEN_ID = 0,
    SETTINGS_SCREEN_ID = 1,
    PRIVILEGES_SCREEN_ID = 2,

    SIMPLE_NOTIFICATION_ID = 0,

    ACTIVITY_STATUS_SHARED_DATA_ID = 0,
    DIMENSION_SHARED_DATA_ID = 1,

    ROLES_SHARED_DATA_STARTING_INDEX = 10,
    DEFAULT_ROLE_INDEX = - 1,
    MAX_ROLES_PER_PLAYER = 5,

    PRESETS_SYNC_REQUEST_ID = 0,
    SHARED_DATA_SYNC_REQUEST_ID = 1,
    REQUEST_REPLY_REQUEST_ID = 2,
    SET_ACTIVITY_STATUS_REQUEST_ID = 3,
    MANAGE_PRIVILEGES_REQUEST_ID = 4,

    ITEM_CATEGORIES_PRESET_ID = 0,

    COMMON_CURRENCY_INDEX = 0,

    OPERATOR_ROLE_ID = 100,

    HEALTH_INSTANT_DATA_INDEX = 0,
    MAX_HEALTH_INSTANT_DATA_INDEX = 1,
    TOTAL_ARMOR_INSTANT_DATA_INDEX = 2,
    ABSORPTION_INSTANT_DATA_INDEX = 3,
    ACTIVE_EFFECTS_INSTANT_DATA_INDEX = 10;

    public static final DateTimeFormatter
    ID_DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMddHHmmss", Locale.ENGLISH),
    DEBUG_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:dd z", Locale.ENGLISH);

    public static final String SYSTEM_SENDER = "oxygen_core.sender.sys";

    public static final UUID SYSTEM_UUID = UUID.fromString("d10d07f6-ae3c-4ec6-a055-1160c4cf848a");

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ConfigManager.create();
        OxygenHelperCommon.registerConfig(new OxygenConfig());
        OxygenHelperCommon.registerConfig(new PrivilegesConfig());
        if (event.getSide() == Side.CLIENT)
            CommandOxygenClient.registerArgument(new CoreArgumentClient());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        ConfigManager.instance().loadConfigs();
        this.initNetwork();
        OxygenManagerServer.create();
        CommonReference.registerEvent(new OxygenSoundEffects());
        CommonReference.registerEvent(new OxygenEventsServer());
        if (OxygenConfig.ENABLE_PVP_MANAGER.asBoolean())
            CommonReference.registerEvent(new PlayerVersusPlayerEvents());
        if (OxygenConfig.ENABLE_PRIVILEGES.asBoolean())
            CommonReference.registerEvent(new PrivilegesEventsServer());
        OxygenHelperServer.registerSharedDataValue(ACTIVITY_STATUS_SHARED_DATA_ID, Byte.BYTES);
        OxygenHelperServer.registerSharedDataValue(DIMENSION_SHARED_DATA_ID, Integer.BYTES);
        for (int i = 0; i < MAX_ROLES_PER_PLAYER; i++)
            OxygenHelperServer.registerSharedDataValue(i + ROLES_SHARED_DATA_STARTING_INDEX, Byte.BYTES);
        CommandOxygenOperator.registerArgument(new CoreArgumentOperator());
        NetworkRequestsRegistryServer.registerRequest(PRESETS_SYNC_REQUEST_ID, 10000);
        NetworkRequestsRegistryServer.registerRequest(SHARED_DATA_SYNC_REQUEST_ID, 1000);
        NetworkRequestsRegistryServer.registerRequest(REQUEST_REPLY_REQUEST_ID, 1000);
        NetworkRequestsRegistryServer.registerRequest(SET_ACTIVITY_STATUS_REQUEST_ID, 1000);
        NetworkRequestsRegistryServer.registerRequest(MANAGE_PRIVILEGES_REQUEST_ID, 1000);
        CurrencyHelperServer.registerCurrencyProvider(new OxygenCoinsCurrencyProvider());
        CurrencyHelperServer.registerCurrencyProvider(new OxygenShardsCurrencyProvider());
        CurrencyHelperServer.registerCurrencyProvider(new OxygenVouchersCurrencyProvider());
        InstantDataRegistryServer.registerInstantData(new InstantDataHealth());
        InstantDataRegistryServer.registerInstantData(new InstantDataMaxHealth());
        InstantDataRegistryServer.registerInstantData(new InstantDataAbsorption());
        InstantDataRegistryServer.registerInstantData(new InstantDataArmor());
        InstantDataRegistryServer.registerInstantData(new InstantDataPotionEffects());
        InventoryProviderServer.registerPlayerInventoryProvider(new VanillaPlayerInventoryProvider());
        ScriptingProvider.registerAdapter(new DummyScriptingAdapter());
        if (OxygenConfig.ENABLE_ECMASCRIPT_ADAPTER.asBoolean())
            ScriptingProvider.registerAdapter(new ECMAScriptAdapter());
        ConditionsRegistry.registerCondition("minecraft:worldIsDaytime", ConditionWorldIsDaytime.class);
        ConditionsRegistry.registerCondition("minecraft:playerDimension", ConditionPlayerDimension.class);
        ConditionsRegistry.registerCondition("minecraft:playerHealthAmount", ConditionPlayerHealth.class);
        ConditionsRegistry.registerCondition("minecraft:playerExperienceLevel", ConditionPlayerExperience.class);
        ConditionsRegistry.registerCondition("minecraft:playerHaveItem", ConditionPlayerHaveItem.class);
        ConditionsRegistry.registerCondition("minecraft:playerHeldItem", ConditionPlayerHeldItem.class);
        ConditionsRegistry.registerCondition("minecraft:playerUsername", ConditionPlayerUsername.class);
        ConditionsRegistry.registerCondition("oxygen_core:playerCurrencyAmount", ConditionPlayerCurrency.class);
        EnumOxygenPrivilege.register();
        if (event.getSide() == Side.CLIENT) {     
            OxygenManagerClient.create();
            ClientReference.registerCommand(new CommandOxygenClient("oxygenc"));
            CommonReference.registerEvent(new OxygenEventsClient());
            CommonReference.registerEvent(new OxygenOverlayHandler());     
            OxygenGUIHelper.registerOverlay(new RequestOverlay());
            OxygenGUIHelper.registerOxygenMenuEntry(NotificationsScreen.NOTIFICATIONS_MENU_ENTRY);
            OxygenGUIHelper.registerOxygenMenuEntry(SettingsScreen.SETTINGS_MENU_ENTRY);
            OxygenGUIHelper.registerOxygenMenuEntry(PrivilegesScreen.PRIVILEGES_MENU_ENTRY);
            OxygenHelperClient.registerStatusMessagesHandler(new OxygenStatusMessagesHandler());
            EnumBaseClientSetting.register();
            EnumCoreClientSetting.register();
            EnumBaseGUISetting.register();
            EnumCoreGUISetting.register();
            SettingsScreen.registerSettingsContainer(new BaseSettingsContainer());
            SettingsScreen.registerSettingsContainer(new CoreSettingsContainer());
            OxygenHelperClient.registerCurrencyProperties(new OxygenCoinsCurrencyProperties());
            OxygenHelperClient.registerCurrencyProperties(new OxygenShardsCurrencyProperties());
            OxygenHelperClient.registerCurrencyProperties(new OxygenVouchersCurrencyProperties());
            OxygenHelperClient.registerInstantData(new InstantDataHealth());
            OxygenHelperClient.registerInstantData(new InstantDataMaxHealth());
            OxygenHelperClient.registerInstantData(new InstantDataAbsorption());
            OxygenHelperClient.registerInstantData(new InstantDataArmor());
            OxygenHelperClient.registerInstantData(new InstantDataPotionEffects());
            InventoryProviderClient.registerPlayerInventoryProvider(new VanillaPlayerInventoryProvider());
        }
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        this.network.sortPackets();
        ItemsBlackList.loadBlackLists();
        OxygenManagerServer.instance().getPresetsManager().loadPresets();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        CommonReference.registerCommand(event, new CommandOxygenOperator("oxygenop"));
        CommonReference.registerCommand(event, new CommandOxygenServer("oxygens"));
        OxygenManagerServer.instance().getChatChannelsManager().initChannels(event);

        String 
        worldName = event.getServer().getFolderName(),
        worldFolder = event.getServer().isSinglePlayer() ? CommonReference.getGameFolder() + "/saves/" + worldName : CommonReference.getGameFolder() + "/" + worldName;
        LOGGER.info("[Core] Initializing world: {}", worldName);
        OxygenManagerServer.instance().worldLoaded(worldFolder);
        OxygenManagerServer.instance().getPrivilegesContainer().worldLoaded();

        LOGGER.info("[Core] Active common currency provider: <{}>", OxygenManagerServer.instance().getCurrencyManager().getCommonCurrencyProvider().getDisplayName());
        LOGGER.info("[Core] Loaded currency providers:");
        CurrencyHelperServer.getCurrencyProviders()
        .stream()
        .sorted((p1, p2)->p1.getIndex() - p2.getIndex())
        .forEach((provider)->LOGGER.info("[Core]  - index: <{}>, name: <{}>", provider.getIndex(), provider.getDisplayName()));

        MinecraftForge.EVENT_BUS.post(new OxygenWorldLoadedEvent());
    }

    public static void addDefaultPrivileges() {
        if (PrivilegesProviderServer.getRole(OPERATOR_ROLE_ID).getPrivilege(EnumOxygenPrivilege.EXPOSE_OFFLINE_PLAYERS.id()) == null) {
            PrivilegesProviderServer.getRole(OPERATOR_ROLE_ID).addPrivilege(PrivilegeUtils.getPrivilege(EnumOxygenPrivilege.EXPOSE_OFFLINE_PLAYERS.id(), true));
            OxygenManagerServer.instance().getPrivilegesContainer().markChanged();
            LOGGER.info("[Core] Default Operator role privileges added.");
        }
    }

    @EventHandler
    public void serverStopping(FMLServerStoppingEvent event) { 
        OxygenManagerServer.instance().worldUnloaded();
        if (event.getSide() == Side.SERVER)
            OxygenManagerServer.instance().getExecutionManager().shutdown();
    }

    private void initNetwork() {
        network = Network.create(MODID);

        network.registerPacket(CPSyncConfigs.class);
        network.registerPacket(CPSyncMainData.class);
        network.registerPacket(CPSyncPlayerRoles.class);
        network.registerPacket(CPSyncRolesData.class);
        network.registerPacket(CPSyncSharedData.class);
        network.registerPacket(CPShowStatusMessage.class);
        network.registerPacket(CPSyncNotification.class);
        network.registerPacket(CPSyncObservedPlayersData.class);
        network.registerPacket(CPPlaySoundEvent.class);
        network.registerPacket(CPSyncValidDataIds.class);
        network.registerPacket(CPSyncAbsentData.class);
        network.registerPacket(CPSyncPresetsVersions.class);
        network.registerPacket(SPRequestPresetSync.class);
        network.registerPacket(CPSyncPreset.class);
        network.registerPacket(CPSyncWatchedValue.class);
        network.registerPacket(CPAddSharedData.class);
        network.registerPacket(CPRemoveSharedData.class);
        network.registerPacket(CPSyncPrivilegesManagementData.class);
        network.registerPacket(CPRoleAction.class);
        network.registerPacket(CPRolePrivilegeOperation.class);
        network.registerPacket(CPPlayerRolesChanged.class);
        network.registerPacket(CPDefaultPrivilegeOperation.class);
        network.registerPacket(CPSyncInstantData.class);

        network.registerPacket(SPRequestReply.class);
        network.registerPacket(SPSetActivityStatus.class);
        network.registerPacket(SPStartDataSync.class);
        network.registerPacket(SPAbsentDataIds.class);
        network.registerPacket(SPRequestSharedDataSync.class);
        network.registerPacket(SPSetChatFormattingRole.class);
        network.registerPacket(SPRequestPrivilegesData.class);
        network.registerPacket(SPRoleOperation.class);
        network.registerPacket(SPRemoveRole.class);
        network.registerPacket(SPRolePrivilegeOperation.class);
        network.registerPacket(SPDefaultPrivilegeOperation.class);
        network.registerPacket(SPPlayerRoleOperation.class);
    }

    public static Network network() {
        return network;
    }
}
