package austeretony.oxygen_mail.client.mail;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.api.PrivilegesClient;
import austeretony.oxygen_core.client.gui.base.block.Texture;
import austeretony.oxygen_core.client.gui.base.common.ImageLabel;
import austeretony.oxygen_core.client.gui.base.common.WidgetGroup;
import austeretony.oxygen_core.client.gui.base.text.NumberField;
import austeretony.oxygen_core.client.gui.base.text.TextLabel;
import austeretony.oxygen_core.client.preset.CurrencyProperties;
import austeretony.oxygen_core.client.settings.CoreSettings;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.util.CommonUtils;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.mail.Attachment;
import austeretony.oxygen_mail.common.mail.Attachments;
import austeretony.oxygen_mail.common.main.MailPrivileges;

public class RemittanceWidgetSupplier implements SelectionWidgetSupplier {

    private NumberField numberField;
    private TextLabel postageLabel;

    @Override
    public WidgetGroup getWidgetGroup() {
        WidgetGroup widgetGroup = new WidgetGroup();

        CurrencyProperties properties = OxygenClient.getCurrencyProperties(OxygenMain.CURRENCY_COINS);
        Texture texture = Texture.builder()
                .imageSize(properties.getIconWidth(), properties.getIconHeight())
                .size(properties.getIconWidth(), properties.getIconHeight())
                .texture(properties.getIconTexture())
                .build();
        widgetGroup.addWidget(new ImageLabel(0, properties.getIconYOffset(), texture));

        long maxValue = OxygenClient.getWatcherValue(OxygenMain.CURRENCY_COINS, 0L);
        widgetGroup.addWidget(numberField = new NumberField(properties.getIconWidth() + 2 * properties.getIconXOffset() + 2, 0, 30, 0L, maxValue)
                .setKeyPressListener((keyCode, keyChar) -> updatePostage()));
        numberField.setText(String.valueOf(0));

        postageLabel = createPostageWidget(OxygenMain.CURRENCY_COINS, 0, 17, widgetGroup);

        return widgetGroup;
    }

    private void updatePostage() {
        long remittanceValue = numberField.getTypedNumberAsLong();
        float postagePercent = PrivilegesClient.getFloat(MailPrivileges.REMITTANCE_POSTAGE_PERCENT.getId(),
                MailConfig.REMITTANCE_POSTAGE_PERCENT.asFloat());
        long postageValue = (long) (remittanceValue * postagePercent);

        postageLabel.getText().setColorEnabled(CoreSettings.COLOR_TEXT_BASE_ENABLED.asInt());
        long balance = OxygenClient.getWatcherValue(OxygenMain.CURRENCY_COINS, 0L);
        if (balance < remittanceValue + postageValue) {
            postageLabel.getText().setColorEnabled(CoreSettings.COLOR_TEXT_INACTIVE.asInt());
        }

        postageLabel.getText().setText(CommonUtils.formatCurrencyValue(postageValue));
    }

    @Override
    public Attachment createAttachment() {
        return Attachments.remittance(OxygenMain.CURRENCY_COINS, numberField.getTypedNumberAsLong());
    }
}
