package austeretony.oxygen_trade.client;

import austeretony.oxygen_trade.common.main.EnumOfferAction;

public class QueuedOfferActionClient {

    final EnumOfferAction action;

    final OfferClient offer;

    final long balance;

    protected QueuedOfferActionClient(EnumOfferAction action, OfferClient offer, long balance) {
        this.action = action;
        this.offer = offer;
        this.balance = balance;
    }
}
