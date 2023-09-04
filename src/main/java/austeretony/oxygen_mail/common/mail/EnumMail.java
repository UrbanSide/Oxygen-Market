package austeretony.oxygen_mail.common.mail;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import austeretony.oxygen_core.client.api.ClientReference;
import io.netty.buffer.ByteBuf;

public enum EnumMail {

    LETTER("letter") {

        @Override
        public Attachment readAttachment(BufferedInputStream bis) {
            return new AttachmentDummy();
        }

        @Override
        public Attachment readAttachment(ByteBuf buffer) {
            return new AttachmentDummy();
        }
    },
    REMITTANCE("remittance") {

        @Override
        public Attachment readAttachment(BufferedInputStream bis) throws IOException {
            return AttachmentRemittance.read(bis);
        }

        @Override
        public Attachment readAttachment(ByteBuf buffer) {
            return AttachmentRemittance.read(buffer);
        }
    },
    PARCEL("parcel") {

        @Override
        public Attachment readAttachment(BufferedInputStream bis) throws IOException {
            return AttachmentParcel.read(bis);
        }

        @Override
        public Attachment readAttachment(ByteBuf buffer) {
            return AttachmentParcel.read(buffer);
        }
    },
    COD("cod") {

        @Override
        public Attachment readAttachment(BufferedInputStream bis) throws IOException {
            return AttachmentCOD.read(bis);
        }

        @Override
        public Attachment readAttachment(ByteBuf buffer) {
            return AttachmentCOD.read(buffer);
        }
    };

    private final String name;

    EnumMail(String name) {
        this.name = name;
    }

    public abstract Attachment readAttachment(BufferedInputStream bis) throws IOException;

    public abstract Attachment readAttachment(ByteBuf buffer);

    public String localizedName() {
        return ClientReference.localize("oxygen_mail.mail." + this.name);
    }
}