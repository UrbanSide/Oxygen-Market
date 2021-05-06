package austeretony.oxygen_market.client;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.persistent.AbstractPersistentData;
import austeretony.oxygen_market.client.gui.market.MarketScreen;
import austeretony.oxygen_market.common.main.MarketMain;
import austeretony.oxygen_market.common.market.Deal;
import austeretony.oxygen_market.common.market.SalesHistoryEntry;
import austeretony.oxygen_market.common.network.operation.MarketOperation;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.*;
import java.util.stream.Collectors;

public final class MarketManagerClient extends AbstractPersistentData {

    private static MarketManagerClient instance;

    private final Map<Long, Deal> dealsMap = new HashMap<>();
    private final Map<Long, SalesHistoryEntry> salesHistoryMap = new HashMap<>();

    private MarketManagerClient() {
        OxygenClient.registerPersistentData(this);
    }

    public static MarketManagerClient instance() {
        if (instance == null) {
            instance = new MarketManagerClient();
        }
        return instance;
    }

    public void clientInitialized() {
        OxygenClient.loadPersistentDataAsync(this);
    }

    public Map<Long, Deal> getDealsMap() {
        return dealsMap;
    }

    public List<Deal> getClientPlayerDeals() {
        return dealsMap.values()
                .stream()
                .filter(e -> e.getSellerUUID().equals(OxygenClient.getClientPlayerUUID()))
                .sorted(Comparator.comparingLong(Deal::getId).reversed())
                .collect(Collectors.toList());
    }

    public Map<Long, SalesHistoryEntry> getSalesHistoryMap() {
        return salesHistoryMap;
    }

    public void createDeal(int dealsQuantity, ItemStackWrapper stackWrapper, int quantityPerDeal, long pricePerDeal) {
        OxygenClient.sendToServer(
                MarketMain.MARKET_OPERATIONS_HANDLER_ID,
                MarketOperation.CREATE_DEAL.ordinal(),
                buffer -> {
                    buffer.writeShort(dealsQuantity);
                    stackWrapper.write(buffer);
                    buffer.writeShort(quantityPerDeal);
                    buffer.writeLong(pricePerDeal);
                });
    }

    public void dealsCreated(Set<Long> createdDealsIds, Deal deal, long balance) {
        for (long dealId : createdDealsIds) {
            dealsMap.put(dealId, new Deal(dealId, deal.getSellerUUID(), deal.getSellerUsername(), deal.getStackWrapper(),
                    deal.getQuantity(), deal.getPrice()));
        }
        MarketScreen.dealsCreated(createdDealsIds.size(), deal, balance);
    }

    public void cancelDeals(Set<Long> dealsIds) {
        OxygenClient.sendToServer(
                MarketMain.MARKET_OPERATIONS_HANDLER_ID,
                MarketOperation.CANCEL_DEAL.ordinal(),
                buffer -> {
                    buffer.writeShort(dealsIds.size());
                    for (long dealId : dealsIds) {
                        buffer.writeLong(dealId);
                    }
                });
    }

    public void dealsCanceled(Set<Long> dealsIds) {
        dealsMap.keySet().removeAll(dealsIds);
        MarketScreen.dealsCanceled(dealsIds);
    }

    public void purchase(Set<Long> dealsIds) {
        OxygenClient.sendToServer(
                MarketMain.MARKET_OPERATIONS_HANDLER_ID,
                MarketOperation.PURCHASE.ordinal(),
                buffer -> {
                    buffer.writeShort(dealsIds.size());
                    for (long dealId : dealsIds) {
                        buffer.writeLong(dealId);
                    }
                });
    }

    public void purchased(Set<Long> dealsIds, long balance) {
        dealsMap.keySet().removeAll(dealsIds);
        MarketScreen.purchased(dealsIds, balance);
    }

    public void purchaseFailed(Set<Long> dealsIds) {
        MarketScreen.purchaseFailed(dealsIds);
    }

    @Override
    public String getName() {
        return "market:market_client_data";
    }

    @Override
    public String getPath() {
        return OxygenClient.getDataFolder() + "/client/market/market.dat";
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
