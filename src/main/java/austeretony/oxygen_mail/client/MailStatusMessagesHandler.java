package austeretony.oxygen_mail.client;

import austeretony.oxygen_core.common.chat.ChatMessagesHandler;
import austeretony.oxygen_mail.common.main.EnumMailStatusMessage;
import austeretony.oxygen_mail.common.main.MailMain;

public class MailStatusMessagesHandler implements ChatMessagesHandler {

    @Override
    public int getModIndex() {
        return MailMain.MAIL_MOD_INDEX;
    }

    @Override
    public String getMessage(int messageIndex) {
        return EnumMailStatusMessage.values()[messageIndex].localizedName();
    }
}
