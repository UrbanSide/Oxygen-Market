package austeretony.oxygen_market.server.market;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.UUID;

import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.persistent.PersistentEntry;
import austeretony.oxygen_core.common.sync.SynchronousEntry;
import austeretony.oxygen_core.common.util.ByteBufUtils;
import austeretony.oxygen_core.common.util.StreamUtils;
import io.netty.buffer.ByteBuf;

public class SalesHistoryEntryServer implements PersistentEntry, SynchronousEntry {

    private long entryId, price;

    private UUID sellerUUID, buyerUUID;

    private ItemStackWrapper stackWrapper;

    private int amount;

    private String sellerUsername, buyerUsername;

    public SalesHistoryEntryServer() {}

    public SalesHistoryEntryServer(long entryId, UUID ownerUUID, UUID buyerUUID, ItemStackWrapper offeredStack, int amount, long price) {
        this.entryId = entryId;
        this.sellerUUID = ownerUUID;
        this.buyerUUID = buyerUUID;
        this.stackWrapper = offeredStack;
        this.amount = amount;
        this.price = price;
    }

    @Override
    public long getId() {
        return this.entryId;
    }

    public UUID getSellerUUID() {
        return this.sellerUUID;
    }

    public UUID getBuyerUUID() {
        return this.buyerUUID;
    }

    public ItemStackWrapper getStackWrapperStack() {
        return this.stackWrapper;
    }

    public int getAmount() {
        return this.amount;
    }

    public long getPrice() {
        return this.price;
    }

    public void setSellerUsername(String username) {
        this.sellerUsername = username;
    }

    public void setBuyerUsername(String username) {
        this.buyerUsername = username;
    }

    @Override
    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write(this.entryId, bos);
        StreamUtils.write(this.sellerUUID, bos);
        StreamUtils.write(this.buyerUUID, bos);
        this.stackWrapper.write(bos);
        StreamUtils.write((short) this.amount, bos);
        StreamUtils.write(this.price, bos);
    }

    @Override
    public void read(BufferedInputStream bis) throws IOException {
        this.entryId = StreamUtils.readLong(bis);
        this.sellerUUID = StreamUtils.readUUID(bis);
        this.buyerUUID = StreamUtils.readUUID(bis);
        this.stackWrapper = ItemStackWrapper.read(bis);
        this.amount = StreamUtils.readShort(bis);
        this.price = StreamUtils.readLong(bis);
    }

    @Override
    public void write(ByteBuf buffer) {
        buffer.writeLong(this.entryId);
        ByteBufUtils.writeString(this.sellerUsername, buffer);
        ByteBufUtils.writeString(this.buyerUsername, buffer);
        this.stackWrapper.write(buffer);
        buffer.writeShort(this.amount);
        buffer.writeLong(this.price);
    }

    @Override
    public void read(ByteBuf buffer) {}
}
