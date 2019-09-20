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

public class PlayerOfferServer implements PersistentEntry, SynchronizedData {

    private long offerId, price;

    private UUID playerUUID;

    private ItemStackWrapper offeredStack;

    private int amount;

    private String username;

    public PlayerOfferServer() {}

    public PlayerOfferServer(long offerId, UUID playerUUID, ItemStackWrapper offeredStack, int amount, long price) {
        this.offerId = offerId;
        this.playerUUID = playerUUID;
        this.offeredStack = offeredStack;
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

    public ItemStackWrapper getOfferedStack() {
        return this.offeredStack;
    }

    public int getAmount() {
        return this.amount;
    }

    public long getPrice() {
        return this.price;
    }

    public void updateOwnerUsername() {
        this.username = OxygenHelperServer.getPlayerSharedData(this.playerUUID).getUsername();
    }

    public boolean isOwner(UUID playerUUID) {
        return this.playerUUID.equals(playerUUID);
    }

    @Override
    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write(this.offerId, bos);
        StreamUtils.write(this.playerUUID, bos);
        this.offeredStack.write(bos);
        StreamUtils.write((short) this.amount, bos);
        StreamUtils.write(this.price, bos);
    }

    @Override
    public void read(BufferedInputStream bis) throws IOException {
        this.offerId = StreamUtils.readLong(bis);
        this.playerUUID = StreamUtils.readUUID(bis);
        this.offeredStack = ItemStackWrapper.read(bis);
        this.amount = StreamUtils.readShort(bis);
        this.price = StreamUtils.readLong(bis);
    }

    @Override
    public void write(ByteBuf buffer) {
        buffer.writeLong(this.offerId);
        ByteBufUtils.writeString(this.username, buffer);
        this.offeredStack.write(buffer);
        buffer.writeShort(this.amount);
        buffer.writeLong(this.price);
    }

    @Override
    public void read(ByteBuf buffer) {}
}
