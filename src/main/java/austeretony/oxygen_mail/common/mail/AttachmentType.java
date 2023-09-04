package austeretony.oxygen_mail.common.mail;

import austeretony.oxygen_core.client.util.MinecraftClient;
import austeretony.oxygen_mail.client.mail.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public enum AttachmentType {

    NONE("none") {
        @Override
        public Attachment readFromNBT(NBTTagCompound tagCompound) {
            return new AttachmentNone();
        }

        @Override
        public Attachment read(ByteBuf buffer) {
            return new AttachmentNone();
        }

        @Override
        public SelectionWidgetSupplier getSelectionWidgetSupplier() {
            return new NoneAttachmentWidgetSupplier();
        }
    },
    REMITTANCE("remittance") {
        @Override
        public Attachment readFromNBT(NBTTagCompound tagCompound) {
            return AttachmentRemittance.readFromNBT(tagCompound);
        }

        @Override
        public Attachment read(ByteBuf buffer) {
            return AttachmentRemittance.read(buffer);
        }

        @Override
        public SelectionWidgetSupplier getSelectionWidgetSupplier() {
            return new RemittanceWidgetSupplier();
        }
    },
    PARCEL("parcel") {
        @Override
        public Attachment readFromNBT(NBTTagCompound tagCompound) {
            return AttachmentParcel.readFromNBT(tagCompound);
        }

        @Override
        public Attachment read(ByteBuf buffer) {
            return AttachmentParcel.read(buffer);
        }

        @Override
        public SelectionWidgetSupplier getSelectionWidgetSupplier() {
            return new ParcelWidgetSupplier();
        }
    },
    COD("cod") {
        @Override
        public Attachment readFromNBT(NBTTagCompound tagCompound) {
            return AttachmentCOD.readFromNBT(tagCompound);
        }

        @Override
        public Attachment read(ByteBuf buffer) {
            return AttachmentCOD.read(buffer);
        }

        @Override
        public SelectionWidgetSupplier getSelectionWidgetSupplier() {
            return new CODWidgetSupplier();
        }
    };

    private final String name;

    AttachmentType(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return MinecraftClient.localize("oxygen_main.attachment_type." + name);
    }

    public abstract Attachment readFromNBT(NBTTagCompound tagCompound);

    public abstract Attachment read(ByteBuf buffer);

    public abstract SelectionWidgetSupplier getSelectionWidgetSupplier();
}
