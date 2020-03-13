package austeretony.oxygen_market.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.persistent.PersistentEntry;
import austeretony.oxygen_core.common.sync.SynchronousEntry;
import austeretony.oxygen_core.common.util.ByteBufUtils;
import austeretony.oxygen_core.common.util.StreamUtils;
import io.netty.buffer.ByteBuf;

public class OfferClient implements PersistentEntry, SynchronousEntry {

    private long offerId, price;

    private String username;

    private ItemStackWrapper stackWrapper;

    private int amount;

    public OfferClient() {}

    @Override
    public long getId() {
        return this.offerId;
    }

    public String getUsername() {
        return this.username;
    }

    public ItemStackWrapper getStackWrapper() {
        return this.stackWrapper;
    }

    public int getAmount() {
        return this.amount;
    }

    public long getPrice() {
        return this.price;
    }

    public boolean isOwner(String username) {
        return this.username.equals(username);
    }

    @Override
    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write(this.offerId, bos);
        StreamUtils.write(this.username, bos);
        this.stackWrapper.write(bos);
        StreamUtils.write((short) this.amount, bos);
        StreamUtils.write(this.price, bos);
    }

    @Override
    public void read(BufferedInputStream bis) throws IOException {
        this.offerId = StreamUtils.readLong(bis);
        this.username = StreamUtils.readString(bis);
        this.stackWrapper = ItemStackWrapper.read(bis);
        this.amount = StreamUtils.readShort(bis);
        this.price = StreamUtils.readLong(bis);
    }

    @Override
    public void write(ByteBuf buffer) {}

    @Override
    public void read(ByteBuf buffer) {
        this.offerId = buffer.readLong();
        this.username = ByteBufUtils.readString(buffer);
        this.stackWrapper = ItemStackWrapper.read(buffer);
        this.amount = buffer.readShort();
        this.price = buffer.readLong();
    }
}
