package austeretony.oxygen_market.server;

import austeretony.oxygen_core.common.api.OxygenCommon;
import austeretony.oxygen_core.common.chat.StatusMessageType;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.persistent.AbstractPersistentData;
import austeretony.oxygen_core.common.sound.SoundEffects;
import austeretony.oxygen_core.common.util.CommonUtils;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_core.common.util.MinecraftCommon;
import austeretony.oxygen_core.server.api.OxygenServer;
import austeretony.oxygen_core.server.api.PrivilegesServer;
import austeretony.oxygen_core.server.operation.Operation;
import austeretony.oxygen_mail.common.mail.Attachments;
import austeretony.oxygen_mail.server.api.MailServer;
import austeretony.oxygen_market.common.config.MarketConfig;
import austeretony.oxygen_market.common.main.MarketMain;
import austeretony.oxygen_market.common.main.MarketPrivileges;
import austeretony.oxygen_market.common.market.Deal;
import austeretony.oxygen_market.common.market.SalesHistoryEntry;
import austeretony.oxygen_market.common.network.operation.MarketOperation;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.*;
import java.util.concurrent.TimeUnit;

public final class MarketManagerServer extends AbstractPersistentData {

    private static MarketManagerServer instance;

    private final Map<Long, Deal> dealsMap = new HashMap<>();
    private final Map<Long, SalesHistoryEntry> salesHistoryMap = new HashMap<>();

    // deals with transaction in progress
    private final Set<Long> processedDealsIds = new HashSet<>();

    private MarketManagerServer() {
        OxygenServer.registerPersistentData(this);
        OxygenServer.scheduleTask(this::runScheduledTasks, 1L, TimeUnit.HOURS);
    }

    private void runScheduledTasks() {
        processExpiredDeals();
        processExpiredSalesHistoryEntries();
    }

    private void processExpiredDeals() {
        final Runnable task = () -> {
            int processed = 0;

            long currentTimeMillis = OxygenServer.getCurrentTimeMillis();
            long expirationTimeMillis = TimeUnit.HOURS.toMillis(MarketConfig.DEAL_EXPIRE_TIME_HOURS.asInt());
            Iterator<Deal> iterator = dealsMap.values().iterator();

            while (iterator.hasNext()) {
                Deal deal = iterator.next();
                if (deal == null) continue;

                boolean isExpired = currentTimeMillis - deal.getId() > expirationTimeMillis;
                if (isExpired) {
                    MailServer.systemMail(deal.getSellerUUID(), "oxygen_market.mail.return.subject")
                            .withSenderName("oxygen_market.mail.sender")
                            .withMessage("oxygen_market.mail.return.message")
                            .withAttachment(Attachments.parcel(deal.getStackWrapper(), deal.getQuantity()))
                            .withMailBoxCapacityIgnore()
                            .send();

                    processed++;
                    iterator.remove();
                }
            }
            if (processed > 0) {
                markChanged();
            }
            OxygenMain.logInfo(1, "[Market] Expired deals processed: {} deals in total.", processed);
        };
        OxygenServer.addTask(task);
    }

    private void processExpiredSalesHistoryEntries() {
        final Runnable task = () -> {
            long currentTimeMillis = OxygenServer.getCurrentTimeMillis();
            long expirationTimeMillis = TimeUnit.HOURS.toMillis(MarketConfig.SALES_HISTORY_ENTRY_EXPIRE_TIME_HOURS.asInt());
            Iterator<SalesHistoryEntry> iterator = salesHistoryMap.values().iterator();

            while (iterator.hasNext()) {
                SalesHistoryEntry entry = iterator.next();
                if (entry == null) continue;

                boolean isExpired = currentTimeMillis - entry.getId() > expirationTimeMillis;
                if (isExpired) {
                    iterator.remove();
                }
            }
            OxygenMain.logInfo(1, "[Market] Expired sales history entries processed.");
        };
        OxygenServer.addTask(task);
    }

    public static MarketManagerServer instance() {
        if (instance == null) {
            instance = new MarketManagerServer();
        }
        return instance;
    }

    public void serverStarting() {
        final Runnable task = () -> {
            OxygenServer.loadPersistentData(this);
            runScheduledTasks();
        };
        OxygenServer.addTask(task);
    }

    public Map<Long, Deal> getDealsMap() {
        return dealsMap;
    }

