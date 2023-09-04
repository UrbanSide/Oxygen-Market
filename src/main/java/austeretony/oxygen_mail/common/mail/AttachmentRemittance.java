package austeretony.oxygen_mail.common.mail;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.api.PrivilegesClient;
import austeretony.oxygen_core.client.gui.base.GUIUtils;
import austeretony.oxygen_core.client.gui.base.core.Widget;
import austeretony.oxygen_core.client.preset.CurrencyProperties;
import austeretony.oxygen_core.client.settings.CoreSettings;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.player.shared.PlayerSharedData;
import austeretony.oxygen_core.common.sound.SoundEffects;
import austeretony.oxygen_core.common.util.*;
import austeretony.oxygen_core.common.util.objects.Pair;
import austeretony.oxygen_core.server.api.OxygenServer;
import austeretony.oxygen_core.server.api.PrivilegesServer;
import austeretony.oxygen_core.server.operation.Operation;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.main.MailPrivileges;
import austeretony.oxygen_mail.server.api.MailServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AttachmentRemittance implements Attachment {

    private final int currencyIndex;
    private final long value;

    //client
    private CurrencyProperties properties;

    protected AttachmentRemittance(int currencyIndex, long value) {
        this.currencyIndex = currencyIndex;
        this.value = value;
    }

    @Override
    public AttachmentType getType() {
        return AttachmentType.REMITTANCE;
    }

    @Override
    public boolean isValid(EntityPlayerMP playerMP) {
        return OxygenServer.getCurrencyProvider(currencyIndex) != null && value > 0L
                && value < PrivilegesServer.getLong(MinecraftCommon.getEntityUUID(playerMP), MailPrivileges.REMITTANCE_MAX_VALUE.getId(),
                MailConfig.REMITTANCE_MAX_VALUE.asLong());
    }

    @Override
    public long getExpireTimeMillis(MailEntry mailEntry) {
        int hours = mailEntry.isSentByPlayer() ? MailConfig.REMITTANCE_EXPIRE_TIME_HOURS.asInt()
                : MailConfig.SYSTEM_REMITTANCE_EXPIRE_TIME_HOURS.asInt();
        return TimeUnit.HOURS.toMillis(hours);
    }

    @Override
    public Pair<Integer, Long> getPostage(EntityPlayerMP playerMP) {
        float percent = PrivilegesServer.getFloat(MinecraftCommon.getEntityUUID(playerMP), MailPrivileges.REMITTANCE_POSTAGE_PERCENT.getId(),
                MailConfig.REMITTANCE_POSTAGE_PERCENT.asFloat());
        return Pair.of(currencyIndex, (long) (value * MathUtils.clamp(percent, 0F, 1F)));
    }

    @Override
    public Pair<Integer, Long> getBalance(EntityPlayerMP playerMP) {
        return Pair.of(currencyIndex, OxygenServer.getWatcherValue(MinecraftCommon.getEntityUUID(playerMP), currencyIndex, 0L));
    }

    @Override
    public void send(EntityPlayerMP playerMP, Operation operation) {
        operation.withCurrencyWithdraw(currencyIndex, value);
    }

    @Override
    public void playSendSound(EntityPlayerMP playerMP) {
        OxygenServer.playSound(playerMP, SoundEffects.miscRingingCoins);
    }

    @Override
    public void receive(Operation operation, MailEntry mailEntry) {
        operation.withCurrencyGain(currencyIndex, value);
    }

    @Override
    public void playReceiveSound(EntityPlayerMP playerMP) {
        OxygenServer.playSound(playerMP, SoundEffects.miscRingingCoins);
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
                .withAttachment(Attachments.remittance(currencyIndex, value))
                .withMailBoxCapacityIgnore()
                .send();
    }

    @Override
    public void write(ByteBuf buffer) {
        buffer.writeByte(currencyIndex);
        buffer.writeLong(value);
    }

    public static AttachmentRemittance read(ByteBuf buffer) {
        return new AttachmentRemittance(buffer.readByte(), buffer.readLong());
    }

    @Override
    public NBTTagCompound writeToNBT() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setByte("currency_index", (byte) currencyIndex);
        tagCompound.setLong("value", value);
        return tagCompound;
    }

    public static AttachmentRemittance readFromNBT(NBTTagCompound tagCompound) {
        return new AttachmentRemittance(tagCompound.getByte("currency_index"), tagCompound.getLong("value"));
    }

    @Override
    public boolean isValid() {
        return OxygenClient.getCurrencyProperties(currencyIndex) != null && value > 0L
                && value < PrivilegesClient.getLong(MailPrivileges.REMITTANCE_MAX_VALUE.getId(), MailConfig.REMITTANCE_MAX_VALUE.asLong());
    }

    @Override
    public Pair<Integer, Long> getPostage() {
        float percent = PrivilegesClient.getFloat(MailPrivileges.REMITTANCE_POSTAGE_PERCENT.getId(),
                MailConfig.REMITTANCE_POSTAGE_PERCENT.asFloat());
        return Pair.of(currencyIndex, (long) (value * MathUtils.clamp(percent, 0F, 1F)));
    }

    @Override
    public void draw(Widget widget, int mouseX, int mouseY) {
        if (properties == null) {
            properties = OxygenClient.getCurrencyProperties(currencyIndex);
        }
        if (properties == null) return;

        GUIUtils.colorDef();
        GUIUtils.drawTexturedRect(0F,
                (widget.getHeight() - (properties.getIconHeight() + properties.getIconYOffset())) / 2F,
                properties.getIconWidth(), properties.getIconHeight(), properties.getIconTexture(), 0, 0,
                properties.getIconWidth(), properties.getIconHeight());

        float textScale = CoreSettings.SCALE_TEXT_ADDITIONAL.asFloat() - .05F;
        float textHeight = GUIUtils.getTextHeight(textScale);
        GUIUtils.drawString(CommonUtils.formatCurrencyValue(value), properties.getIconWidth() + 2 * properties.getIconXOffset() + 2,
                (widget.getHeight() - textHeight) / 2F + .5F, textScale, CoreSettings.COLOR_TEXT_BASE_ENABLED.asInt(), false);
    }

    @Override
    public void drawForeground(Widget widget, int mouseX, int mouseY) {
        if (properties == null) {
            properties = OxygenClient.getCurrencyProperties(currencyIndex);
        }
        if (properties == null) return;

        if (mouseX >= widget.getX() && mouseY >= widget.getY() && mouseX < widget.getX() + 16
                && mouseY < widget.getY() + 16) {
            Widget.drawToolTip(mouseX, mouseY - Widget.TOOLTIP_HEIGHT, properties.getLocalizedName());
        }
    }

    @Override
    public String toString() {
        return "AttachmentRemittance[" +
                "currencyIndex= " + currencyIndex + ", " +
                "value= " + value +
                "]";
    }
}
