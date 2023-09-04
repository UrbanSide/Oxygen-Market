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

public class OfferServer implements PersistentEntry, SynchronousEntry {

    private long offerId, price;

    private UUID playerUUID;

    private ItemStackWrapper stackWrapper;

    private int amount;

    private String username;

    public OfferServer() {}

    public OfferServer(long offerId, UUID playerUUID, ItemStackWrapper offeredStack, int amount, long price) {
        this.offerId = offerId;
        this.playerUUID = playerUUID;
        this.stackWrapper = offeredStack;
        this.amount = amount;
        this.price = price;
    }

    @Override
    public long getId() {
        return this.offerId;
    }

    public UUID getPlayerUUID() {
        return this.playerUUID;
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

    public void setOwnerUsername(String username) {
        this.username = username;
    }

    public String getOwnerUsername() {
        return this.username;
    }

    public boolean isOwner(UUID playerUUID) {
        return this.playerUUID.equals(playerUUID);
    }

    @Override
    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write(this.offerId, bos);
        StreamUtils.write(this.playerUUID, bos);
        this.stackWrapper.write(bos);
        StreamUtils.write((short) this.amount, bos);
        StreamUtils.write(this.price, bos);
    }

    @Override
    public void read(BufferedInputStream bis) throws IOException {
        this.offerId = StreamUtils.readLong(bis);
        this.playerUUID = StreamUtils.readUUID(bis);
        this.stackWrapper = ItemStackWrapper.read(bis);
        this.amount = StreamUtils.readShort(bis);
        this.price = StreamUtils.readLong(bis);
    }

    @Override
    public void write(ByteBuf buffer) {
        buffer.writeLong(this.offerId);
        ByteBufUtils.writeString(this.username, buffer);
        this.stackWrapper.write(buffer);
        buffer.writeShort(this.amount);
        buffer.writeLong(this.price);
    }

    @Override
    public void read(ByteBuf buffer) {}

    @Override
    public String toString() {
        return String.format("OfferServer[id: %d, seller: %s/%s, itemstack: %s, amount: %d, price: %s]", 
                this.offerId,
                this.username,
                this.playerUUID,
                this.stackWrapper,
                this.amount,
                this.price);
    }
}