    public int getPlayerDealsAmount(UUID playerUUID) {
        int amount = 0;
        for (Deal deal : dealsMap.values()) {
            if (deal.getSellerUUID().equals(playerUUID)) {
                amount++;
            }
        }
        return amount;
    }

    public Map<Long, SalesHistoryEntry> getSalesHistoryMap() {
        return salesHistoryMap;
    }

    public void createDeal(EntityPlayerMP playerMP, int dealsQuantity, ItemStackWrapper stackWrapper, int quantityPerDeal,
                           long pricePerDeal) {
        if (dealsQuantity <= 0 || quantityPerDeal <= 0 || pricePerDeal <= 0L) {
            OxygenServer.sendStatusMessage(playerMP, MarketMain.MODULE_INDEX, StatusMessageType.ERROR,
                    "oxygen.status_message.invalid_operation");
            return;
        }

        UUID playerUUID = MinecraftCommon.getEntityUUID(playerMP);
        if (!PrivilegesServer.getBoolean(playerUUID, MarketPrivileges.MARKET_ACCESS.getId(),
                MarketConfig.ENABLE_MARKET_ACCESS.asBoolean())) {
            OxygenServer.sendStatusMessage(playerMP, MarketMain.MODULE_INDEX, StatusMessageType.ERROR,
                    "oxygen.status_message.no_access");
            return;
        }

        if (!MarketConfig.ENABLE_MARKET_ACCESS_CLIENT_SIDE.asBoolean()
                && OxygenServer.isTimeout(MarketMain.TIMEOUT_MARKET_OPERATIONS, playerUUID)) {
            OxygenServer.sendStatusMessage(playerMP, MarketMain.MODULE_INDEX, StatusMessageType.ERROR,
                    "oxygen.status_message.operation_timeout");
            return;
        }

        if (OxygenServer.isItemBlacklisted(MarketMain.ITEMS_BLACKLIST_MARKET, stackWrapper)) {
            OxygenServer.sendStatusMessage(playerMP, MarketMain.MODULE_INDEX, StatusMessageType.ERROR,
                    "oxygen.status_message.item_blacklisted");
            return;
        }

        int maxDeals = PrivilegesServer.getInt(playerUUID, MarketPrivileges.MAX_DEALS_PER_PLAYER.getId(),
                MarketConfig.MAX_DEALS_PER_PLAYER.asInt());
        if (dealsQuantity > Math.max(maxDeals - getPlayerDealsAmount(playerUUID), 0)) {
            OxygenServer.sendStatusMessage(playerMP, MarketMain.MODULE_INDEX, StatusMessageType.ERROR,
                    "oxygen_market.status_message.too_many_deals");
            return;
        }

        int maxStack = PrivilegesServer.getInt(playerUUID, MarketPrivileges.DEAL_MAX_STACK_SIZE.getId(),
                MarketConfig.DEAL_MAX_STACK_SIZE.asInt());
        if (maxStack < 0) {
            maxStack = OxygenCommon.getMaxItemStackSize(stackWrapper);
        }
        if (quantityPerDeal > maxStack) {
            OxygenServer.sendStatusMessage(playerMP, MarketMain.MODULE_INDEX, StatusMessageType.ERROR,
                    "oxygen_market.status_message.too_many_items");
            return;
        }

        long maxPrice = PrivilegesServer.getLong(playerUUID, MarketPrivileges.PRICE_MAX_VALUE.getId(),
                MarketConfig.PRICE_MAX_VALUE.asLong());
        if (pricePerDeal > maxPrice) {
            OxygenServer.sendStatusMessage(playerMP, MarketMain.MODULE_INDEX, StatusMessageType.ERROR,
                    "oxygen_market.status_message.too_high_price");
            return;
        }

        float feePercent = PrivilegesServer.getFloat(playerUUID, MarketPrivileges.DEAL_PLACEMENT_FEE_PERCENT.getId(),
                MarketConfig.DEAL_PLACEMENT_FEE_PERCENT.asFloat());
        long fee = (long) (pricePerDeal * MathUtils.clamp(feePercent, 0F, 1F));
        if (fee < 0L) {
            fee = 0L;
        }

        long finalFee = fee;
        final Runnable successTask = () -> {
            Deal deal = null;
            Set<Long> createdDealsIds = new HashSet<>(dealsQuantity);

            long now = OxygenServer.getCurrentTimeMillis();
            for (int i = 0; i < dealsQuantity; i++) {
                long dealId = CommonUtils.createId(now + i, dealsMap.keySet());
                createdDealsIds.add(dealId);

                deal = new Deal(dealId, playerUUID, MinecraftCommon.getEntityName(playerMP), stackWrapper,
                        quantityPerDeal, pricePerDeal);
                dealsMap.put(dealId, deal);
            }
            markChanged();

            if (finalFee > 0L) {
                OxygenServer.playSound(playerMP, SoundEffects.miscRingingCoins);
            }
            OxygenServer.playSound(playerMP, SoundEffects.miscInventoryOperation);

            OxygenMain.logInfo(2, "[Market] {} placed {} deal(s): {}", playerUUID, dealsQuantity, deal);
            OxygenServer.sendStatusMessage(playerMP, MarketMain.MODULE_INDEX, StatusMessageType.COMMON,
                    "oxygen_market.status_message.deal_created");

            Deal finalDeal = deal;
            long balance = OxygenServer.getWatcherValue(playerUUID, OxygenMain.CURRENCY_COINS, 0L);
            OxygenServer.sendToClient(
                    playerMP,
                    MarketMain.MARKET_OPERATIONS_HANDLER_ID,
                    MarketOperation.DEAL_CREATED.ordinal(),
                    buffer -> {
                        buffer.writeShort(createdDealsIds.size());
                        for (long dealId : createdDealsIds) {
                            buffer.writeLong(dealId);
                        }
                        finalDeal.write(buffer);
                        buffer.writeLong(balance);
                    });
        };

        Operation.of(MarketMain.OPERATION_DEAL_CREATION, playerMP)
                .withSuccessTask(successTask)
                .withFailTask(reason -> OxygenServer.sendMessageOnOperationFail(playerMP, reason, MarketMain.MODULE_INDEX))
                .withCurrencyWithdraw(OxygenMain.CURRENCY_COINS, fee * dealsQuantity)
                .withItemWithdraw(stackWrapper, quantityPerDeal * dealsQuantity)
                .process();
    }

