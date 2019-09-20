package austeretony.oxygen_trade.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.UUID;

import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.persistent.PersistentEntry;
import austeretony.oxygen_core.common.sync.SynchronizedData;
import austeretony.oxygen_core.common.util.ByteBufUtils;
import austeretony.oxygen_core.common.util.StreamUtils;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import io.netty.buffer.ByteBuf;

public class SalesHistoryEntryServer implements PersistentEntry, SynchronizedData {

    private long entryId, price;

    private UUID sellerUUID, buyerUUID;

    private ItemStackWrapper offeredStack;

    private int amount;

    private String sellerUsername, buyerUsername;

    public SalesHistoryEntryServer() {}

    private SalesHistoryEntryServer(long entryId, UUID ownerUUID, UUID buyerUUID, ItemStackWrapper offeredStack, int amount, long price) {
        this.entryId = entryId;
        this.sellerUUID = ownerUUID;
        this.buyerUUID = buyerUUID;
        this.offeredStack = offeredStack;
        this.amount = amount;
        this.price = price;
    }

    public static SalesHistoryEntryServer fromOffer(PlayerOfferServer offer, UUID buyerUUID) {
        long id = System.currentTimeMillis();
        while (TradeManagerServer.instance().getSalesHistoryContainer().isEntryExist(id))
            id++;
        return new SalesHistoryEntryServer(id, offer.getPlayerUUID(), buyerUUID, offer.getOfferedStack(), offer.getAmount(), offer.getPrice());
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

    public ItemStackWrapper getOfferedStack() {
        return this.offeredStack;
    }

    public int getAmount() {
        return this.amount;
    }

    public long getPrice() {
        return this.price;
    }

    public void updateSellerUsername() {
        this.sellerUsername = OxygenHelperServer.getPlayerSharedData(this.sellerUUID).getUsername();
    }

    public void updateBuyerUsername() {
        this.buyerUsername = OxygenHelperServer.getPlayerSharedData(this.buyerUUID).getUsername();
    }

    @Override
    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write(this.entryId, bos);
        StreamUtils.write(this.sellerUUID, bos);
        StreamUtils.write(this.buyerUUID, bos);
        this.offeredStack.write(bos);
        StreamUtils.write((short) this.amount, bos);
        StreamUtils.write(this.price, bos);
    }

    @Override
    public void read(BufferedInputStream bis) throws IOException {
        this.entryId = StreamUtils.readLong(bis);
        this.sellerUUID = StreamUtils.readUUID(bis);
        this.buyerUUID = StreamUtils.readUUID(bis);
        this.offeredStack = ItemStackWrapper.read(bis);
        this.amount = StreamUtils.readShort(bis);
        this.price = StreamUtils.readLong(bis);
    }

    @Override
    public void write(ByteBuf buffer) {
        buffer.writeLong(this.entryId);
        ByteBufUtils.writeString(this.sellerUsername, buffer);
        ByteBufUtils.writeString(this.buyerUsername, buffer);
        this.offeredStack.write(buffer);
        buffer.writeShort(this.amount);
        buffer.writeLong(this.price);
    }

    @Override
    public void read(ByteBuf buffer) {}
}
