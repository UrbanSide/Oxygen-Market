package austeretony.oxygen_market.client.market;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.persistent.PersistentEntry;
import austeretony.oxygen_core.common.sync.SynchronousEntry;
import austeretony.oxygen_core.common.util.ByteBufUtils;
import austeretony.oxygen_core.common.util.StreamUtils;
import io.netty.buffer.ByteBuf;

public class SalesHistoryEntryClient implements PersistentEntry, SynchronousEntry {

    private long entryId, price;

    private String sellerUsername, buyerUsername;

    private ItemStackWrapper stackWrapper;

    private int amount;

    public SalesHistoryEntryClient() {}

    public SalesHistoryEntryClient(String sellerUsername, String buyerUsername, ItemStackWrapper offeredStack, int amount, long price) {
        this.entryId = System.currentTimeMillis();
        this.sellerUsername = sellerUsername;
        this.buyerUsername = buyerUsername;
        this.stackWrapper = offeredStack;
        this.amount = amount;
        this.price = price;
    }

    @Override
    public long getId() {
        return this.entryId;
    }

    public String getSellerUsername() {
        return this.sellerUsername;
    }

    public String getBuyerUsername() {
        return this.buyerUsername;
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

    @Override
    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write(this.entryId, bos);
        StreamUtils.write(this.sellerUsername, bos);
        StreamUtils.write(this.buyerUsername, bos);
        this.stackWrapper.write(bos);
        StreamUtils.write((short) this.amount, bos);
        StreamUtils.write(this.price, bos);
    }

    @Override
    public void read(BufferedInputStream bis) throws IOException {
        this.entryId = StreamUtils.readLong(bis);
        this.sellerUsername = StreamUtils.readString(bis);
        this.buyerUsername = StreamUtils.readString(bis);
        this.stackWrapper = ItemStackWrapper.read(bis);
        this.amount = StreamUtils.readShort(bis);
        this.price = StreamUtils.readLong(bis);
    }

    @Override
    public void write(ByteBuf buffer) {}

    @Override
    public void read(ByteBuf buffer) {
        this.entryId = buffer.readLong();
        this.sellerUsername = ByteBufUtils.readString(buffer);
        this.buyerUsername = ByteBufUtils.readString(buffer);
        this.stackWrapper = ItemStackWrapper.read(buffer);
        this.amount = buffer.readShort();
        this.price = buffer.readLong();
    }
}