    public void cancelDeal(EntityPlayerMP playerMP, Set<Long> dealsIds) {
        UUID playerUUID = MinecraftCommon.getEntityUUID(playerMP);
        if (!MarketConfig.ENABLE_MARKET_ACCESS_CLIENT_SIDE.asBoolean()
                && OxygenServer.isTimeout(MarketMain.TIMEOUT_MARKET_OPERATIONS, playerUUID)) {
            OxygenServer.sendStatusMessage(playerMP, MarketMain.MODULE_INDEX, StatusMessageType.ERROR,
                    "oxygen.status_message.operation_timeout");
            return;
        }

        Set<Deal> dealsSet = new HashSet<>();
        for (long id : dealsIds) {
            Deal deal = dealsMap.get(id);
            if (deal != null) {
                dealsSet.add(deal);
            }
        }
        if (dealsSet.isEmpty()) return;

        Deal sample = dealsSet.iterator().next();
        dealsSet.removeIf(e -> !e.isEqual(sample)
                || processedDealsIds.contains(e.getId())
                || !e.getSellerUUID().equals(playerUUID));

        if (!MailServer.canReceiveMail(playerUUID)) {
            OxygenServer.sendStatusMessage(playerMP, MarketMain.MODULE_INDEX, StatusMessageType.ERROR,
                    "oxygen_market.status_message.mailbox_is_full");
            return;
        }

        MailServer.systemMail(sample.getSellerUUID(), "oxygen_market.mail.cancel.subject")
                .withSenderName("oxygen_market.mail.sender")
                .withMessage("oxygen_market.mail.cancel.message")
                .withAttachment(Attachments.parcel(sample.getStackWrapper(),
                        Math.min(sample.getQuantity() * dealsSet.size(), Short.MAX_VALUE)))
                .send();

        for (Deal deal : dealsSet) {
            dealsMap.remove(deal.getId());
        }
        markChanged();

        OxygenMain.logInfo(2, "[Market] {} canceled deal: {} of {}", playerUUID, dealsSet.size(), sample);
        OxygenServer.sendStatusMessage(playerMP, MarketMain.MODULE_INDEX, StatusMessageType.COMMON,
                "oxygen_market.status_message.deal_canceled");

        OxygenServer.sendToClient(
                playerMP,
                MarketMain.MARKET_OPERATIONS_HANDLER_ID,
                MarketOperation.DEAL_CANCELED.ordinal(),
                buffer -> {
                    buffer.writeShort(dealsIds.size());
                    for (long dealId : dealsIds) {
                        buffer.writeLong(dealId);
                    }
                });
    }

