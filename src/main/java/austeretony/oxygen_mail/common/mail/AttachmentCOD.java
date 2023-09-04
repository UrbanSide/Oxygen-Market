package austeretony.oxygen_mail.common.mail;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.api.PrivilegesClient;
import austeretony.oxygen_core.client.gui.base.GUIUtils;
import austeretony.oxygen_core.client.gui.base.core.Widget;
import austeretony.oxygen_core.client.preset.CurrencyProperties;
import austeretony.oxygen_core.client.settings.CoreSettings;
import austeretony.oxygen_core.client.util.MinecraftClient;
import austeretony.oxygen_core.common.api.OxygenCommon;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.player.shared.PlayerSharedData;
import austeretony.oxygen_core.common.sound.SoundEffects;
import austeretony.oxygen_core.common.util.*;
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

import java.util.*;
import java.util.concurrent.TimeUnit;

public class AttachmentCOD implements Attachment {

    public static int MAX_ITEMS_PER_PARCEL = 4;

    private final int currencyIndex;
    private final Map<ItemStackWrapper, Integer> itemsMap;
    private final long value;

    //client
    private CurrencyProperties properties;

    protected AttachmentCOD(int currencyIndex, long value, Map<ItemStackWrapper, Integer> itemsMap) {
        this.currencyIndex = currencyIndex;
        this.value = value;
        this.itemsMap = itemsMap;
    }

    @Override
    public AttachmentType getType() {
        return AttachmentType.COD;
    }

