package austeretony.oxygen_mail.client;

import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_mail.client.gui.mail.MailMenuScreen;
import austeretony.oxygen_mail.common.mail.Attachment;
import austeretony.oxygen_mail.common.mail.EnumMail;
import austeretony.oxygen_mail.common.mail.Mail;

public class MailMenuManager {

    public static void openMailMenu() {
        ClientReference.displayGuiScreen(new MailMenuScreen());
    }

    public static void openMailMenuDelegated() {
        ClientReference.delegateToClientThread(MailMenuManager::openMailMenu);
    } 

    public void sharedDataSynchronized() {
        ClientReference.delegateToClientThread(()->{
            if (isMenuOpened())
                ((MailMenuScreen) ClientReference.getCurrentScreen()).sharedDataSynchronized();
        });
    }

    public void mailSynchronized() {
        ClientReference.delegateToClientThread(()->{
            if (isMenuOpened())
                ((MailMenuScreen) ClientReference.getCurrentScreen()).mailSynchronized();
        });
    }

    public void mailSent(EnumMail type, Attachment attachment, long balance) {
        ClientReference.delegateToClientThread(()->{
            if (isMenuOpened())
                ((MailMenuScreen) ClientReference.getCurrentScreen()).mailSent(type, attachment, balance);
        });
    }

    public void removeItemStack(ItemStackWrapper stackWrapper, int amount) {
        ClientReference.delegateToClientThread(()->{
            if (isMenuOpened())
                ((MailMenuScreen) ClientReference.getCurrentScreen()).removeItemStack(stackWrapper, amount);
        });
    }

    public void addItemStack(ItemStackWrapper stackWrapper, int amount) {
        ClientReference.delegateToClientThread(()->{
            if (isMenuOpened())
                ((MailMenuScreen) ClientReference.getCurrentScreen()).addItemStack(stackWrapper, amount);
        });
    }

    public void messageRemoved(long messageId) {
        ClientReference.delegateToClientThread(()->{
            if (isMenuOpened())
                ((MailMenuScreen) ClientReference.getCurrentScreen()).messageRemoved(messageId);
        });
    }

    public void attachmentReceived(long oldMessageId, Mail mail, long balance) {
        ClientReference.delegateToClientThread(()->{
            if (isMenuOpened())
                ((MailMenuScreen) ClientReference.getCurrentScreen()).attachmentReceived(oldMessageId, mail, balance);
        });
    }

    public static boolean isMenuOpened() {
        return ClientReference.hasActiveGUI() && ClientReference.getCurrentScreen() instanceof MailMenuScreen;
    }
}