    public void purchase(EntityPlayerMP playerMP, Set<Long> dealsIds) {
        UUID playerUUID = MinecraftCommon.getEntityUUID(playerMP);
        if (!PrivilegesServer.getBoolean(playerUUID, MarketPrivileges.MARKET_ACCESS.getId(),
                MarketConfig.ENABLE_MARKET_ACCESS.asBoolean())) {
            return;
        }

        if (!MarketConfig.ENABLE_MARKET_ACCESS_CLIENT_SIDE.asBoolean()
                && OxygenServer.isTimeout(MarketMain.TIMEOUT_MARKET_OPERATIONS, playerUUID)) {
            OxygenServer.sendStatusMessage(playerMP, MarketMain.MODULE_INDEX, StatusMessageType.ERROR,
                    "oxygen.status_message.operation_timeout");
            return;
        }

        Set<Deal> dealsSet = new HashSet<>();
        for (long id : dealsIds) {
            Deal deal = dealsMap.get(id);
            if (deal != null) {
                dealsSet.add(deal);
            }
        }

        if (dealsSet.isEmpty()) {
            OxygenServer.sendToClient(
                    playerMP,
                    MarketMain.MARKET_OPERATIONS_HANDLER_ID,
                    MarketOperation.PURCHASE_FAILED.ordinal(),
                    buffer -> {
                        buffer.writeShort(dealsIds.size());
                        for (long dealId : dealsIds) {
                            buffer.writeLong(dealId);
                        }
                    });

            OxygenServer.sendStatusMessage(playerMP, MarketMain.MODULE_INDEX, StatusMessageType.ERROR,
                    "oxygen_market.status_message.deal_not_found");
            return;
        }

        Deal sample = dealsSet.iterator().next();
        if (!MarketConfig.ENABLE_SELF_PURCHASE.asBoolean() && sample.getSellerUUID().equals(playerUUID)) {
            OxygenServer.sendStatusMessage(playerMP, MarketMain.MODULE_INDEX, StatusMessageType.ERROR,
                    "oxygen_market.status_message.self_purchase_disabled");
            return;
        }

        dealsSet.removeIf(e -> !e.isEqual(sample) || processedDealsIds.contains(e.getId()));
        if (dealsSet.isEmpty()) {
            OxygenServer.sendToClient(
                    playerMP,
                    MarketMain.MARKET_OPERATIONS_HANDLER_ID,
                    MarketOperation.PURCHASE_FAILED.ordinal(),
                    buffer -> {
                        buffer.writeShort(dealsIds.size());
                        for (long dealId : dealsIds) {
                            buffer.writeLong(dealId);
                        }
                    });

            OxygenServer.sendStatusMessage(playerMP, MarketMain.MODULE_INDEX, StatusMessageType.ERROR,
                    "oxygen_market.status_message.deal_not_found");
            return;
        }

        if (!MailServer.canReceiveMail(playerUUID)) {
            OxygenServer.sendStatusMessage(playerMP, MarketMain.MODULE_INDEX, StatusMessageType.ERROR,
                    "oxygen_market.status_message.mailbox_is_full");
            return;
        }

        final Runnable successTask = () -> {
            for (Deal deal : dealsSet) {
                dealsMap.remove(deal.getId());
            }
            sendPurchasedItemToBuyer(playerMP, sample, dealsSet.size());
            sendIncomeToSeller(playerMP, sample, dealsSet.size());

            if (MarketConfig.ENABLE_SALES_HISTORY.asBoolean()) {
                for (Deal deal : dealsSet) {
                    long entryId = CommonUtils.createId(OxygenServer.getCurrentTimeMillis(), salesHistoryMap.keySet());
                    SalesHistoryEntry historyEntry = new SalesHistoryEntry(entryId, deal.getStackWrapper(), deal.getQuantity(),
                            deal.getPrice());
                    salesHistoryMap.put(entryId, historyEntry);
                }
            }
            markChanged();

            OxygenServer.playSound(playerMP, SoundEffects.miscRingingCoins);
            OxygenServer.playSound(playerMP, SoundEffects.miscInventoryOperation);

            OxygenMain.logInfo(2, "[Market] {} purchased: {} of {}", playerUUID, dealsSet.size(), sample);
            OxygenServer.sendStatusMessage(playerMP, MarketMain.MODULE_INDEX, StatusMessageType.COMMON,
                    "oxygen_market.status_message.purchased");

            long balance = OxygenServer.getWatcherValue(playerUUID, OxygenMain.CURRENCY_COINS, 0L);
            OxygenServer.sendToClient(
                    playerMP,
                    MarketMain.MARKET_OPERATIONS_HANDLER_ID,
                    MarketOperation.PURCHASED.ordinal(),
                    buffer -> {
                        buffer.writeShort(dealsIds.size());
                        for (long dealId : dealsIds) {
                            buffer.writeLong(dealId);
                        }
                        buffer.writeLong(balance);
                    });

            for (Deal deal : dealsSet) {
                processedDealsIds.remove(deal.getId());
            }
        };

        for (Deal deal : dealsSet) {
            processedDealsIds.add(deal.getId()); // this should prevent deals from being purchased and cancelled simultaneously
        }
        Operation.of(MarketMain.OPERATION_PURCHASE, playerMP)
                .withSuccessTask(successTask)
                .withFailTask(reason -> {
                    OxygenServer.sendMessageOnOperationFail(playerMP, reason, MarketMain.MODULE_INDEX);
                    for (Deal deal : dealsSet) {
                        processedDealsIds.remove(deal.getId());
                    }
                })
                .withCurrencyWithdraw(OxygenMain.CURRENCY_COINS, sample.getPrice() * dealsSet.size())
                .process();
    }

