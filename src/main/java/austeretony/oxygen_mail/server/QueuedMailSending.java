package austeretony.oxygen_mail.server;

import austeretony.oxygen_mail.common.mail.Attachment;
import austeretony.oxygen_mail.common.mail.EnumMail;
import net.minecraft.entity.player.EntityPlayerMP;

public class QueuedMailSending {

    final EntityPlayerMP senderMP;

    final EnumMail type;

    final String addresseeUsername, subject, message;

    final Attachment attachment;

    protected QueuedMailSending(EntityPlayerMP senderMP, String addresseeUsername, EnumMail type, String subject, String message, Attachment attachment) {
        this.senderMP = senderMP;
        this.addresseeUsername = addresseeUsername;
        this.type = type;
        this.subject = subject;
        this.message = message;
        this.attachment = attachment;
    }
}
