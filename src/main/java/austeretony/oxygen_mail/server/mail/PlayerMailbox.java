package austeretony.oxygen_mail.server.mail;

import austeretony.oxygen_core.common.util.nbt.NBTUtils;
import austeretony.oxygen_core.server.api.OxygenServer;
import austeretony.oxygen_core.server.api.PrivilegesServer;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.mail.MailEntry;
import austeretony.oxygen_mail.common.main.MailPrivileges;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerMailbox {

    private final UUID playerUUID;
    private final Map<Long, MailEntry> mailMap = new HashMap<>();

    private long lastSendTimeMillis;

    public PlayerMailbox(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public Map<Long, MailEntry> getMailMap() {
        return mailMap;
    }

    public void addMailEntry(MailEntry mailEntry) {
        mailMap.put(mailEntry.getId(), mailEntry);
    }

    @Nullable
    public MailEntry removeMailEntry(long entryId) {
        return mailMap.remove(entryId);
    }

    @Nullable
    public MailEntry getMailEntry(long entryId) {
        return mailMap.get(entryId);
    }

    public int getStoredMailEntriesAmount() {
        return mailMap.size();
    }

    public int getMaxCapacity() {
        return PrivilegesServer.getInt(playerUUID, MailPrivileges.MAILBOX_SIZE.getId(), MailConfig.MAILBOX_SIZE.asInt());
    }

    public boolean canReceiveMail() {
        return getStoredMailEntriesAmount() < getMaxCapacity();
    }

    public boolean canSendMail() {
        long expireTime = lastSendTimeMillis + MailConfig.MAIL_SENDING_COOL_DOWN_SECONDS.asInt() * 1000L;
        return PrivilegesServer.getBoolean(playerUUID, MailPrivileges.ALLOW_MAIL_SENDING.getId(), true)
                && OxygenServer.getCurrentTimeMillis() > expireTime;
    }

    public void setLastSendTimeMillis(long value) {
        lastSendTimeMillis = value;
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setTag("player_uuid", NBTUtils.toNBTUUID(playerUUID));
        tagCompound.setLong("last_send_time_millis", lastSendTimeMillis);

        NBTTagList mailList = new NBTTagList();
        for (MailEntry mailEntry : mailMap.values()) {
            mailList.appendTag(mailEntry.writeToNBT());
        }
        tagCompound.setTag("mail_list", mailList);

        return tagCompound;
    }

    public static PlayerMailbox readFromNBT(NBTTagCompound tagCompound) {
        PlayerMailbox mailbox = new PlayerMailbox(NBTUtils.fromNBTUUID(tagCompound.getTag("player_uuid")));
        mailbox.lastSendTimeMillis = tagCompound.getLong("last_send_time_millis");

        NBTTagList mailList = tagCompound.getTagList("mail_list", 10);
        for (int i = 0; i < mailList.tagCount(); i++) {
            MailEntry mailEntry = MailEntry.readFromNBT(mailList.getCompoundTagAt(i));
            mailbox.mailMap.put(mailEntry.getId(), mailEntry);
        }

        return mailbox;
    }
}