    private void sendPurchasedItemToBuyer(EntityPlayerMP buyer, Deal deal, int boughtQuantity) {
        MailServer.systemMail(MinecraftCommon.getEntityUUID(buyer), "oxygen_market.mail.purchase.subject")
                .withSenderName("oxygen_market.mail.sender")
                .withMessage("oxygen_market.mail.purchase.message", deal.getSellerUsername())
                .withAttachment(Attachments.parcel(deal.getStackWrapper(),
                        Math.min(deal.getQuantity() * boughtQuantity, Short.MAX_VALUE)))
                .withMailBoxCapacityIgnore()
                .send();
    }

    private void sendIncomeToSeller(EntityPlayerMP buyer, Deal deal, int boughtQuantity) {
        float feePercent = PrivilegesServer.getFloat(MinecraftCommon.getEntityUUID(buyer),
                MarketPrivileges.DEAL_SALE_FEE_PERCENT.getId(), MarketConfig.DEAL_SALE_FEE_PERCENT.asFloat());
        long totalPrice = deal.getPrice() * boughtQuantity;
        long income = totalPrice - (long) (totalPrice * MathUtils.clamp(feePercent, 0F, 1F));
        if (income < 0L) {
            income = 0L;
        }

        MailServer.systemMail(deal.getSellerUUID(), "oxygen_market.mail.income.subject")
                .withSenderName("oxygen_market.mail.sender")
                .withMessage("oxygen_market.mail.income.message",
                        MinecraftCommon.getEntityName(buyer),
                        deal.getStackWrapper().getItemStackCached().getUnlocalizedName() + ".name")
                .withAttachment(Attachments.remittance(OxygenMain.CURRENCY_COINS, income))
                .withMailBoxCapacityIgnore()
                .send();
    }

    @Override
    public String getName() {
        return "market:market_server_data";
    }

    @Override
    public String getPath() {
        return OxygenServer.getDataFolder() + "/world/market/market_data.dat";
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        NBTTagList dealsList = new NBTTagList();
        for (Deal deal : dealsMap.values()) {
            dealsList.appendTag(deal.writeToNBT());
        }
        tagCompound.setTag("deals_list", dealsList);

        NBTTagList salesList = new NBTTagList();
        for (SalesHistoryEntry entry : salesHistoryMap.values()) {
            salesList.appendTag(entry.writeToNBT());
        }
        tagCompound.setTag("sales_list", salesList);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        NBTTagList dealsList = tagCompound.getTagList("deals_list", 10);
        for (int i = 0; i < dealsList.tagCount(); i++) {
            Deal deal = Deal.readFromNBT(dealsList.getCompoundTagAt(i));
            dealsMap.put(deal.getId(), deal);
        }

        NBTTagList salesList = tagCompound.getTagList("sales_list", 10);
        for (int i = 0; i < salesList.tagCount(); i++) {
            SalesHistoryEntry entry = SalesHistoryEntry.readFromNBT(salesList.getCompoundTagAt(i));
            salesHistoryMap.put(entry.getId(), entry);
        }
    }

    @Override
    public void reset() {
        dealsMap.clear();
        salesHistoryMap.clear();
    }
}
