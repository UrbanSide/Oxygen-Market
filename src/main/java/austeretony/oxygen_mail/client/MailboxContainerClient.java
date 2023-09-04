package austeretony.oxygen_mail.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.persistent.AbstractPersistentData;
import austeretony.oxygen_core.common.util.StreamUtils;
import austeretony.oxygen_mail.common.mail.Mail;
import io.netty.util.internal.ConcurrentSet;

public class MailboxContainerClient extends AbstractPersistentData {

    private final Map<Long, Mail> mail = new ConcurrentHashMap<>();

    private final Set<Long> read = new ConcurrentSet<>();

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

    public void addMessage(Mail message) {
        this.mail.put(message.getId(), message);
    }

    public void removeMessage(long messageId) {
        this.mail.remove(messageId);
    }

    public boolean isMarkedAsRead(long messageId) {
        return this.read.contains(messageId);
    }

    public void markAsRead(long messageId) {
        this.read.add(messageId);
    }

    public void removeReadMark(long messageId) {
        this.read.remove(messageId);
    }

    @Override
    public String getDisplayName() {
        return "mail:mailbox_client";
    }

    @Override
    public String getPath() {
        return OxygenHelperClient.getDataFolder() + "/client/players/" + OxygenHelperClient.getPlayerUUID() + "/mail/mail_client.dat";
    }

    @Override
    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write((short) this.mail.size(), bos);
        for (Mail message : this.mail.values())
            message.write(bos);
        StreamUtils.write((short) this.read.size(), bos);
        for (long messageId : this.read)
            StreamUtils.write(messageId, bos);
    }

    @Override
    public void read(BufferedInputStream bis) throws IOException {
        int 
        amount = StreamUtils.readShort(bis),
        i = 0;
        Mail message;
        for (; i < amount; i++) {
            message = new Mail();
            message.read(bis);
            this.mail.put(message.getId(), message);
        }
        amount = StreamUtils.readShort(bis);
        for (i = 0; i < amount; i++)
            this.read.add(StreamUtils.readLong(bis));
    }

    @Override
    public void reset() {
        this.mail.clear();
    }
}
