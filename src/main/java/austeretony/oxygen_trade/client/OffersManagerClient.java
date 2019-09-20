package austeretony.oxygen_trade.client;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.server.OxygenManagerServer;
import austeretony.oxygen_trade.common.main.EnumOfferAction;
import austeretony.oxygen_trade.common.network.server.SPCreateOffer;
import austeretony.oxygen_trade.common.network.server.SPPurchaseOrCancelOffer;

public class OffersManagerClient {

    private final TradeManagerClient manager;

    private final Queue<QueuedOfferActionClient> offerActionsQueue = new ConcurrentLinkedQueue<>();

    public OffersManagerClient(TradeManagerClient manager) {
        this.manager = manager;
        OxygenManagerServer.instance().getExecutionManager().getExecutors().getSchedulerExecutorService().scheduleAtFixedRate(
                ()->this.processOfferActionsQueue(), 1000L, 250L, TimeUnit.MILLISECONDS);
    }

    public int getPlayerOffersAmount() {
        int amount = 0;
        String username = OxygenHelperClient.getPlayerUsername();
        for (PlayerOfferClient offer : this.manager.getOffersContainer().getOffers())
            if (offer.getUsername().equals(username))
                amount++;
        return amount;
    }

    public List<PlayerOfferClient> getPlayerOffers() {
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

    public void performedOfferAction(EnumOfferAction action, PlayerOfferClient offer, long balance) {
        this.offerActionsQueue.offer(new QueuedOfferActionClient(action, offer, balance));
    }

    private void processOfferActionsQueue() {
        while (!this.offerActionsQueue.isEmpty()) {
            final QueuedOfferActionClient action = this.offerActionsQueue.poll();
            OxygenHelperClient.addRoutineTask(()->{
                switch (action.action) {
                case PURCHASE:
                case CANCEL:
                    this.manager.getOffersContainer().removeOffer(action.offer.getId());
                    break;
                case CREATION:
                    this.manager.getOffersContainer().addOffer(action.offer);
                    break;
                }
                this.manager.getOffersContainer().setChanged(true);
                this.manager.getTradeMenuManager().performedOfferAction(action.action, action.offer, action.balance);
            });
        }
    }
}
