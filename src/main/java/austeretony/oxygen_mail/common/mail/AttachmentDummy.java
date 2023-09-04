package austeretony.oxygen_mail.common.mail;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.UUID;

import austeretony.alternateui.screen.core.GUISimpleElement;
import austeretony.oxygen_core.client.api.PrivilegesProviderClient;
import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.sound.OxygenSoundEffects;
import austeretony.oxygen_core.server.api.CurrencyHelperServer;
import austeretony.oxygen_core.server.api.PrivilegesProviderServer;
import austeretony.oxygen_core.server.api.SoundEventHelperServer;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.main.EnumMailPrivilege;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AttachmentDummy implements Attachment {

    @Override
    public boolean send(EntityPlayerMP playerMP, Mail mail) {
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        long postage = PrivilegesProviderServer.getAsLong(playerUUID, EnumMailPrivilege.LETTER_POSTAGE_VALUE.id(), MailConfig.LETTER_POSTAGE_VALUE.asLong());
        boolean postageExist = postage > 0L;
        if (!postageExist || CurrencyHelperServer.enoughCurrency(playerUUID, postage, OxygenMain.COMMON_CURRENCY_INDEX)) {
            if (postageExist) {
                CurrencyHelperServer.removeCurrency(playerUUID, postage, OxygenMain.COMMON_CURRENCY_INDEX);
                SoundEventHelperServer.playSoundClient(playerMP, OxygenSoundEffects.RINGING_COINS.getId());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean receive(EntityPlayerMP playerMP, Mail mail) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public long getPostage() {
        return PrivilegesProviderClient.getAsLong(EnumMailPrivilege.LETTER_POSTAGE_VALUE.id(), MailConfig.LETTER_POSTAGE_VALUE.asLong());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canSend() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void sent() {}

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canReceive() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void received() {}

    @Override
    @SideOnly(Side.CLIENT)
    public void draw(GUISimpleElement widget, int mouseX, int mouseY) {}

    @Override
    public void write(BufferedOutputStream bos) throws IOException {}

    @Override
    public void write(ByteBuf buffer) {}

    @Override
    public Attachment toParcel() {
        return null;
    }

    @Override
    public ItemStack getItemStack() {
        return null;
    }

    @Override
    public String toString() {
        return "[dummy attachment]";
    }
}
