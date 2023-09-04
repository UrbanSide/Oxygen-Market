package austeretony.oxygen_mail.client.mail;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.api.PrivilegesClient;
import austeretony.oxygen_core.client.gui.base.common.WidgetGroup;
import austeretony.oxygen_core.client.gui.base.text.TextLabel;
import austeretony.oxygen_core.client.settings.CoreSettings;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.util.CommonUtils;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.mail.Attachment;
import austeretony.oxygen_mail.common.mail.Attachments;
import austeretony.oxygen_mail.common.main.MailPrivileges;

public class NoneAttachmentWidgetSupplier implements SelectionWidgetSupplier {

    private TextLabel postageLabel;

    @Override
    public WidgetGroup getWidgetGroup() {
        WidgetGroup widgetGroup = new WidgetGroup();

        postageLabel = createPostageWidget(OxygenMain.CURRENCY_COINS, 0, 4, widgetGroup);
        long postageValue = PrivilegesClient.getLong(MailPrivileges.LETTER_POSTAGE_VALUE.getId(), MailConfig.LETTER_POSTAGE_VALUE.asLong());
        postageLabel.getText().setColorEnabled(CoreSettings.COLOR_TEXT_BASE_ENABLED.asInt());
        long balance = OxygenClient.getWatcherValue(OxygenMain.CURRENCY_COINS, 0L);
        if (balance < postageValue) {
            postageLabel.getText().setColorEnabled(CoreSettings.COLOR_TEXT_INACTIVE.asInt());
        }
        postageLabel.getText().setText(CommonUtils.formatCurrencyValue(postageValue));

        return widgetGroup;
    }

    @Override
    public Attachment createAttachment() {
        return Attachments.none();
    }
}
