package austeretony.oxygen_mail.common.config;

import austeretony.oxygen_core.common.config.AbstractConfig;
import austeretony.oxygen_core.common.config.ConfigValue;
import austeretony.oxygen_core.common.config.ConfigValueUtils;
import austeretony.oxygen_mail.common.main.MailMain;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class MailConfig extends AbstractConfig {

    public static final ConfigValue
            ENABLE_MAIL_SCREEN_KEY = ConfigValueUtils.getBoolean("client", "enable_mail_screen_key", true),
            MAIL_SCREEN_KEY = ConfigValueUtils.getInt("client", "mail_screen_key", Keyboard.KEY_LBRACKET),

    ENABLE_MAIL_ACCESS_CLIENT_SIDE = ConfigValueUtils.getBoolean("server", "enable_mail_access_client_side", true, true),
            ENABLE_MAIL_SENDING = ConfigValueUtils.getBoolean("server", "enable_mail_sending", true, true),
            MAIL_SCREEN_OPERATIONS_TIMEOUT_MILLIS = ConfigValueUtils.getInt("server", "mail_screen_operations_timeout_millis", 5 * 60 * 1000),

    MAILBOX_SIZE = ConfigValueUtils.getInt("server", "mailbox_size", 50, true),
            MAIL_SENDING_COOL_DOWN_SECONDS = ConfigValueUtils.getInt("server", "mail_sending_cool_down_seconds", 0, true),
            LETTER_EXPIRE_TIME_HOURS = ConfigValueUtils.getInt("server", "letter_expire_time_hours", 240, true),
            REMITTANCE_EXPIRE_TIME_HOURS = ConfigValueUtils.getInt("server", "remittance_expire_time_hours", 24, true),
            PARCEL_EXPIRE_TIME_HOURS = ConfigValueUtils.getInt("server", "parcel_expire_time_hours", 24, true),
            COD_EXPIRE_TIME_HOURS = ConfigValueUtils.getInt("server", "cod_expire_time_hours", 1, true),
            SYSTEM_LETTER_EXPIRE_TIME_HOURS = ConfigValueUtils.getInt("server", "system_letter_expire_time_hours", -1, true),
            SYSTEM_REMITTANCE_EXPIRE_TIME_HOURS = ConfigValueUtils.getInt("server", "system_remittance_expire_time_hours", -1, true),
            SYSTEM_PARCEL_EXPIRE_TIME_HOURS = ConfigValueUtils.getInt("server", "system_parcel_expire_time_hours", -1, true),

    REMITTANCE_MAX_VALUE = ConfigValueUtils.getLong("server", "remittance_max_value", 100_000L, true),
            PARCEL_MAX_STACK_SIZE = ConfigValueUtils.getInt("server", "parcel_max_stack_size", -1, true),
            COD_MAX_VALUE = ConfigValueUtils.getLong("server", "cod_max_value", 100_000L, true),

    LETTER_POSTAGE_VALUE = ConfigValueUtils.getLong("server", "letter_postage_value", 0L, true),
            REMITTANCE_POSTAGE_PERCENT = ConfigValueUtils.getFloat("server", "remittance_postage_percent", 0.05F, true),
            PARCEL_POSTAGE_VALUE = ConfigValueUtils.getLong("server", "parcel_postage_value", 100L, true),
            COD_PRICE_FEE_PERCENT = ConfigValueUtils.getFloat("server", "cod_price_fee_percent", 0.05F, true);

    @Override
    public String getDomain() {
        return MailMain.MOD_ID;
    }

    @Override
    public String getVersion() {
        return MailMain.VERSION_CUSTOM;
    }

    @Override
    public String getFileName() {
        return "mail.json";
    }

    @Override
    public void getValues(List<ConfigValue> values) {
        values.add(ENABLE_MAIL_SCREEN_KEY);
        values.add(MAIL_SCREEN_KEY);

        values.add(ENABLE_MAIL_ACCESS_CLIENT_SIDE);
        values.add(ENABLE_MAIL_SENDING);
        values.add(MAIL_SCREEN_OPERATIONS_TIMEOUT_MILLIS);

        values.add(MAILBOX_SIZE);
        values.add(MAIL_SENDING_COOL_DOWN_SECONDS);

        values.add(LETTER_EXPIRE_TIME_HOURS);
        values.add(REMITTANCE_EXPIRE_TIME_HOURS);
        values.add(PARCEL_EXPIRE_TIME_HOURS);
        values.add(COD_EXPIRE_TIME_HOURS);
        values.add(SYSTEM_LETTER_EXPIRE_TIME_HOURS);
        values.add(SYSTEM_REMITTANCE_EXPIRE_TIME_HOURS);
        values.add(SYSTEM_PARCEL_EXPIRE_TIME_HOURS);

        values.add(REMITTANCE_MAX_VALUE);
        values.add(PARCEL_MAX_STACK_SIZE);
        values.add(COD_MAX_VALUE);

        values.add(LETTER_POSTAGE_VALUE);
        values.add(REMITTANCE_POSTAGE_PERCENT);
        values.add(PARCEL_POSTAGE_VALUE);
        values.add(COD_PRICE_FEE_PERCENT);
    }
}
