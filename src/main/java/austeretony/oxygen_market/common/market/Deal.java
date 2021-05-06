package austeretony.oxygen_market.common.market;

import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.sync.SynchronousEntry;
import austeretony.oxygen_core.common.util.ByteBufUtils;
import austeretony.oxygen_core.common.util.nbt.NBTUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

import java.util.UUID;

public class Deal implements SynchronousEntry {

    private long id, price;
    private UUID sellerUUID;
    private String sellerUsername;
    private ItemStackWrapper stackWrapper;
    private int quantity;

    public Deal() {}

    public Deal(long id, UUID sellerUUID, String sellerUsername, ItemStackWrapper stackWrapper, int quantity,
                long price) {
        this.id = id;
        this.sellerUUID = sellerUUID;
        this.sellerUsername = sellerUsername;
        this.stackWrapper = stackWrapper;
        this.quantity = quantity;
        this.price = price;
    }

    @Override
    public long getId() {
        return id;
    }

    public UUID getSellerUUID() {
        return sellerUUID;
    }

    public String getSellerUsername() {
        return sellerUsername;
    }

    public ItemStackWrapper getStackWrapper() {
        return stackWrapper;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getPrice() {
        return price;
    }

    public double getUnitPrice() {
        return (double) price / quantity;
    }

    public boolean isEqual(Deal other) {
        return price == other.price &&
                quantity == other.quantity &&
                sellerUUID.equals(other.sellerUUID) &&
                sellerUsername.equals(other.sellerUsername) &&
                stackWrapper.equals(other.stackWrapper);
    }

    @Override
    public void write(ByteBuf buffer) {
        buffer.writeLong(id);
        ByteBufUtils.writeUUID(sellerUUID, buffer);
        ByteBufUtils.writeString(sellerUsername, buffer);
        stackWrapper.write(buffer);
        buffer.writeShort(quantity);
        buffer.writeLong(price);
    }

    @Override
    public void read(ByteBuf buffer) {
        id = buffer.readLong();
        sellerUUID = ByteBufUtils.readUUID(buffer);
        sellerUsername = ByteBufUtils.readString(buffer);
        stackWrapper = ItemStackWrapper.read(buffer);
        quantity = buffer.readShort();
        price = buffer.readLong();
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setLong("id", id);
        tagCompound.setTag("seller_uuid", NBTUtils.toNBTUUID(sellerUUID));
        tagCompound.setString("seller_username", sellerUsername);
        tagCompound.setTag("item_stack", stackWrapper.writeToNBT());
        tagCompound.setShort("quantity", (short) quantity);
        tagCompound.setLong("price", price);
        return tagCompound;
    }

    public static Deal readFromNBT(NBTTagCompound tagCompound) {
        Deal deal = new Deal();
        deal.id = tagCompound.getLong("id");
        deal.sellerUUID = NBTUtils.fromNBTUUID(tagCompound.getTag("seller_uuid"));
        deal.sellerUsername = tagCompound.getString("seller_username");
        deal.stackWrapper = ItemStackWrapper.readFromNBT(tagCompound.getCompoundTag("item_stack"));
        deal.quantity = tagCompound.getShort("quantity");
        deal.price = tagCompound.getLong("price");
        return deal;
    }

    @Override
    public String toString() {
        return "Deal[" +
                "id= " + id + ", " +
                "sellerUUID= " + sellerUUID + ", " +
                "sellerUsername= " + sellerUsername + ", " +
                "stackWrapper= " + stackWrapper + ", " +
                "quantity= " + quantity + ", " +
                "price= " + price +
                "]";
    }
}
