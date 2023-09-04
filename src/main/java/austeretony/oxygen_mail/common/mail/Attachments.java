package austeretony.oxygen_mail.common.mail;

import austeretony.oxygen_core.common.item.ItemStackWrapper;

public class Attachments {

    public static Attachment dummy() {
        return new AttachmentDummy();
    }

    public static Attachment remittance(int currencyIndex, long value) {
        return new AttachmentRemittance(currencyIndex, value);
    }

    public static Attachment parcel(ItemStackWrapper stackWrapper, int amount) {
        return new AttachmentParcel(stackWrapper, amount);
    }

    public static Attachment cod(ItemStackWrapper stackWrapper, int amount, int currencyIndex, long value) {
        return new AttachmentCOD(stackWrapper, amount, currencyIndex, value);
    }
}
