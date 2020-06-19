package austeretony.oxygen_market.server;

import java.util.Iterator;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.Nullable;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.main.EnumOxygenStatusMessage;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.sound.OxygenSoundEffects;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_core.server.OxygenManagerServer;
import austeretony.oxygen_core.server.api.CurrencyHelperServer;
import austeretony.oxygen_core.server.api.InventoryProviderServer;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.PrivilegesProviderServer;
import austeretony.oxygen_core.server.api.SoundEventHelperServer;
import austeretony.oxygen_core.server.api.TimeHelperServer;
import austeretony.oxygen_mail.common.mail.Attachments;
import austeretony.oxygen_mail.common.mail.EnumMail;
import austeretony.oxygen_mail.server.api.MailHelperServer;
import austeretony.oxygen_market.common.config.MarketConfig;
import austeretony.oxygen_market.common.main.EnumMarketPrivilege;
import austeretony.oxygen_market.common.main.EnumMarketStatusMessage;
import austeretony.oxygen_market.common.main.EnumOfferAction;
import austeretony.oxygen_market.common.network.client.CPOfferAction;
import austeretony.oxygen_market.server.market.OfferServer;
import austeretony.oxygen_market.server.market.SalesHistoryEntryServer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

public class OffersManagerServer {

    private final MarketManagerServer manager;

    private final Queue<QueuedOfferCreation> offerCreationQueue = new ConcurrentLinkedQueue<>();

    private final Queue<QueuedOfferAction> offerActionsQueue = new ConcurrentLinkedQueue<>();

    public OffersManagerServer(MarketManagerServer manager) {
        this.manager = manager;
    }

    void process() {
        this.processOfferCreationQueue();
        this.processOfferActionsQueue();
    }

    public void processExpiredOffers() {
        final Runnable task = ()->{
            if (this.manager.getOffersContainer().getOffersAmount() > 0) {
                Iterator<OfferServer> iterator = this.manager.getOffersContainer().getOffers().iterator();
                OfferServer offer;
                long 
                currTimeMillis = TimeHelperServer.getCurrentMillis(),
                expireTimeMillis = MarketConfig.OFFER_EXPIRE_TIME_HOURS.asInt() * 3_600_000L;
                int removed = 0;
                while (iterator.hasNext()) {
                    offer = iterator.next();
                    if (offer != null) {
                        if (currTimeMillis - offer.getId() > expireTimeMillis) {
                            iterator.remove();
                            this.returnExpiredItemToSeller(offer);
                            removed++;
                        }
                    }
                }
                if (removed > 0)
                    this.manager.getOffersContainer().setChanged(true);
                OxygenMain.LOGGER.info("[Market] Removed {} expired offers.", removed);
            }
        };
        OxygenHelperServer.addRoutineTask(task);
    }

    private void returnExpiredItemToSeller(OfferServer offer) {
        MailHelperServer.sendSystemMail(
                offer.getPlayerUUID(), 
                "market.sender.market", 
                EnumMail.PARCEL,
                "market.expired", 
                Attachments.parcel(offer.getStackWrapper(), offer.getAmount()),
                true,
                "market.expiredOfferMessage",
                String.valueOf(offer.getAmount()),
                offer.getStackWrapper().getItemStack().getDisplayName());
    }

    public int getPlayerOffersAmount(UUID playerUUID) {
        int amount = 0;
        for (OfferServer offer : this.manager.getOffersContainer().getOffers())
            if (offer.getPlayerUUID().equals(playerUUID))
                amount++;
        return amount;
    }

    public boolean canCreateOffer(UUID playerUUID) {
        return this.getPlayerOffersAmount(playerUUID) < PrivilegesProviderServer.getAsInt(playerUUID, EnumMarketPrivilege.MAX_OFFERS_PER_PLAYER.id(), MarketConfig.MAX_OFFERS_PER_PLAYER.asInt());
    }

    public void createOffer(EntityPlayerMP playerMP, ItemStackWrapper stackWrapper, int amount, long price) {
        if (PrivilegesProviderServer.getAsBoolean(CommonReference.getPersistentUUID(playerMP), EnumMarketPrivilege.MARKET_ACCESS.id(), true))
            this.offerCreationQueue.offer(new QueuedOfferCreation(playerMP, stackWrapper, amount, price));
    }

