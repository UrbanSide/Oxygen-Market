package austeretony.oxygen_mail.common.mail;

import austeretony.oxygen_core.client.gui.base.core.Widget;
import austeretony.oxygen_core.common.util.objects.Pair;
import austeretony.oxygen_core.server.operation.Operation;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import java.util.UUID;

public interface Attachment {

    AttachmentType getType();

    boolean isValid(EntityPlayerMP playerMP);

    long getExpireTimeMillis(MailEntry mailEntry);

    Pair<Integer, Long> getPostage(EntityPlayerMP playerMP);

    Pair<Integer, Long> getBalance(EntityPlayerMP playerMP);

    void send(EntityPlayerMP playerMP, Operation operation);

    default void playSendSound(EntityPlayerMP playerMP) {}

    void receive(Operation operation, MailEntry mailEntry);

    default void playReceiveSound(EntityPlayerMP playerMP) {}

    boolean canBeReturned(MailEntry mailEntry);

    void returnToSender(UUID initiatorUUID, MailEntry mailEntry);

    void write(ByteBuf buffer);

    NBTTagCompound writeToNBT();

    //client

    boolean isValid();

    Pair<Integer, Long> getPostage();

    void draw(Widget widget, int mouseX, int mouseY);

    void drawForeground(Widget widget, int mouseX, int mouseY);
}
