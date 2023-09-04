package austeretony.oxygen_mail.client.gui.mail.sending.callback;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.gui.base.Keys;
import austeretony.oxygen_core.client.gui.base.Texts;
import austeretony.oxygen_core.client.gui.base.background.Background;
import austeretony.oxygen_core.client.gui.base.block.Texture;
import austeretony.oxygen_core.client.gui.base.common.ImageLabel;
import austeretony.oxygen_core.client.gui.base.core.Callback;
import austeretony.oxygen_core.client.gui.base.special.KeyButton;
import austeretony.oxygen_core.client.gui.base.text.TextLabel;
import austeretony.oxygen_core.client.preset.CurrencyProperties;
import austeretony.oxygen_core.client.settings.CoreSettings;
import austeretony.oxygen_core.common.util.CommonUtils;
import austeretony.oxygen_core.common.util.objects.Pair;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.client.gui.mail.AttachmentWidget;
import austeretony.oxygen_mail.common.mail.Attachment;
import austeretony.oxygen_mail.common.mail.AttachmentType;

import java.util.UUID;

public class SendMailCallback extends Callback {

    private final UUID addresseeUUID;
    private final String addresseeUsername;
    private final String subject;
    private final String message;
    private final AttachmentType attachmentType;
    private final Attachment attachment;

    public SendMailCallback(UUID addresseeUUID, String addresseeUsername, String subject, String message,
                            AttachmentType attachmentType, Attachment attachment) {
        super(160, 116);
        this.addresseeUUID = addresseeUUID;
        this.addresseeUsername = addresseeUsername;
        this.subject = subject;
        this.message = message;
        this.attachmentType = attachmentType;
        this.attachment = attachment;
    }

    @Override
    public void init() {
        addWidget(new Background.UnderlinedTitleBottom(this));
        addWidget(new TextLabel(4, 12, Texts.title("oxygen_mail.gui.mail.sending.callback.send_mail")));

        addWidget(new TextLabel(6, 23, Texts.additionalDark("oxygen_mail.gui.mail.sending.label.addressee")));
        addWidget(new TextLabel(6, 31, Texts.additional(addresseeUsername)));

        addWidget(new TextLabel(6, 40, Texts.additionalDark("oxygen_mail.gui.mail.sending.label.subject")));
        addWidget(new TextLabel(6, 48, Texts.additional(subject)));

        addWidget(new TextLabel(6, 60, Texts.additional(attachmentType.getDisplayName())));
        AttachmentWidget attachmentWidget;
        addWidget(attachmentWidget = new AttachmentWidget(6, 62));
        attachmentWidget.setAttachment(attachment);

        Pair<Integer, Long> postage = attachment.getPostage();
        long balance = OxygenClient.getWatcherValue(postage.getKey(), 0L);

        CurrencyProperties properties = OxygenClient.getCurrencyProperties(postage.getKey());
        Texture texture = Texture.builder()
                .imageSize(properties.getIconWidth(), properties.getIconHeight())
                .size(properties.getIconWidth(), properties.getIconHeight())
                .texture(properties.getIconTexture())
                .build();

        int postageY = 90;
        addWidget(new TextLabel(6, postageY, Texts.additionalDark("oxygen_mail.gui.mail.label.postage")));
        addWidget(new ImageLabel(6, postageY + 2 + properties.getIconYOffset(), texture));
        TextLabel textLabel = new TextLabel(6 + properties.getIconWidth() + 2 * properties.getIconXOffset() + 2, postageY + 9,
                Texts.additional(CommonUtils.formatCurrencyValue(postage.getValue())));
        if (postage.getValue() > balance) {
            textLabel.getText().setColorEnabled(CoreSettings.COLOR_TEXT_INACTIVE.asInt());
        }
        addWidget(textLabel);

        int buttonPosSegment = (int) (getWidth() / 2F);
        KeyButton confirmButton;
        addWidget(confirmButton = new KeyButton(0, getHeight() - 10, Keys.CONFIRM_KEY, "oxygen_core.gui.button.confirm")
                .setPressListener(this::confirm)
                .setEnabled(attachment.isValid() && postage.getValue() <= balance));
        confirmButton.setX(getX() + (int) ((buttonPosSegment - confirmButton.getText().getWidth()) / 2F));
        KeyButton cancelButton;
        addWidget(cancelButton = new KeyButton(0, getHeight() - 10, Keys.CANCEL_KEY, "oxygen_core.gui.button.cancel")
                .setPressListener(this::close));
        cancelButton.setX(getX() + buttonPosSegment + (int) ((buttonPosSegment - cancelButton.getText().getWidth()) / 2F));
    }

    private void confirm() {
        MailManagerClient.instance().sendMail(addresseeUUID, subject, message, attachmentType, attachment);
        close();
    }
}