    private void processOfferCreationQueue() {
        final Runnable task = ()->{
            while (!this.offerCreationQueue.isEmpty()) {
                final QueuedOfferCreation queued = this.offerCreationQueue.poll();
                if (queued != null)
                    this.processOfferCreation(queued);
            }
        };
        OxygenHelperServer.addRoutineTask(task);
    }

    private void processOfferCreation(QueuedOfferCreation queued) {
        if (this.tryToCreateOffer(queued.playerMP, queued.stackWrapper, queued.amount, queued.price))
            this.manager.sendStatusMessage(queued.playerMP, EnumMarketStatusMessage.OFFER_CREATED);
        else
            this.manager.sendStatusMessage(queued.playerMP, EnumMarketStatusMessage.OFFER_CREATION_FAILED);
    }

    private boolean tryToCreateOffer(EntityPlayerMP playerMP, ItemStackWrapper stackWrapper, int amount, long price) {
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        int maxAmount = PrivilegesProviderServer.getAsInt(playerUUID, EnumMarketPrivilege.ITEMS_PER_OFFER_MAX_AMOUNT.id(), MarketConfig.ITEMS_PER_OFFER_MAX_AMOUNT.asInt());
        if (maxAmount < 0)
            maxAmount = stackWrapper.getItemStack().getMaxStackSize();
        long maxPrice = PrivilegesProviderServer.getAsLong(playerUUID, EnumMarketPrivilege.PRICE_MAX_VALUE.id(), MarketConfig.PRICE_MAX_VALUE.asLong());
        if (this.validateItem(playerMP, stackWrapper) 
                && amount > 0 && amount <= maxAmount 
                && price > 0 && price <= maxPrice) {
            if (this.canCreateOffer(playerUUID)) {
                long offerCreationFee = MathUtils.percentValueOf(price, PrivilegesProviderServer.getAsInt(
                        playerUUID, 
                        EnumMarketPrivilege.OFFER_CREATION_FEE_PERCENT.id(), 
                        MarketConfig.OFFER_CREATION_FEE_PERCENT.asInt()));
                boolean feeExist = offerCreationFee > 0;
                if (InventoryProviderServer.getPlayerInventory().getEqualItemAmount(playerMP, stackWrapper) >= amount 
                        && (!feeExist || CurrencyHelperServer.enoughCurrency(playerUUID, offerCreationFee, OxygenMain.COMMON_CURRENCY_INDEX))) {
                    InventoryProviderServer.getPlayerInventory().removeItem(playerMP, stackWrapper, amount);

                    SoundEventHelperServer.playSoundClient(playerMP, OxygenSoundEffects.INVENTORY_OPERATION.getId());

                    if (feeExist) {
                        CurrencyHelperServer.removeCurrency(playerUUID, offerCreationFee, OxygenMain.COMMON_CURRENCY_INDEX);
                        SoundEventHelperServer.playSoundClient(playerMP, OxygenSoundEffects.RINGING_COINS.getId());
                    }
                    OfferServer offer = new OfferServer(this.manager.getOffersContainer().createId(), playerUUID, stackWrapper, amount, price);
                    this.addOffer(offer);
                    OxygenMain.network().sendTo(new CPOfferAction(EnumOfferAction.CREATION, offer, CurrencyHelperServer.getCurrency(playerUUID, OxygenMain.COMMON_CURRENCY_INDEX)), playerMP);

                    if (MarketConfig.ADVANCED_LOGGING.asBoolean())
                        OxygenMain.LOGGER.info("[Market] Offer created: {}.", 
                                offer);

                    return true;
                }
            }
        }
        return false;
    }

