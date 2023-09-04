package austeretony.oxygen_mail.common.mail;

import java.io.BufferedOutputStream;
import java.io.IOException;

import javax.annotation.Nullable;

import austeretony.alternateui.screen.core.GUISimpleElement;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface Attachment {

    boolean send(EntityPlayerMP playerMP, @Nullable Mail mail);

    boolean receive(EntityPlayerMP playerMP, Mail mail);

    @SideOnly(Side.CLIENT)
    long getPostage();

    @SideOnly(Side.CLIENT)
    boolean canSend();

    @SideOnly(Side.CLIENT)
    void sent();

    @SideOnly(Side.CLIENT)
    boolean canReceive();

    @SideOnly(Side.CLIENT)
    void received();

    @SideOnly(Side.CLIENT)
    void draw(GUISimpleElement widget, int mouseX, int mouseY);

    void write(BufferedOutputStream bos) throws IOException;

    void write(ByteBuf buffer);

    @Nullable
    Attachment toParcel();

    @Nullable
    ItemStack getItemStack();
}
