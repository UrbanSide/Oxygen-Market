package austeretony.oxygen_mail.common.config;

import java.util.List;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.api.config.AbstractConfig;
import austeretony.oxygen_core.common.config.ConfigValue;
import austeretony.oxygen_core.common.config.ConfigValueUtils;
import austeretony.oxygen_mail.common.main.MailMain;

public class MailConfig extends AbstractConfig {

    public static final ConfigValue
    ENABLE_MAIL_MENU_KEY = ConfigValueUtils.getValue("client", "enable_mail_menu_key", true),
    MAIL_MENU_KEY = ConfigValueUtils.getValue("client", "mail_menu_key", 26),

    ENABLE_MAIL_ACCESS_CLIENTSIDE = ConfigValueUtils.getValue("server", "enable_mail_menu_access_clientside", true, true),
    MAIL_MENU_OPERATIONS_TIMEOUT_MILLIS = ConfigValueUtils.getValue("server", "mail_menu_operations_timeout_millis", 240000),
    
    ALLOW_MAIL_SENDING = ConfigValueUtils.getValue("server", "allow_mail_sending", true, true),
    MAILBOX_SIZE = ConfigValueUtils.getValue("server", "mailbox_size", 30, true),

    LETTER_EXPIRE_TIME_HOURS = ConfigValueUtils.getValue("server", "letter_expire_time_hours", 240, true),
    REMITTANCE_EXPIRE_TIME_HOURS = ConfigValueUtils.getValue("server", "remittance_expire_time_hours", 24, true),
    PARCEL_EXPIRE_TIME_HOURS = ConfigValueUtils.getValue("server", "parcel_expire_time_hours", 24, true),
    COD_EXPIRE_TIME_HOURS = ConfigValueUtils.getValue("server", "cod_expire_time_hours", 1, true),
    SYSTEM_LETTER_EXPIRE_TIME_HOURS = ConfigValueUtils.getValue("server", "system_letter_expire_time_hours", - 1, true),
    SYSTEM_REMITTANCE_EXPIRE_TIME_HOURS = ConfigValueUtils.getValue("server", "system_remittance_expire_time_hours", - 1, true),
    SYSTEM_PARCEL_EXPIRE_TIME_HOURS = ConfigValueUtils.getValue("server", "system_parcel_expire_time_hours", - 1, true),

    MAIL_SENDING_COOLDOWN_SECONDS = ConfigValueUtils.getValue("server", "mail_sending_cooldown_seconds", 30),
    REMITTANCE_MAX_VALUE = ConfigValueUtils.getValue("server", "remittance_max_value", 100000L, true),
    PARCEL_MAX_AMOUNT = ConfigValueUtils.getValue("server", "parcel_max_amount", - 1, true),
    COD_MAX_VALUE = ConfigValueUtils.getValue("server", "cod_max_value", 50000L, true),

    LETTER_POSTAGE_VALUE = ConfigValueUtils.getValue("server", "letter_postage_value", 0L, true),
    REMITTANCE_POSTAGE_PERCENT = ConfigValueUtils.getValue("server", "remittance_postage_percent", 5, true),
    PARCEL_POSTAGE_VALUE = ConfigValueUtils.getValue("server", "parcel_postage_value", 100L, true),
    COD_POSTAGE_PERCENT = ConfigValueUtils.getValue("server", "cod_postage_percent", 5, true),

    ADVANCED_LOGGING = ConfigValueUtils.getValue("server", "advanced_logging", false);

    @Override
    public String getDomain() {
        return MailMain.MODID;
    }

    @Override
    public String getVersion() {
        return MailMain.VERSION_CUSTOM;
    }

    @Override
    public String getExternalPath() {
        return CommonReference.getGameFolder() + "/config/oxygen/mail.json";
    }

    @Override
    public void getValues(List<ConfigValue> values) {
        values.add(ENABLE_MAIL_MENU_KEY);
        values.add(MAIL_MENU_KEY);
        
        values.add(ENABLE_MAIL_ACCESS_CLIENTSIDE);
        values.add(MAIL_MENU_OPERATIONS_TIMEOUT_MILLIS);

        values.add(ALLOW_MAIL_SENDING);
        values.add(MAILBOX_SIZE);

        values.add(LETTER_EXPIRE_TIME_HOURS);
        values.add(REMITTANCE_EXPIRE_TIME_HOURS);
        values.add(PARCEL_EXPIRE_TIME_HOURS);
        values.add(COD_EXPIRE_TIME_HOURS);
        values.add(SYSTEM_LETTER_EXPIRE_TIME_HOURS);
        values.add(SYSTEM_REMITTANCE_EXPIRE_TIME_HOURS);
        values.add(SYSTEM_PARCEL_EXPIRE_TIME_HOURS);

        values.add(MAIL_SENDING_COOLDOWN_SECONDS);
        values.add(REMITTANCE_MAX_VALUE);
        values.add(PARCEL_MAX_AMOUNT);
        values.add(COD_MAX_VALUE);

        values.add(REMITTANCE_POSTAGE_PERCENT);
        values.add(PARCEL_POSTAGE_VALUE);
        values.add(COD_POSTAGE_PERCENT);

        values.add(ADVANCED_LOGGING);
    }
}
