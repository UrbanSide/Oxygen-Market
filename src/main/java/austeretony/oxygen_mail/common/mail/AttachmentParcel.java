package austeretony.oxygen_mail.common.mail;

import austeretony.oxygen_core.client.api.PrivilegesClient;
import austeretony.oxygen_core.client.gui.base.GUIUtils;
import austeretony.oxygen_core.client.gui.base.core.Widget;
import austeretony.oxygen_core.client.settings.CoreSettings;
import austeretony.oxygen_core.common.api.OxygenCommon;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.player.shared.PlayerSharedData;
import austeretony.oxygen_core.common.sound.SoundEffects;
import austeretony.oxygen_core.common.util.CommonUtils;
import austeretony.oxygen_core.common.util.MinecraftCommon;
import austeretony.oxygen_core.common.util.objects.Pair;
import austeretony.oxygen_core.server.api.OxygenServer;
import austeretony.oxygen_core.server.api.PrivilegesServer;
import austeretony.oxygen_core.server.operation.Operation;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.main.MailMain;
import austeretony.oxygen_mail.common.main.MailPrivileges;
import austeretony.oxygen_mail.server.api.MailServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AttachmentParcel implements Attachment {

    public static int MAX_ITEMS_PER_PARCEL = 4;

    private final Map<ItemStackWrapper, Integer> itemsMap;

    protected AttachmentParcel(Map<ItemStackWrapper, Integer> itemsMap) {
        this.itemsMap = itemsMap;
    }

    @Override
    public AttachmentType getType() {
        return AttachmentType.PARCEL;
    }

    @Override
    public boolean isValid(EntityPlayerMP playerMP) {
        if (itemsMap.size() > MAX_ITEMS_PER_PARCEL) return false;
        int maxStackSize = PrivilegesServer.getInt(MinecraftCommon.getEntityUUID(playerMP), MailPrivileges.PARCEL_MAX_STACK_SIZE.getId(),
                MailConfig.PARCEL_MAX_STACK_SIZE.asInt());

        for (Map.Entry<ItemStackWrapper, Integer> entry : itemsMap.entrySet()) {
            if (OxygenServer.isItemBlacklisted(MailMain.ITEMS_BLACKLIST_MAIL, entry.getKey())) {
                return false;
            }
            int maxStack = maxStackSize;
            if (maxStack < 0) {
                maxStack = OxygenCommon.getMaxItemStackSize(entry.getKey());
            }
            if (entry.getValue() <= 0 || entry.getValue() > maxStack) {
                return false;
            }
        }
        return true;
    }

    @Override
    public long getExpireTimeMillis(MailEntry mailEntry) {
        int hours = mailEntry.isSentByPlayer() ? MailConfig.PARCEL_EXPIRE_TIME_HOURS.asInt()
                : MailConfig.SYSTEM_PARCEL_EXPIRE_TIME_HOURS.asInt();
        return TimeUnit.HOURS.toMillis(hours);
    }

    @Override
    public Pair<Integer, Long> getPostage(EntityPlayerMP player) {
        return Pair.of(OxygenMain.CURRENCY_COINS, PrivilegesServer.getLong(MinecraftCommon.getEntityUUID(player),
                MailPrivileges.PARCEL_POSTAGE_VALUE.getId(), MailConfig.PARCEL_POSTAGE_VALUE.asLong()) * itemsMap.size());
    }

    @Override
    public Pair<Integer, Long> getBalance(EntityPlayerMP playerMP) {
        int currencyIndex = OxygenMain.CURRENCY_COINS;
        return Pair.of(currencyIndex, OxygenServer.getWatcherValue(MinecraftCommon.getEntityUUID(playerMP), currencyIndex, 0L));
    }

    @Override
    public void send(EntityPlayerMP playerMP, Operation operation) {
        operation.withItemsWithdraw(itemsMap);
    }

    @Override
    public void playSendSound(EntityPlayerMP playerMP) {
        OxygenServer.playSound(playerMP, SoundEffects.miscInventoryOperation);
    }

    @Override
    public void receive(Operation operation, MailEntry mailEntry) {
        operation.withItemsAdd(itemsMap);
    }

    @Override
    public void playReceiveSound(EntityPlayerMP playerMP) {
        OxygenServer.playSound(playerMP, SoundEffects.miscInventoryOperation);
    }

    @Override
    public boolean canBeReturned(MailEntry mailEntry) {
        return mailEntry.isSentByPlayer();
    }

    @Override
    public void returnToSender(UUID initiatorUUID, MailEntry mailEntry) {
        boolean isSystemReturn = initiatorUUID.equals(OxygenMain.SYSTEM_UUID);
        String subject = isSystemReturn ? "mail.return_exp.subject" : "mail.return.subject";
        String message = isSystemReturn ? "mail.return_exp.message" : "mail.return.message";

        PlayerSharedData sharedData = OxygenServer.getPlayerSharedData(initiatorUUID);
        String addresseeArg = OxygenMain.SYSTEM_SENDER;
        if (sharedData != null) {
            addresseeArg = sharedData.getUsername();
        }

        MailServer.systemMail(mailEntry.getSenderUUID(), subject)
                .withSenderName(OxygenMain.SYSTEM_SENDER)
                .withMessage(message, addresseeArg, mailEntry.getSubject())
                .withAttachment(Attachments.parcel(itemsMap))
                .withMailBoxCapacityIgnore()
                .send();
    }

    @Override
    public void write(ByteBuf buffer) {
        buffer.writeByte(itemsMap.size());
        for (Map.Entry<ItemStackWrapper, Integer> entry : itemsMap.entrySet()) {
            entry.getKey().write(buffer);
            buffer.writeShort(entry.getValue());
        }
    }

    public static AttachmentParcel read(ByteBuf buffer) {
        int amount = buffer.readByte();
        Map<ItemStackWrapper, Integer> itemsMap = new LinkedHashMap<>(amount);
        for (int i = 0; i < amount; i++) {
            itemsMap.put(ItemStackWrapper.read(buffer), (int) buffer.readShort());
        }
        return new AttachmentParcel(itemsMap);
    }

    @Override
    public NBTTagCompound writeToNBT() {
        NBTTagCompound tagCompound = new NBTTagCompound();

        NBTTagList itemsList = new NBTTagList();
        for (Map.Entry<ItemStackWrapper, Integer> entry : itemsMap.entrySet()) {
            NBTTagCompound entryTag = new NBTTagCompound();
            entryTag.setTag("item_stack", entry.getKey().writeToNBT());
            entryTag.setShort("quantity", entry.getValue().shortValue());
            itemsList.appendTag(entryTag);
        }
        tagCompound.setTag("items_list", itemsList);

        return tagCompound;
    }

    public static AttachmentParcel readFromNBT(NBTTagCompound tagCompound) {
        NBTTagList itemsList = tagCompound.getTagList("items_list", 10);
        Map<ItemStackWrapper, Integer> itemsMap = new LinkedHashMap<>(itemsList.tagCount());

        for (int i = 0; i < itemsList.tagCount(); i++) {
            NBTTagCompound entryTag = itemsList.getCompoundTagAt(i);
            itemsMap.put(ItemStackWrapper.readFromNBT(entryTag.getCompoundTag("item_stack")),
                    (int) entryTag.getShort("quantity"));
        }
        return new AttachmentParcel(itemsMap);
    }

    @Override
    public boolean isValid() {
        if (itemsMap.size() > MAX_ITEMS_PER_PARCEL) return false;
        int maxStackSize = PrivilegesClient.getInt(MailPrivileges.PARCEL_MAX_STACK_SIZE.getId(), MailConfig.PARCEL_MAX_STACK_SIZE.asInt());

        for (Map.Entry<ItemStackWrapper, Integer> entry : itemsMap.entrySet()) {
            int maxStack = maxStackSize;
            if (maxStack < 0) {
                maxStack = OxygenCommon.getMaxItemStackSize(entry.getKey());
            }
            if (entry.getValue() <= 0 || entry.getValue() > maxStack) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Pair<Integer, Long> getPostage() {
        return Pair.of(OxygenMain.CURRENCY_COINS, PrivilegesClient.getLong(MailPrivileges.PARCEL_POSTAGE_VALUE.getId(),
                MailConfig.PARCEL_POSTAGE_VALUE.asLong()) * itemsMap.size());
    }

    @Override
    public void draw(Widget widget, int mouseX, int mouseY) {
        int index = 0;
        for (Map.Entry<ItemStackWrapper, Integer> entry : itemsMap.entrySet()) {
            int x = index * 18 + index;
            ItemStack itemStack = entry.getKey().getItemStackCached();
            GUIUtils.renderItemStack(itemStack, x, 0, CoreSettings.ENABLE_DURABILITY_BARS_GUI_DISPLAY.asBoolean());

            if (entry.getValue() > 1) {
                float textScale = CoreSettings.SCALE_TEXT_ADDITIONAL.asFloat() - .05F;
                GUIUtils.drawString(String.valueOf(entry.getValue()), 14 + x, 12, textScale,
                        CoreSettings.COLOR_TEXT_BASE_ENABLED.asInt(), true);
            }
            index++;
        }
    }

    @Override
    public void drawForeground(Widget widget, int mouseX, int mouseY) {
        int index = 0;
        for (ItemStackWrapper stackWrapper : itemsMap.keySet()) {
            int boundX = index * 18 + index;
            if (mouseX >= widget.getX() + boundX && mouseY >= widget.getY() && mouseX < widget.getX() + boundX + 16
                    && mouseY < widget.getY() + 16) {
                int offset = 16 + 6;
                int x = widget.getX() + boundX + offset;
                int y = widget.getY() + 2;

                ItemStack itemStack = stackWrapper.getItemStackCached();
                List<String> tooltipLines = GUIUtils.getItemStackToolTip(itemStack);
                float width = 0;
                for (String line : tooltipLines) {
                    float lineWidth = GUIUtils.getTextWidth(line, CoreSettings.SCALE_TEXT_TOOLTIP.asFloat()) + 6F;
                    if (lineWidth > width) {
                        width = lineWidth;
                    }
                }
                int startX = widget.getScreenX() + width + boundX + offset > widget.getScreen().width ? (int) (x - width - offset) : x;

                Widget.drawToolTip(startX, y, tooltipLines);
            }
            index++;
        }
    }

    @Override
    public String toString() {
        return "AttachmentParcel[" +
                "itemsMap= " + CommonUtils.formatForLogging(itemsMap) +
                "]";
    }
}
