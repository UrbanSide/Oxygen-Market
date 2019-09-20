package austeretony.oxygen_trade.client;

import austeretony.oxygen_trade.common.main.EnumOfferAction;

public class QueuedOfferActionClient {

    final EnumOfferAction action;

    final PlayerOfferClient offer;

    final long balance;

    protected QueuedOfferActionClient(EnumOfferAction action, PlayerOfferClient offer, long balance) {
        this.action = action;
        this.offer = offer;
        this.balance = balance;
    }
}
