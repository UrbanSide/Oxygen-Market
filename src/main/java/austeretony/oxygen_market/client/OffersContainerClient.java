package austeretony.oxygen_market.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.persistent.AbstractPersistentData;
import austeretony.oxygen_core.common.util.StreamUtils;

public class OffersContainerClient extends AbstractPersistentData {

    private final Map<Long, OfferClient> offers = new ConcurrentHashMap<>();

    public int getOffersAmount() {
        return this.offers.size();
    }

    public Set<Long> getOfferIds() {
        return this.offers.keySet();
    }

    public Collection<OfferClient> getOffers() {
        return this.offers.values();
    }

    @Nullable
    public OfferClient getOffer(long offerId) {
        return this.offers.get(offerId);
    }

    public void addOffer(OfferClient offer) {
        this.offers.put(offer.getId(), offer);
    }

    public void removeOffer(long offerId) {
        this.offers.remove(offerId);
    }

    @Override
    public String getDisplayName() {
        return "market:offers_data_client";
    }

    @Override
    public String getPath() {
        return OxygenHelperClient.getDataFolder() + "/client/world/market/offers_client.dat";
    }

    @Override
    public void read(BufferedInputStream bis) throws IOException {
        int amount = StreamUtils.readInt(bis);
        OfferClient offer;
        for (int i = 0; i < amount; i++) {
            offer = new OfferClient();
            offer.read(bis);
            this.addOffer(offer);
        }
    }

    @Override
    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write(this.offers.size(), bos);
        for (OfferClient offer : this.offers.values())
            offer.write(bos);
    }

    @Override
    public void reset() {
        this.offers.clear();
    }
}
