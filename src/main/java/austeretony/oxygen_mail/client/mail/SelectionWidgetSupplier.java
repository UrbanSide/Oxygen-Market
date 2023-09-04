package austeretony.oxygen_mail.client.mail;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.gui.base.Texts;
import austeretony.oxygen_core.client.gui.base.block.Texture;
import austeretony.oxygen_core.client.gui.base.common.ImageLabel;
import austeretony.oxygen_core.client.gui.base.common.WidgetGroup;
import austeretony.oxygen_core.client.gui.base.text.TextLabel;
import austeretony.oxygen_core.client.preset.CurrencyProperties;
import austeretony.oxygen_mail.common.mail.Attachment;

public interface SelectionWidgetSupplier {

    WidgetGroup getWidgetGroup();

    Attachment createAttachment();

    default TextLabel createPostageWidget(int postageCurrencyIndex, int x, int y, WidgetGroup widgetGroup) {
        CurrencyProperties properties = OxygenClient.getCurrencyProperties(postageCurrencyIndex);
        Texture texture = Texture.builder()
                .imageSize(properties.getIconWidth(), properties.getIconHeight())
                .size(properties.getIconWidth(), properties.getIconHeight())
                .texture(properties.getIconTexture())
                .build();

        widgetGroup.addWidget(new TextLabel(x, y, Texts.additionalDark("oxygen_mail.gui.mail.label.postage")));
        widgetGroup.addWidget(new ImageLabel(x, y + 2 + properties.getIconYOffset(), texture));
        TextLabel textLabel = new TextLabel(x + properties.getIconWidth() + 2 * properties.getIconXOffset() + 2, y + 9,
                Texts.additional(String.valueOf(0)));
        widgetGroup.addWidget(textLabel);
        return textLabel;
    }
}
