package austeretony.oxygen_mail.common.mail;

import austeretony.oxygen_core.client.api.PrivilegesClient;
import austeretony.oxygen_core.client.gui.base.core.Widget;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.util.MinecraftCommon;
import austeretony.oxygen_core.common.util.objects.Pair;
import austeretony.oxygen_core.server.api.OxygenServer;
import austeretony.oxygen_core.server.api.PrivilegesServer;
import austeretony.oxygen_core.server.operation.Operation;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.main.MailPrivileges;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AttachmentNone implements Attachment {

    protected AttachmentNone() {}

    @Override
    public AttachmentType getType() {
        return AttachmentType.NONE;
    }

    @Override
    public boolean isValid(EntityPlayerMP playerMP) {
        return true;
    }

    @Override
    public long getExpireTimeMillis(MailEntry mailEntry) {
        int hours = mailEntry.isSentByPlayer() ? MailConfig.LETTER_EXPIRE_TIME_HOURS.asInt()
                : MailConfig.SYSTEM_LETTER_EXPIRE_TIME_HOURS.asInt();
        return TimeUnit.HOURS.toMillis(hours);
    }

    @Override
    public Pair<Integer, Long> getPostage(EntityPlayerMP player) {
        return Pair.of(OxygenMain.CURRENCY_COINS, PrivilegesServer.getLong(MinecraftCommon.getEntityUUID(player),
                MailPrivileges.LETTER_POSTAGE_VALUE.getId(), MailConfig.LETTER_POSTAGE_VALUE.asLong()));
    }

    @Override
    public Pair<Integer, Long> getBalance(EntityPlayerMP playerMP) {
        int currencyIndex = OxygenMain.CURRENCY_COINS;
        return Pair.of(currencyIndex, OxygenServer.getWatcherValue(MinecraftCommon.getEntityUUID(playerMP), currencyIndex, 0L));
    }

    @Override
    public void send(EntityPlayerMP playerMP, Operation operation) {}

    @Override
    public void receive(Operation operation, MailEntry mailEntry) {}

    @Override
    public boolean canBeReturned(MailEntry mailEntry) {
        return false;
    }

    @Override
    public void returnToSender(UUID initiatorUUID, MailEntry mailEntry) {}

    @Override
    public void write(ByteBuf buffer) {}

    @Override
    public NBTTagCompound writeToNBT() {
        return new NBTTagCompound();
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Pair<Integer, Long> getPostage() {
        return Pair.of(OxygenMain.CURRENCY_COINS, PrivilegesClient.getLong(MailPrivileges.LETTER_POSTAGE_VALUE.getId(),
                MailConfig.LETTER_POSTAGE_VALUE.asLong()));
    }

    @Override
    public void draw(Widget widget, int mouseX, int mouseY) {}

    @Override
    public void drawForeground(Widget widget, int mouseX, int mouseY) {}

    @Override
    public String toString() {
        return "AttachmentNone[]";
    }
}
