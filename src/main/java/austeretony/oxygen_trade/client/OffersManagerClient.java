package austeretony.oxygen_trade.client;

import java.util.List;
import java.util.stream.Collectors;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.api.PrivilegesProviderClient;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_trade.common.config.TradeConfig;
import austeretony.oxygen_trade.common.main.EnumOfferAction;
import austeretony.oxygen_trade.common.main.EnumTradePrivilege;
import austeretony.oxygen_trade.common.network.server.SPCreateOffer;
import austeretony.oxygen_trade.common.network.server.SPPurchaseOrCancelOffer;

public class OffersManagerClient {

    private final TradeManagerClient manager;

    public OffersManagerClient(TradeManagerClient manager) {
        this.manager = manager;
    }

    public int getPlayerOffersAmount() {
        int amount = 0;
        String username = OxygenHelperClient.getPlayerUsername();
        for (OfferClient offer : this.manager.getOffersContainer().getOffers())
            if (offer.getUsername().equals(username))
                amount++;
        return amount;
    }

    public List<OfferClient> getPlayerOffers() {
        String username = OxygenHelperClient.getPlayerUsername();
        return this.manager.getOffersContainer().getOffers()
                .stream()
                .filter((offer)->offer.getUsername().equals(username))
                .collect(Collectors.toList());
    }

    public void purchaseItemSynced(long offerId) {
        OxygenMain.network().sendToServer(new SPPurchaseOrCancelOffer(EnumOfferAction.PURCHASE, offerId));
    }

    public void createOfferSynced(ItemStackWrapper stackWrapper, int amount, long price) {
        OxygenMain.network().sendToServer(new SPCreateOffer(stackWrapper, amount, price));
    }

    public void cancelOfferSynced(long offerId) {
        OxygenMain.network().sendToServer(new SPPurchaseOrCancelOffer(EnumOfferAction.CANCEL, offerId));
    }

    public void performedOfferAction(EnumOfferAction action, OfferClient offer, long balance) {
        switch (action) {
        case PURCHASE:
            if (!PrivilegesProviderClient.getAsBoolean(EnumTradePrivilege.SALES_HISTORY_ACCESS.id(), TradeConfig.ENABLE_SALES_HISTORY_SYNC.asBoolean()))
                this.manager.getSalesHistoryContainer().addEntry(new SalesHistoryEntryClient(
                        offer.getUsername(),
                        ClientReference.getClientPlayer().getName(),
                        offer.getOfferedStack(),
                        offer.getAmount(),
                        offer.getPrice()));
        case CANCEL:
            this.manager.getOffersContainer().removeOffer(offer.getId());
            break;
        case CREATION:
            this.manager.getOffersContainer().addOffer(offer);
            break;
        }
        this.manager.getOffersContainer().setChanged(true);
        this.manager.getTradeMenuManager().performedOfferAction(action, offer, balance);
    }
}
