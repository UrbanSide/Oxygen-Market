package austeretony.oxygen_mail.common.main;

import austeretony.oxygen_core.common.privileges.PrivilegeRegistry;
import austeretony.oxygen_core.common.util.value.ValueType;

public final class MailPrivileges {

    public static final PrivilegeRegistry.Entry
    MAILBOX_SIZE = PrivilegeRegistry.register(800, "mail:mailbox_size", ValueType.INTEGER),
    ALLOW_MAIL_SENDING = PrivilegeRegistry.register(801, "mail:allow_mail_sending", ValueType.BOOLEAN),

    REMITTANCE_MAX_VALUE = PrivilegeRegistry.register(820, "mail:remittance_max_value", ValueType.LONG),
    PARCEL_MAX_STACK_SIZE = PrivilegeRegistry.register(821, "mail:parcel_max_stack_size", ValueType.INTEGER),
    COD_MAX_VALUE = PrivilegeRegistry.register(822, "mail:cod_max_value", ValueType.LONG),

    LETTER_POSTAGE_VALUE = PrivilegeRegistry.register(830, "mail:letter_postage_value", ValueType.LONG),
    REMITTANCE_POSTAGE_PERCENT = PrivilegeRegistry.register(831, "mail:remittance_postage_percent", ValueType.FLOAT),
    PARCEL_POSTAGE_VALUE = PrivilegeRegistry.register(832, "mail:parcel_postage_value", ValueType.LONG),
    COD_PRICE_FEE_PERCENT = PrivilegeRegistry.register(833, "mail:cod_price_fee_percent", ValueType.FLOAT);

    private MailPrivileges() {}

    public static void register() {}
}