    private boolean validateItem(EntityPlayerMP playerMP, ItemStackWrapper stackWrapper) {
        if (stackWrapper.getItemId() == Item.getIdFromItem(Items.AIR)) {
            this.manager.sendStatusMessage(playerMP, EnumMarketStatusMessage.ITEM_DAMAGED);
            return false;
        }
        if (this.manager.getItemsBlackList().isBlackListed(Item.getItemById(stackWrapper.getItemId()))) {
            this.manager.sendStatusMessage(playerMP, EnumMarketStatusMessage.ITEM_BLACKLISTED);
            return false;
        }
        return true;
    }

    private void addOffer(OfferServer offer) {
        this.manager.getOffersContainer().addOffer(offer);
        this.manager.getOffersContainer().setChanged(true);
    }

    public void purchaseItem(EntityPlayerMP playerMP, long offerId) {
        if (PrivilegesProviderServer.getAsBoolean(CommonReference.getPersistentUUID(playerMP), EnumMarketPrivilege.MARKET_ACCESS.id(), true))
            this.offerActionsQueue.offer(new QueuedOfferAction(playerMP, EnumOfferAction.PURCHASE, offerId));
    }

    public void cancelOffer(EntityPlayerMP playerMP, long offerId) {
        this.offerActionsQueue.offer(new QueuedOfferAction(playerMP, EnumOfferAction.CANCEL, offerId));
    }

    private void processOfferActionsQueue() {
        final Runnable task = ()->{
            while (!this.offerActionsQueue.isEmpty()) {
                final QueuedOfferAction queued = this.offerActionsQueue.poll();
                if (queued != null)
                    this.processOfferAction(queued.playerMP, queued.action, queued.offerId);
            }
        };
        OxygenHelperServer.addRoutineTask(task);
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
            this.manager.sendStatusMessage(playerMP, EnumMarketStatusMessage.ITEM_PURCHASED);
        else
            this.manager.sendStatusMessage(playerMP, EnumMarketStatusMessage.ITEM_PURCHASE_FAILED);
    }

