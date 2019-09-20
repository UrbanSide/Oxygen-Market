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
import net.minecraft.item.ItemStack;

public class SalesHistoryEntryClient implements PersistentEntry, SynchronizedData {

    private long entryId, price;

    private String sellerUsername, buyerUsername;

    private ItemStackWrapper offeredStack;

    private int amount;

    private ItemStack itemStack;

    public SalesHistoryEntryClient() {}

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

    public ItemStackWrapper getOfferedStack() {
        return this.offeredStack;
    }

    public int getAmount() {
        return this.amount;
    }

    public long getPrice() {
        return this.price;
    }

    public ItemStack getItemStack() {
        if (this.itemStack == null)
            this.itemStack = this.offeredStack.getItemStack();
        return this.itemStack;
    }

    @Override
    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write(this.entryId, bos);
        StreamUtils.write(this.sellerUsername, bos);
        StreamUtils.write(this.buyerUsername, bos);
        this.offeredStack.write(bos);
        StreamUtils.write((short) this.amount, bos);
        StreamUtils.write(this.price, bos);
    }

    @Override
    public void read(BufferedInputStream bis) throws IOException {
        this.entryId = StreamUtils.readLong(bis);
        this.sellerUsername = StreamUtils.readString(bis);
        this.buyerUsername = StreamUtils.readString(bis);
        this.offeredStack = ItemStackWrapper.read(bis);
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
        this.offeredStack = ItemStackWrapper.read(buffer);
        this.amount = buffer.readShort();
        this.price = buffer.readLong();
    }
}
