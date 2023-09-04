package austeretony.oxygen_mail.common.mail;

import austeretony.oxygen_core.common.item.ItemStackWrapper;

import java.util.Collections;
import java.util.Map;

public class Attachments {

    public static Attachment none() {
        return new AttachmentNone();
    }

    public static Attachment remittance(int currencyIndex, long value) {
        return new AttachmentRemittance(currencyIndex, value);
    }

    public static Attachment parcel(Map<ItemStackWrapper, Integer> itemsMap) {
        return new AttachmentParcel(itemsMap);
    }

    public static Attachment parcel(ItemStackWrapper stackWrapper, int quantity) {
        return new AttachmentParcel(Collections.singletonMap(stackWrapper, quantity));
    }

    public static Attachment cod(int currencyIndex, long value, Map<ItemStackWrapper, Integer> itemsMap) {
        return new AttachmentCOD(currencyIndex, value, itemsMap);
    }
}
