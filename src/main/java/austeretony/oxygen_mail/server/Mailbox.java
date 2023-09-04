package austeretony.oxygen_mail.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import austeretony.oxygen_core.common.util.StreamUtils;
import austeretony.oxygen_core.server.api.PrivilegesProviderServer;
import austeretony.oxygen_core.server.api.TimeHelperServer;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.mail.Mail;
import austeretony.oxygen_mail.common.main.EnumMailPrivilege;

public class Mailbox {

    private final UUID playerUUID;

    private final Map<Long, Mail> mail = new ConcurrentHashMap<>();

    private long nextSendingTimeMillis;

    public Mailbox(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    public int getMessagesAmount() {
        return this.mail.size();
    }

    public Collection<Mail> getMessages() {
        return this.mail.values();
    }

    public Set<Long> getMessagesIds() {
        return this.mail.keySet();
    }

    @Nullable
    public Mail getMessage(long messageId) {
        return this.mail.get(messageId);
    }

    public void addMessage(Mail mail) {                       
        this.mail.put(mail.getId(), mail);
    }

    public void removeMessage(long messageId) {
        this.mail.remove(messageId);
    }

    public int getMaxCapacity() {
        return PrivilegesProviderServer.getAsInt(this.playerUUID, EnumMailPrivilege.MAILBOX_SIZE.id(), MailConfig.MAILBOX_SIZE.asInt());
    }

    public boolean canAcceptMessages() {
        return this.getMessagesAmount() < this.getMaxCapacity();
    }

    public boolean canSendMessage() {
        return System.currentTimeMillis() >= this.nextSendingTimeMillis;
    }

    public void applySendingCooldown() {
        this.nextSendingTimeMillis = TimeHelperServer.getCurrentMillis() 
                + PrivilegesProviderServer.getAsInt(this.playerUUID, EnumMailPrivilege.MAIL_SENDING_COOLDOWN_SECONDS.id(), MailConfig.MAIL_SENDING_COOLDOWN_SECONDS.asInt()) * 1000;
    }

    public  long createId(long seed) {
        long id = ++seed;
        while (this.mail.containsKey(id))
            id++;
        return id;
    }

    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write(this.playerUUID, bos);
        StreamUtils.write((short) this.mail.size(), bos);
        for (Mail message : this.mail.values()) 
            message.write(bos);
    }

    public static Mailbox read(BufferedInputStream bis) throws IOException {
        Mailbox mailbox = new Mailbox(StreamUtils.readUUID(bis));
        int amount = StreamUtils.readShort(bis);
        Mail message;
        for (int i = 0; i < amount; i++) {
            message = new Mail();
            message.read(bis);
            mailbox.mail.put(message.getId(), message);
        }
        return mailbox;
    }

    public void clear() {
        this.mail.clear();
    }
}
