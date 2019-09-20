package austeretony.oxygen_trade.server;

import java.util.Iterator;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.currency.CurrencyHelperServer;
import austeretony.oxygen_core.common.inventory.InventoryHelper;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.sound.OxygenSoundEffects;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_core.server.OxygenManagerServer;
import austeretony.oxygen_core.server.OxygenPlayerData;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.PrivilegeProviderServer;
import austeretony.oxygen_core.server.api.SoundEventHelperServer;
import austeretony.oxygen_core.server.api.WatcherHelperServer;
import austeretony.oxygen_mail.common.Parcel;
import austeretony.oxygen_mail.server.api.MailHelperServer;
import austeretony.oxygen_trade.common.config.TradeConfig;
import austeretony.oxygen_trade.common.main.EnumOfferAction;
import austeretony.oxygen_trade.common.main.EnumTradePrivilege;
import austeretony.oxygen_trade.common.main.EnumTradeStatusMessage;
import austeretony.oxygen_trade.common.main.TradeMain;
import austeretony.oxygen_trade.common.network.client.CPOfferAction;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class OffersManagerServer {

    private final TradeManagerServer manager;

    private final Queue<QueuedOfferCreation> offerCreationQueue = new ConcurrentLinkedQueue<>();

    private final Queue<QueuedOfferActionServer> offerActionsQueue = new ConcurrentLinkedQueue<>();

    public OffersManagerServer(TradeManagerServer manager) {
        this.manager = manager;
        OxygenManagerServer.instance().getExecutionManager().getExecutors().getSchedulerExecutorService().scheduleAtFixedRate(
                ()->{
                    this.processOfferCreationQueue();
                    this.processOfferActionsQueue();
                }, 1000L, 250L, TimeUnit.MILLISECONDS);
    }

    public void informPlayer(EntityPlayerMP playerMP, EnumTradeStatusMessage status) {
        OxygenHelperServer.sendStatusMessage(playerMP, TradeMain.TRADE_MOD_INDEX, status.ordinal());
    }

    public void processExpiredOffers() {
        OxygenHelperServer.addRoutineTask(()->{
            if (this.manager.getOffersContainer().getOffersAmount() > 0) {
                Iterator<PlayerOfferServer> iterator = this.manager.getOffersContainer().getOffers().iterator();
                PlayerOfferServer offer;
                long 
                currTimeMillis = System.currentTimeMillis(),
                expireTimeMillis = TradeConfig.OFFER_EXPIRE_TIME_HOURS.getIntValue() * 3_600_000L;
                int removed = 0;
                while (iterator.hasNext()) {
                    offer = iterator.next();
                    if (currTimeMillis - offer.getId() > expireTimeMillis) {
                        iterator.remove();
                        this.returnItemToSeller(offer);
                        removed++;
                    }
                }
                if (removed > 0)
                    this.manager.getOffersContainer().setChanged(true);
                TradeMain.LOGGER.info("Removed <{}> expired offers.", removed);
            }
        });
    }

    private void returnExpiredItemToSeller(PlayerOfferServer offer) {
        MailHelperServer.sendSystemPackage(
                offer.getPlayerUUID(), 
                "mail.sender.sys", 
                "trade.expired", 
                "trade.expiredOfferMessage",
                Parcel.create(offer.getOfferedStack(), offer.getAmount()));
    }

    public int getPlayerOffersAmount(UUID playerUUID) {
        int amount = 0;
        for (PlayerOfferServer offer : this.manager.getOffersContainer().getOffers())
            if (offer.getPlayerUUID().equals(playerUUID))
                amount++;
        return amount;
    }

    public boolean canCreateOffer(UUID playerUUID) {
        return this.getPlayerOffersAmount(playerUUID) < PrivilegeProviderServer.getValue(playerUUID, EnumTradePrivilege.MAX_OFFERS_PER_PLAYER.toString(), TradeConfig.MAX_OFFERS_PER_PLAYER.getIntValue());
    }

    public void createOffer(EntityPlayerMP playerMP, ItemStackWrapper stackWrapper, int amount, long price) {
        this.offerCreationQueue.offer(new QueuedOfferCreation(CommonReference.getPersistentUUID(playerMP), stackWrapper, amount, price));
    }

    private void processOfferCreationQueue() {
        while (!this.offerCreationQueue.isEmpty()) {
            final QueuedOfferCreation creation = this.offerCreationQueue.poll();
            final EntityPlayerMP playerMP = CommonReference.playerByUUID(creation.playerUUID);
            if (playerMP != null)
                OxygenHelperServer.addRoutineTask(()->{
                    if (this.tryToCreateOffer(playerMP, creation.playerUUID, creation.stackWrapper, creation.amount, creation.price))
                        this.informPlayer(playerMP, EnumTradeStatusMessage.OFFER_CREATED);
                    else
                        this.informPlayer(playerMP, EnumTradeStatusMessage.OFFER_CREATION_FAILED);
                });
        }
    }

    private boolean tryToCreateOffer(EntityPlayerMP playerMP, UUID playerUUID, ItemStackWrapper stackWrapper, int amount, long price) {
        int maxAmount = PrivilegeProviderServer.getValue(playerUUID, EnumTradePrivilege.ITEMS_PER_OFFER_MAX_AMOUNT.toString(), TradeConfig.ITEMS_PER_OFFER_MAX_AMOUNT.getIntValue());
        if (maxAmount < 0)
            maxAmount = stackWrapper.getItemStack().getMaxStackSize();
        long maxPrice = PrivilegeProviderServer.getValue(playerUUID, EnumTradePrivilege.PRICE_MAX_VALUE.toString(), TradeConfig.PRICE_MAX_VALUE.getLongValue());
        if (this.validateItem(playerMP, stackWrapper) 
                && amount > 0 && amount <= maxAmount 
                && price > 0 && price <= maxPrice) {
            if (this.canCreateOffer(playerUUID)) {
                long offerCreationFee = MathUtils.percentValueOf(price, PrivilegeProviderServer.getValue(
                        playerUUID, 
                        EnumTradePrivilege.OFFER_CREATION_FEE_PERCENT.toString(), 
                        TradeConfig.OFFER_CREATION_FEE_PERCENT.getIntValue()));
                boolean feeExist = offerCreationFee > 0;
                if (InventoryHelper.getEqualStackAmount(playerMP, stackWrapper) >= amount 
                        && (feeExist || CurrencyHelperServer.enoughCurrency(playerUUID, offerCreationFee))) {
                    CommonReference.delegateToServerThread(()->InventoryHelper.removeEqualStack(playerMP, stackWrapper, amount));
                    if (feeExist) {
                        CurrencyHelperServer.removeCurrency(playerUUID, offerCreationFee);
                        CurrencyHelperServer.save(playerUUID);
                        WatcherHelperServer.setValue(playerUUID, OxygenPlayerData.CURRENCY_COINS_WATCHER_ID, CurrencyHelperServer.getCurrency(playerUUID));
                        SoundEventHelperServer.playSoundClient(playerMP, OxygenSoundEffects.SELL.id);
                    }
                    PlayerOfferServer offer = new PlayerOfferServer(this.createOfferId(), playerUUID, stackWrapper, amount, price);
                    this.addOffer(offer);
                    OxygenMain.network().sendTo(new CPOfferAction(EnumOfferAction.CREATION, offer, CurrencyHelperServer.getCurrency(playerUUID)), playerMP);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean validateItem(EntityPlayerMP playerMP, ItemStackWrapper stackWrapper) {
        if (stackWrapper.itemId == Item.getIdFromItem(Items.AIR)) {
            this.informPlayer(playerMP, EnumTradeStatusMessage.ITEM_DAMAGED);
            return false;
        }
        if (this.manager.getItemsBlackList().isBlackListed(Item.getItemById(stackWrapper.itemId))) {
            this.informPlayer(playerMP, EnumTradeStatusMessage.ITEM_BLACKLISTED);
            return false;
        }
        return true;
    }

    private void addOffer(PlayerOfferServer offer) {
        this.manager.getOffersContainer().addOffer(offer);
        this.manager.getOffersContainer().setChanged(true);
    }

    private long createOfferId() {
        long id = System.currentTimeMillis();
        while (this.manager.getOffersContainer().isOfferExist(id))
            id++;
        return id;
    }

    public void purchaseItem(EntityPlayerMP playerMP, long offerId) {
        this.offerActionsQueue.offer(new QueuedOfferActionServer(CommonReference.getPersistentUUID(playerMP), EnumOfferAction.PURCHASE, offerId));
    }

    public void cancelOffer(EntityPlayerMP playerMP, long offerId) {
        this.offerActionsQueue.offer(new QueuedOfferActionServer(CommonReference.getPersistentUUID(playerMP), EnumOfferAction.CANCEL, offerId));
    }

    private void processOfferActionsQueue() {
        while (!this.offerActionsQueue.isEmpty()) {
            final QueuedOfferActionServer action = this.offerActionsQueue.poll();
            final EntityPlayerMP playerMP = CommonReference.playerByUUID(action.playerUUID);
            if (playerMP != null)
                OxygenHelperServer.addRoutineTask(()->this.processOfferAction(playerMP, action.action, action.offerId));
        }
    }

    private void processOfferAction(EntityPlayerMP playerMP, EnumOfferAction action, long offerId) {
        switch (action) {
        case PURCHASE:
            this.purchase(playerMP, offerId);
            break;
        case CANCEL:
            this.cancel(playerMP, offerId);
            break;
        default:
            break;   
        }
    }

    private void purchase(EntityPlayerMP playerMP, long offerId) {
        if (this.tryToPurchaseItem(playerMP, offerId))
            this.informPlayer(playerMP, EnumTradeStatusMessage.ITEM_PURCHASED);
        else
            this.informPlayer(playerMP, EnumTradeStatusMessage.ITEM_PURCHASE_FAILED);
    }

    private boolean tryToPurchaseItem(EntityPlayerMP playerMP, long offerId) {
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        if (this.manager.getOffersContainer().isOfferExist(offerId)) {
            PlayerOfferServer offer = this.manager.getOffersContainer().getOffer(offerId);
            synchronized (offer) {//prevents simultaneously specific item purchase and cancellation 
                if (this.manager.getOffersContainer().isOfferExist(offerId)) {
                    ItemStack itemStack = offer.getOfferedStack().getItemStack();
                    if (InventoryHelper.haveEnoughSpace(playerMP, offer.getAmount(), itemStack.getMaxStackSize()) 
                            && CurrencyHelperServer.enoughCurrency(playerUUID, offer.getPrice())) {
                        CurrencyHelperServer.removeCurrency(playerUUID, offer.getPrice());
                        CurrencyHelperServer.save(playerUUID);
                        WatcherHelperServer.setValue(playerUUID, OxygenPlayerData.CURRENCY_COINS_WATCHER_ID, CurrencyHelperServer.getCurrency(playerUUID));
                        SoundEventHelperServer.playSoundClient(playerMP, OxygenSoundEffects.SELL.id);
                        this.removeOffer(offerId);
                        this.addSalesHsistoryRecord(offer, playerUUID);
                        this.sendItemToBuyer(playerUUID, offer);
                        this.sendSaleIncomeToSeller(offer);
                        OxygenMain.network().sendTo(new CPOfferAction(EnumOfferAction.PURCHASE, offer, CurrencyHelperServer.getCurrency(playerUUID)), playerMP);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void removeOffer(long offerId) {
        this.manager.getOffersContainer().removeOffer(offerId);
        this.manager.getOffersContainer().setChanged(true);
    }

    private void addSalesHsistoryRecord(PlayerOfferServer offer, UUID buyerUUID) {
        if (TradeConfig.ENABLE_SALES_HISTORY.getBooleanValue()) {
            this.manager.getSalesHistoryContainer().addEntry(SalesHistoryEntryServer.fromOffer(offer, buyerUUID));
            this.manager.getSalesHistoryContainer().setChanged(true);
        }
    }

    private void sendItemToBuyer(UUID buyerUUID, PlayerOfferServer offer) {
        MailHelperServer.sendSystemPackage(
                buyerUUID, 
                "mail.sender.sys", 
                "trade.purchased", 
                "trade.purchasedItemMessage",
                Parcel.create(offer.getOfferedStack(), offer.getAmount()));
    }

    private void sendSaleIncomeToSeller(PlayerOfferServer offer) {
        long saleFee = MathUtils.percentValueOf(offer.getPrice(), PrivilegeProviderServer.getValue(offer.getPlayerUUID(), EnumTradePrivilege.OFFER_SALE_FEE_PERCENT.toString(), TradeConfig.OFFER_SALE_FEE_PERCENT.getIntValue()));
        MailHelperServer.sendSystemRemittance(
                offer.getPlayerUUID(), 
                "mail.sender.sys", 
                "trade.sold", 
                "trade.soldItemMessage",
                offer.getPrice() - saleFee);
    }

    public void cancel(EntityPlayerMP playerMP, long offerId) {
        if (this.tryToCancelOffer(playerMP, offerId))
            this.informPlayer(playerMP, EnumTradeStatusMessage.OFFER_CANCELED);
        else
            this.informPlayer(playerMP, EnumTradeStatusMessage.OFFER_CANCELLATION_FAILED);
    }

    private boolean tryToCancelOffer(EntityPlayerMP playerMP, long offerId) {
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        if (this.manager.getOffersContainer().isOfferExist(offerId)) {
            PlayerOfferServer offer = this.manager.getOffersContainer().getOffer(offerId);
            synchronized (offer) {//prevents simultaneously specific item purchase and cancellation 
                if (this.manager.getOffersContainer().isOfferExist(offerId)) {
                    if (offer.isOwner(playerUUID)) {
                        this.removeOffer(offerId);
                        this.returnItemToSeller(offer);
                        OxygenMain.network().sendTo(new CPOfferAction(EnumOfferAction.CANCEL, offer, CurrencyHelperServer.getCurrency(playerUUID)), playerMP);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void returnItemToSeller(PlayerOfferServer offer) {
        MailHelperServer.sendSystemPackage(
                offer.getPlayerUUID(), 
                "mail.sender.sys", 
                "trade.cancel", 
                "trade.cancelOfferMessage",
                Parcel.create(offer.getOfferedStack(), offer.getAmount()));
    }
}