    private boolean tryToPurchaseItem(EntityPlayerMP playerMP, long offerId) {
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        OfferServer offer = this.manager.getOffersContainer().getOffer(offerId);
        if (offer != null) {
            if (offer.isOwner(playerUUID) && !MarketConfig.ENABLE_SELF_PURCHASE.asBoolean()) 
                return false;
            synchronized (offer) {//prevents simultaneously specific item purchase and cancellation 
                offer = this.manager.getOffersContainer().getOffer(offerId);
                if (offer != null) {
                    //TODO 0.10 - Added mailbox capacity check, preventing from purchasing items if mailbox is full
                    if (MailHelperServer.canPlayerAcceptMessages(playerUUID)) {
                        if (CurrencyHelperServer.enoughCurrency(playerUUID, offer.getPrice(), OxygenMain.COMMON_CURRENCY_INDEX)) {
                            CurrencyHelperServer.removeCurrency(playerUUID, offer.getPrice(), OxygenMain.COMMON_CURRENCY_INDEX);
                            SoundEventHelperServer.playSoundClient(playerMP, OxygenSoundEffects.RINGING_COINS.getId());

                            this.removeOffer(offerId);
                            this.addSalesHsistoryRecord(offer, playerUUID);
                            this.sendItemToBuyer(playerUUID, offer);
                            this.sendSaleIncomeToSeller(offer, playerMP);

                            OxygenMain.network().sendTo(new CPOfferAction(EnumOfferAction.PURCHASE, offer, CurrencyHelperServer.getCurrency(playerUUID, OxygenMain.COMMON_CURRENCY_INDEX)), playerMP);

                            if (MarketConfig.ADVANCED_LOGGING.asBoolean())
                                OxygenMain.LOGGER.info("[Market] Player {}/{} purchased item: {}.", 
                                        CommonReference.getName(playerMP),
                                        playerUUID,
                                        offer);

                            return true;
                        }   
                    } else
                        OxygenManagerServer.instance().sendStatusMessage(playerMP, EnumOxygenStatusMessage.MAILBOX_FULL);
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
        if (MarketConfig.ENABLE_SALES_HISTORY.asBoolean()) {
            this.manager.getSalesHistoryContainer().addEntry(new SalesHistoryEntryServer(
                    this.manager.getSalesHistoryContainer().createId(), 
                    offer.getPlayerUUID(), 
                    buyerUUID, 
                    offer.getStackWrapper(), 
                    offer.getAmount(), 
                    offer.getPrice()));
            this.manager.getSalesHistoryContainer().setChanged(true);
        }
    }

    private void sendItemToBuyer(UUID buyerUUID, OfferServer offer) {
        MailHelperServer.sendSystemMail(
                buyerUUID, 
                "market.sender.market", 
                EnumMail.PARCEL,
                "market.purchased", 
                Attachments.parcel(offer.getStackWrapper(), offer.getAmount()),
                true,
                "market.purchasedItemMessage",
                String.valueOf(offer.getAmount()),
                offer.getStackWrapper().getCachedItemStack().getDisplayName(),
                offer.getOwnerUsername());
    }

    private void sendSaleIncomeToSeller(OfferServer offer, EntityPlayerMP buyerMP) {
        long saleFee = MathUtils.percentValueOf(offer.getPrice(), PrivilegesProviderServer.getAsInt(offer.getPlayerUUID(), EnumMarketPrivilege.OFFER_SALE_FEE_PERCENT.id(), MarketConfig.OFFER_SALE_FEE_PERCENT.asInt()));
        MailHelperServer.sendSystemMail(
                offer.getPlayerUUID(), 
                "market.sender.market", 
                EnumMail.REMITTANCE,
                "market.sold", 
                Attachments.remittance(OxygenMain.COMMON_CURRENCY_INDEX, offer.getPrice() - saleFee),
                true,
                "market.soldItemMessage",
                String.valueOf(offer.getAmount()),
                offer.getStackWrapper().getCachedItemStack().getDisplayName(),
                CommonReference.getName(buyerMP));
    }

    public void cancel(EntityPlayerMP playerMP, long offerId) {
        if (this.tryToCancelOffer(playerMP, offerId))
            this.manager.sendStatusMessage(playerMP, EnumMarketStatusMessage.OFFER_CANCELED);
        else
            this.manager.sendStatusMessage(playerMP, EnumMarketStatusMessage.OFFER_CANCELLATION_FAILED);
    }

    private boolean tryToCancelOffer(EntityPlayerMP playerMP, long offerId) {
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        OfferServer offer = this.manager.getOffersContainer().getOffer(offerId);
        if (offer != null) {
            synchronized (offer) {//prevents simultaneously specific item purchase and cancellation 
                offer = this.manager.getOffersContainer().getOffer(offerId);
                if (offer != null) {
                    if (offer.isOwner(playerUUID)) {
                        this.removeOffer(offerId);
                        this.returnItemToSeller(offer);

                        OxygenMain.network().sendTo(new CPOfferAction(EnumOfferAction.CANCEL, offer, CurrencyHelperServer.getCurrency(playerUUID, OxygenMain.COMMON_CURRENCY_INDEX)), playerMP);

                        if (MarketConfig.ADVANCED_LOGGING.asBoolean())
                            OxygenMain.LOGGER.info("[Market] Player {}/{} canceled offer: {}.",
                                    CommonReference.getName(playerMP),
                                    playerUUID,
                                    offer);

                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Nullable
    public OfferServer cancelOfferOp(long offerId, boolean returnItem) {
        OfferServer offer = this.manager.getOffersContainer().getOffer(offerId);
        if (offer != null) {
            synchronized (offer) {//prevents simultaneously specific item purchase and cancellation 
                offer = this.manager.getOffersContainer().getOffer(offerId);
                if (offer != null) {
                    this.removeOffer(offerId);
                    if (returnItem)
                        this.returnItemToSeller(offer);
                    return offer;
                }
            }
        }
        return null;
    }

    private void returnItemToSeller(OfferServer offer) {
        MailHelperServer.sendSystemMail(
                offer.getPlayerUUID(), 
                "market.sender.market", 
                EnumMail.PARCEL,
                "market.cancel", 
                Attachments.parcel(offer.getStackWrapper(), offer.getAmount()),
                true,
                "market.cancelOfferMessage",
                String.valueOf(offer.getAmount()),
                offer.getStackWrapper().getItemStack().getDisplayName());
    }
}