    @Override
    public boolean isValid(EntityPlayerMP playerMP) {
        UUID playerUUID = MinecraftCommon.getEntityUUID(playerMP);
        if (itemsMap.size() > MAX_ITEMS_PER_PARCEL || value > PrivilegesServer.getLong(playerUUID,
                MailPrivileges.COD_MAX_VALUE.getId(), MailConfig.COD_MAX_VALUE.asLong())) return false;
        int maxStackSize = PrivilegesServer.getInt(playerUUID, MailPrivileges.PARCEL_MAX_STACK_SIZE.getId(),
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
        return OxygenServer.getCurrencyProvider(currencyIndex) != null && value > 0L;
    }

    @Override
    public long getExpireTimeMillis(MailEntry mailEntry) {
        return TimeUnit.HOURS.toMillis(MailConfig.COD_EXPIRE_TIME_HOURS.asInt());
    }

    @Override
    public Pair<Integer, Long> getPostage(EntityPlayerMP playerMP) {
        return Pair.of(currencyIndex, PrivilegesServer.getLong(MinecraftCommon.getEntityUUID(playerMP),
                MailPrivileges.PARCEL_POSTAGE_VALUE.getId(), MailConfig.PARCEL_POSTAGE_VALUE.asLong()) * itemsMap.size());
    }

    @Override
    public Pair<Integer, Long> getBalance(EntityPlayerMP playerMP) {
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
        operation.withCurrencyWithdraw(currencyIndex, value);
        operation.withItemsAdd(itemsMap);

        if (mailEntry.isSentByPlayer()) {
            float feePercent = PrivilegesServer.getFloat(MinecraftCommon.getEntityUUID(operation.getPlayer()), MailPrivileges.COD_PRICE_FEE_PERCENT.getId(),
                    MailConfig.COD_PRICE_FEE_PERCENT.asFloat());
            long income = value - (long) (value * MathUtils.clamp(feePercent, 0F, 1F));

            MailServer.systemMail(mailEntry.getSenderUUID(), "mail.cod_income.subject")
                    .withSenderName(OxygenMain.SYSTEM_SENDER)
                    .withMessage("mail.cod_income.message",
                            MinecraftCommon.getEntityName(operation.getPlayer()), mailEntry.getSubject())
                    .withAttachment(Attachments.remittance(currencyIndex, income))
                    .withMailBoxCapacityIgnore()
                    .send();
        }
    }

    @Override
    public void playReceiveSound(EntityPlayerMP playerMP) {
        OxygenServer.playSound(playerMP, SoundEffects.miscRingingCoins);
        OxygenServer.playSound(playerMP, SoundEffects.miscInventoryOperation);
    }

    @Override
    public boolean canBeReturned(MailEntry mailEntry) {
        return true;
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

        buffer.writeByte(currencyIndex);
        buffer.writeLong(value);
    }

    public static AttachmentCOD read(ByteBuf buffer) {
        int amount = buffer.readByte();
        Map<ItemStackWrapper, Integer> itemsMap = new LinkedHashMap<>(amount);
        for (int i = 0; i < amount; i++) {
            itemsMap.put(ItemStackWrapper.read(buffer), (int) buffer.readShort());
        }
        return new AttachmentCOD(buffer.readByte(), buffer.readLong(), itemsMap);
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

        tagCompound.setByte("currency_index", (byte) currencyIndex);
        tagCompound.setLong("value", value);

        return tagCompound;
    }

    public static AttachmentCOD readFromNBT(NBTTagCompound tagCompound) {
        NBTTagList itemsList = tagCompound.getTagList("items_list", 10);
        Map<ItemStackWrapper, Integer> itemsMap = new LinkedHashMap<>(itemsList.tagCount());

        for (int i = 0; i < itemsList.tagCount(); i++) {
            NBTTagCompound entryTag = itemsList.getCompoundTagAt(i);
            itemsMap.put(ItemStackWrapper.readFromNBT(entryTag.getCompoundTag("item_stack")),
                    (int) entryTag.getShort("quantity"));
        }

        return new AttachmentCOD(tagCompound.getByte("currency_index"), tagCompound.getLong("value"), itemsMap);
    }

    @Override
    public boolean isValid() {
        if (itemsMap.size() > MAX_ITEMS_PER_PARCEL
                || value > PrivilegesClient.getLong(MailPrivileges.COD_MAX_VALUE.getId(), MailConfig.COD_MAX_VALUE.asLong())) return false;
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
        return OxygenClient.getCurrencyProperties(currencyIndex) != null && value > 0L;
    }

    @Override
    public Pair<Integer, Long> getPostage() {
        return Pair.of(OxygenMain.CURRENCY_COINS, PrivilegesClient.getLong(MailPrivileges.PARCEL_POSTAGE_VALUE.getId(),
                MailConfig.PARCEL_POSTAGE_VALUE.asLong()) * itemsMap.size());
    }

    @Override
    public void draw(Widget widget, int mouseX, int mouseY) {
        int index = 0;
        int x = 0;
        for (Map.Entry<ItemStackWrapper, Integer> entry : itemsMap.entrySet()) {
            x = index * 18 + index;
            ItemStack itemStack = entry.getKey().getItemStackCached();
            GUIUtils.renderItemStack(itemStack, x, 0, CoreSettings.ENABLE_DURABILITY_BARS_GUI_DISPLAY.asBoolean());

            if (entry.getValue() > 1) {
                float textScale = CoreSettings.SCALE_TEXT_ADDITIONAL.asFloat() - .05F;
                GUIUtils.drawString(String.valueOf(entry.getValue()), 14 + x, 12, textScale,
                        CoreSettings.COLOR_TEXT_BASE_ENABLED.asInt(), true);
            }
            index++;
        }


        float textScale = CoreSettings.SCALE_TEXT_ADDITIONAL.asFloat() - .05F;
        float textHeight = GUIUtils.getTextHeight(textScale);
        GUIUtils.drawString(MinecraftClient.localize("oxygen_mail.gui.mail.label.price"),x + 16 + 8,
                (8 - textHeight) / 2F + .5F, textScale, CoreSettings.COLOR_TEXT_BASE_ENABLED.asInt(), false);

        if (properties == null) {
            properties = OxygenClient.getCurrencyProperties(currencyIndex);
        }
        if (properties == null) return;

        GUIUtils.colorDef();
        GUIUtils.drawTexturedRect(x + 16F + 8F,
                8 + (8 - (properties.getIconHeight() + properties.getIconYOffset())) / 2F,
                properties.getIconWidth(), properties.getIconHeight(), properties.getIconTexture(), 0, 0,
                properties.getIconWidth(), properties.getIconHeight());

        GUIUtils.drawString(CommonUtils.formatCurrencyValue(value), x + 16 + 8 + properties.getIconWidth() + 2 * properties.getIconXOffset() + 2,
                8 + (8 - textHeight) / 2F + .5F, textScale, CoreSettings.COLOR_TEXT_BASE_ENABLED.asInt(), false);
    }

    @Override
    public void drawForeground(Widget widget, int mouseX, int mouseY) {
        int index = 0;
        int boundX = 0;
        for (ItemStackWrapper stackWrapper : itemsMap.keySet()) {
            boundX = index * 18 + index;
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

        if (properties == null) {
            properties = OxygenClient.getCurrencyProperties(currencyIndex);
        }
        if (properties == null) return;

        if (mouseX >= widget.getX() + boundX + 16 + 8 && mouseY >= widget.getY() && mouseX < widget.getX() + boundX + 16 + 8 + 16
                && mouseY < widget.getY() + 16) {
            Widget.drawToolTip(mouseX, mouseY - Widget.TOOLTIP_HEIGHT, properties.getLocalizedName());
        }
    }

    @Override
    public String toString() {
        return "AttachmentCOD[" +
                "currencyIndex= " + currencyIndex + ", " +
                "value= " + value + ", " +
                "itemsMap" + CommonUtils.formatForLogging(itemsMap) +
                "]";
    }
}
