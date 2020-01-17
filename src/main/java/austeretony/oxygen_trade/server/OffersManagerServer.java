package austeretony.oxygen_trade.server;

import java.util.Iterator;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.inventory.InventoryHelper;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.sound.OxygenSoundEffects;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_core.server.api.CurrencyHelperServer;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.PrivilegesProviderServer;
import austeretony.oxygen_core.server.api.SoundEventHelperServer;
import austeretony.oxygen_mail.common.Parcel;
import austeretony.oxygen_mail.server.MailManagerServer;
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
    }

    public void informPlayer(EntityPlayerMP playerMP, EnumTradeStatusMessage status) {
        OxygenHelperServer.sendStatusMessage(playerMP, TradeMain.TRADE_MOD_INDEX, status.ordinal());
    }

    public void processExpiredOffers() {
        OxygenHelperServer.addRoutineTask(()->{
            if (this.manager.getOffersContainer().getOffersAmount() > 0) {
                Iterator<OfferServer> iterator = this.manager.getOffersContainer().getOffers().iterator();
                OfferServer offer;
                long 
                currTimeMillis = System.currentTimeMillis(),
                expireTimeMillis = TradeConfig.OFFER_EXPIRE_TIME_HOURS.asInt() * 3_600_000L;
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
                TradeMain.LOGGER.info("Removed {} expired offers.", removed);
            }
        });
    }

    private void returnExpiredItemToSeller(OfferServer offer) {
        MailHelperServer.sendSystemPackage(
                offer.getPlayerUUID(), 
                "mail.sender.sys", 
                "trade.expired", 
                "trade.expiredOfferMessage",
                Parcel.create(offer.getOfferedStack(), offer.getAmount()),
                true);
    }

    public int getPlayerOffersAmount(UUID playerUUID) {
        int amount = 0;
        for (OfferServer offer : this.manager.getOffersContainer().getOffers())
            if (offer.getPlayerUUID().equals(playerUUID))
                amount++;
        return amount;
    }

    public boolean canCreateOffer(UUID playerUUID) {
        return this.getPlayerOffersAmount(playerUUID) < PrivilegesProviderServer.getAsInt(playerUUID, EnumTradePrivilege.MAX_OFFERS_PER_PLAYER.id(), TradeConfig.MAX_OFFERS_PER_PLAYER.asInt());
    }

    public void createOffer(EntityPlayerMP playerMP, ItemStackWrapper stackWrapper, int amount, long price) {
        if (PrivilegesProviderServer.getAsBoolean(CommonReference.getPersistentUUID(playerMP), EnumTradePrivilege.MARKET_ACCESS.id(), true))
            this.offerCreationQueue.offer(new QueuedOfferCreation(CommonReference.getPersistentUUID(playerMP), stackWrapper, amount, price));
    }

    void processOfferCreationQueue() {
        while (!this.offerCreationQueue.isEmpty()) {
            final QueuedOfferCreation queued = this.offerCreationQueue.poll();
            if (queued != null) {
                final EntityPlayerMP playerMP = CommonReference.playerByUUID(queued.playerUUID);
                if (playerMP != null)
                    OxygenHelperServer.addRoutineTask(()->{
                        if (this.tryToCreateOffer(playerMP, queued.playerUUID, queued.stackWrapper, queued.amount, queued.price))
                            this.informPlayer(playerMP, EnumTradeStatusMessage.OFFER_CREATED);
                        else
                            this.informPlayer(playerMP, EnumTradeStatusMessage.OFFER_CREATION_FAILED);
                    });
            }
        }
    }

    private boolean tryToCreateOffer(EntityPlayerMP playerMP, UUID playerUUID, ItemStackWrapper stackWrapper, int amount, long price) {
        int maxAmount = PrivilegesProviderServer.getAsInt(playerUUID, EnumTradePrivilege.ITEMS_PER_OFFER_MAX_AMOUNT.id(), TradeConfig.ITEMS_PER_OFFER_MAX_AMOUNT.asInt());
        if (maxAmount < 0)
            maxAmount = stackWrapper.getItemStack().getMaxStackSize();
        long maxPrice = PrivilegesProviderServer.getAsLong(playerUUID, EnumTradePrivilege.PRICE_MAX_VALUE.id(), TradeConfig.PRICE_MAX_VALUE.asLong());
        if (this.validateItem(playerMP, stackWrapper) 
                && amount > 0 && amount <= maxAmount 
                && price > 0 && price <= maxPrice) {
            if (this.canCreateOffer(playerUUID)) {
                long offerCreationFee = MathUtils.percentValueOf(price, PrivilegesProviderServer.getAsInt(
                        playerUUID, 
                        EnumTradePrivilege.OFFER_CREATION_FEE_PERCENT.id(), 
                        TradeConfig.OFFER_CREATION_FEE_PERCENT.asInt()));
                boolean feeExist = offerCreationFee > 0;
                if (InventoryHelper.getEqualStackAmount(playerMP, stackWrapper) >= amount 
                        && (feeExist || CurrencyHelperServer.enoughCurrency(playerUUID, offerCreationFee, OxygenMain.COMMON_CURRENCY_INDEX))) {
                    CommonReference.delegateToServerThread(()->InventoryHelper.removeEqualStack(playerMP, stackWrapper, amount));
                    if (feeExist) {
                        CurrencyHelperServer.removeCurrency(playerUUID, offerCreationFee, OxygenMain.COMMON_CURRENCY_INDEX);
                        SoundEventHelperServer.playSoundClient(playerMP, OxygenSoundEffects.SELL.id);
                    }
                    OfferServer offer = new OfferServer(this.createOfferId(), playerUUID, stackWrapper, amount, price);
                    this.addOffer(offer);
                    OxygenMain.network().sendTo(new CPOfferAction(EnumOfferAction.CREATION, offer, CurrencyHelperServer.getCurrency(playerUUID, OxygenMain.COMMON_CURRENCY_INDEX)), playerMP);
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

    private void addOffer(OfferServer offer) {
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
        if (PrivilegesProviderServer.getAsBoolean(CommonReference.getPersistentUUID(playerMP), EnumTradePrivilege.MARKET_ACCESS.id(), true))
            this.offerActionsQueue.offer(new QueuedOfferActionServer(CommonReference.getPersistentUUID(playerMP), EnumOfferAction.PURCHASE, offerId));
    }

    public void cancelOffer(EntityPlayerMP playerMP, long offerId) {
        this.offerActionsQueue.offer(new QueuedOfferActionServer(CommonReference.getPersistentUUID(playerMP), EnumOfferAction.CANCEL, offerId));
    }

    void processOfferActionsQueue() {
        while (!this.offerActionsQueue.isEmpty()) {
            final QueuedOfferActionServer queued = this.offerActionsQueue.poll();
            if (queued != null) {
                final EntityPlayerMP playerMP = CommonReference.playerByUUID(queued.playerUUID);
                if (playerMP != null)
                    OxygenHelperServer.addRoutineTask(()->this.processOfferAction(playerMP, queued.action, queued.offerId));
            }
        }
    }

    private void processOfferAction(EntityPlayerMP playerMP, EnumOfferAction action, long offerId) {
        switch (action) {
        case PURCHASE:
            this.purchase(playerMP, offerId);
            break;
        case CANCEL:
            this.cancel(playerMP, offerId, false, true);
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
            OfferServer offer = this.manager.getOffersContainer().getOffer(offerId);
            if (offer.isOwner(playerUUID) && !TradeConfig.ENABLE_SELF_PURCHASE.asBoolean()) 
                return false;
            synchronized (offer) {//prevents simultaneously specific item purchase and cancellation 
                if (this.manager.getOffersContainer().isOfferExist(offerId)) {
                    //TODO 0.10 - Added mailbox capacity check, preventing from purchasing items if mailbox is full
                    if (MailManagerServer.instance().getMailboxesContainer().getPlayerMailbox(playerUUID).canAcceptMessages()) {
                        ItemStack itemStack = offer.getOfferedStack().getCachedItemStack();
                        if (InventoryHelper.haveEnoughSpace(playerMP, offer.getAmount(), itemStack.getMaxStackSize()) 
                                && CurrencyHelperServer.enoughCurrency(playerUUID, offer.getPrice(), OxygenMain.COMMON_CURRENCY_INDEX)) {
                            CurrencyHelperServer.removeCurrency(playerUUID, offer.getPrice(), OxygenMain.COMMON_CURRENCY_INDEX);
                            SoundEventHelperServer.playSoundClient(playerMP, OxygenSoundEffects.SELL.id);
                            this.removeOffer(offerId);
                            this.addSalesHsistoryRecord(offer, playerUUID);
                            this.sendItemToBuyer(playerUUID, offer);
                            this.sendSaleIncomeToSeller(offer);

                            OxygenMain.network().sendTo(new CPOfferAction(EnumOfferAction.PURCHASE, offer, CurrencyHelperServer.getCurrency(playerUUID, OxygenMain.COMMON_CURRENCY_INDEX)), playerMP);
                            return true;
                        }
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

    private void addSalesHsistoryRecord(OfferServer offer, UUID buyerUUID) {
        if (TradeConfig.ENABLE_SALES_HISTORY.asBoolean()) {
            this.manager.getSalesHistoryContainer().addEntry(SalesHistoryEntryServer.fromOffer(offer, buyerUUID));
            this.manager.getSalesHistoryContainer().setChanged(true);
        }
    }

    private void sendItemToBuyer(UUID buyerUUID, OfferServer offer) {
        MailHelperServer.sendSystemPackage(
                buyerUUID, 
                "mail.sender.sys", 
                "trade.purchased", 
                "trade.purchasedItemMessage",
                Parcel.create(offer.getOfferedStack(), offer.getAmount()),
                true);
    }

    private void sendSaleIncomeToSeller(OfferServer offer) {
        long saleFee = MathUtils.percentValueOf(offer.getPrice(), PrivilegesProviderServer.getAsInt(offer.getPlayerUUID(), EnumTradePrivilege.OFFER_SALE_FEE_PERCENT.id(), TradeConfig.OFFER_SALE_FEE_PERCENT.asInt()));
        MailHelperServer.sendSystemRemittance(
                offer.getPlayerUUID(), 
                "mail.sender.sys", 
                "trade.sold", 
                "trade.soldItemMessage",
                offer.getPrice() - saleFee,
                true);
    }

    public void cancel(EntityPlayerMP playerMP, long offerId, boolean operator, boolean returnItem) {
        if (this.tryToCancelOffer(playerMP, offerId, operator, returnItem))
            this.informPlayer(playerMP, EnumTradeStatusMessage.OFFER_CANCELED);
        else
            this.informPlayer(playerMP, EnumTradeStatusMessage.OFFER_CANCELLATION_FAILED);
    }

    private boolean tryToCancelOffer(EntityPlayerMP playerMP, long offerId, boolean operator, boolean returnItem) {
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        if (this.manager.getOffersContainer().isOfferExist(offerId)) {
            OfferServer offer = this.manager.getOffersContainer().getOffer(offerId);
            synchronized (offer) {//prevents simultaneously specific item purchase and cancellation 
                if (this.manager.getOffersContainer().isOfferExist(offerId)) {
                    if (offer.isOwner(playerUUID) || operator) {
                        this.removeOffer(offerId);
                        if (returnItem)
                            this.returnItemToSeller(offer);
                        if (!operator)
                            OxygenMain.network().sendTo(new CPOfferAction(EnumOfferAction.CANCEL, offer, CurrencyHelperServer.getCurrency(playerUUID, OxygenMain.COMMON_CURRENCY_INDEX)), playerMP);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void returnItemToSeller(OfferServer offer) {
        MailHelperServer.sendSystemPackage(
                offer.getPlayerUUID(), 
                "mail.sender.sys", 
                "trade.cancel", 
                "trade.cancelOfferMessage",
                Parcel.create(offer.getOfferedStack(), offer.getAmount()),
                true);
    }
}
