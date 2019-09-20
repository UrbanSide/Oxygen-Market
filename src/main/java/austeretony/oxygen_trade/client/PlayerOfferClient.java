package austeretony.oxygen_trade.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.persistent.PersistentEntry;
import austeretony.oxygen_core.common.sync.SynchronizedData;
import austeretony.oxygen_core.common.util.ByteBufUtils;
import austeretony.oxygen_core.common.util.StreamUtils;
import io.netty.buffer.ByteBuf;

public class PlayerOfferClient implements PersistentEntry, SynchronizedData {

    private long offerId, price;

    private String username;

    private ItemStackWrapper offeredStack;

    private int amount;

    public PlayerOfferClient() {}

    @Override
    public long getId() {
        return this.offerId;
    }

    public String getUsername() {
        return this.username;
    }

    public ItemStackWrapper getOfferedStack() {
        return this.offeredStack;
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
        this.offeredStack.write(bos);
        StreamUtils.write((short) this.amount, bos);
        StreamUtils.write(this.price, bos);
    }

    @Override
    public void read(BufferedInputStream bis) throws IOException {
        this.offerId = StreamUtils.readLong(bis);
        this.username = StreamUtils.readString(bis);
        this.offeredStack = ItemStackWrapper.read(bis);
        this.amount = StreamUtils.readShort(bis);
        this.price = StreamUtils.readLong(bis);
    }

    @Override
    public void write(ByteBuf buffer) {}

    @Override
    public void read(ByteBuf buffer) {
        this.offerId = buffer.readLong();
        this.username = ByteBufUtils.readString(buffer);
        this.offeredStack = ItemStackWrapper.read(buffer);
        this.amount = buffer.readShort();
        this.price = buffer.readLong();
    }
}
