package austeretony.oxygen_market.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.persistent.AbstractPersistentData;
import austeretony.oxygen_core.common.util.StreamUtils;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_core.server.api.TimeHelperServer;
import austeretony.oxygen_market.server.market.OfferServer;

public class OffersContainerServer extends AbstractPersistentData {

    private final Map<Long, OfferServer> offers = new ConcurrentHashMap<>();

    public int getOffersAmount() {
        return this.offers.size();
    }

    public Set<Long> getOfferIds() {
        return this.offers.keySet();
    }

    public Collection<OfferServer> getOffers() {
        return this.offers.values();
    }

    @Nullable
    public OfferServer getOffer(long offerId) {
        return this.offers.get(offerId);
    }

    public void addOffer(OfferServer offer) {
        offer.setOwnerUsername(OxygenHelperServer.getPlayerSharedData(offer.getPlayerUUID()).getUsername());
        this.offers.put(offer.getId(), offer);
    }

    public void removeOffer(long offerId) {
        this.offers.remove(offerId);
    }

    public long createId() {
        long id = TimeHelperServer.getCurrentMillis();
        while (this.offers.containsKey(id))
            id++;
        return id;
    }

    @Override
    public String getDisplayName() {
        return "market:offers_data_server";
    }

    @Override
    public String getPath() {
        return OxygenHelperServer.getDataFolder() + "/server/world/market/offers_server.dat";
    }

    @Override
    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write(this.offers.size(), bos);
        for (OfferServer offer : this.offers.values())
            offer.write(bos);
    }

    @Override
    public void read(BufferedInputStream bis) throws IOException {
        int amount = StreamUtils.readInt(bis);
        OfferServer offer;
        for (int i = 0; i < amount; i++) {
            offer = new OfferServer();
            offer.read(bis);
            this.addOffer(offer);
        }

        OxygenMain.LOGGER.info("[Market] Loaded {} market offers.", this.getOffersAmount());
        MarketManagerServer.instance().getOffersManager().processExpiredOffers();
    }

    @Override
    public void reset() {
        this.offers.clear();
    }
}
