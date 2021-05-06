package austeretony.oxygen_market.common.market;

import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.sync.SynchronousEntry;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public class SalesHistoryEntry implements SynchronousEntry {

    protected long id, price;
    protected ItemStackWrapper stackWrapper;
    protected int quantity;

    public SalesHistoryEntry() {}

    public SalesHistoryEntry(long id, ItemStackWrapper stackWrapper, int quantity, long price) {
        this.id = id;
        this.stackWrapper = stackWrapper;
        this.quantity = quantity;
        this.price = price;
    }

    @Override
    public long getId() {
        return id;
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

    @Override
    public void write(ByteBuf buffer) {
        buffer.writeLong(id);
        stackWrapper.write(buffer);
        buffer.writeShort(quantity);
        buffer.writeLong(price);
    }

    @Override
    public void read(ByteBuf buffer) {
        id = buffer.readLong();
        stackWrapper = ItemStackWrapper.read(buffer);
        quantity = buffer.readShort();
        price = buffer.readLong();
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setLong("id", id);
        tagCompound.setTag("item_stack", stackWrapper.writeToNBT());
        tagCompound.setShort("quantity", (short) quantity);
        tagCompound.setLong("price", price);
        return tagCompound;
    }

    public static SalesHistoryEntry readFromNBT(NBTTagCompound tagCompound) {
        SalesHistoryEntry entry = new SalesHistoryEntry();
        entry.id = tagCompound.getLong("id");
        entry.stackWrapper = ItemStackWrapper.readFromNBT(tagCompound.getCompoundTag("item_stack"));
        entry.quantity = tagCompound.getShort("quantity");
        entry.price = tagCompound.getLong("price");
        return entry;
    }

    @Override
    public String toString() {
        return "SalesHistoryEntry[" +
                "id= " + id + ", " +
                "stackWrapper= " + stackWrapper + ", " +
                "quantity= " + quantity + ", " +
                "price= " + price +
                "]";
    }
}
