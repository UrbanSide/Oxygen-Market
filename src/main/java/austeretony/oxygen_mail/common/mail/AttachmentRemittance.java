package austeretony.oxygen_mail.common.mail;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.UUID;

import austeretony.alternateui.screen.core.GUIAdvancedElement;
import austeretony.alternateui.screen.core.GUISimpleElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.api.PrivilegesProviderClient;
import austeretony.oxygen_core.client.api.WatcherHelperClient;
import austeretony.oxygen_core.client.currency.CurrencyProperties;
import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.sound.OxygenSoundEffects;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_core.common.util.StreamUtils;
import austeretony.oxygen_core.server.api.CurrencyHelperServer;
import austeretony.oxygen_core.server.api.PrivilegesProviderServer;
import austeretony.oxygen_core.server.api.SoundEventHelperServer;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.main.EnumMailPrivilege;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AttachmentRemittance implements Attachment {

    private final int currencyIndex;

    private final long value;

    public AttachmentRemittance(int currencyIndex, long value) {
        this.currencyIndex = currencyIndex;
        this.value = value;
    }

    @Override
    public boolean send(EntityPlayerMP playerMP, Mail mail) {
        if (this.value <= 0L || CurrencyHelperServer.getCurrencyProvider(this.currencyIndex) == null)
            return false;

        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        if (this.value <= PrivilegesProviderServer.getAsLong(playerUUID, EnumMailPrivilege.REMITTANCE_MAX_VALUE.id(), MailConfig.REMITTANCE_MAX_VALUE.asLong())) {
            long postage = MathUtils.percentValueOf(
                    this.value, 
                    PrivilegesProviderServer.getAsInt(playerUUID, EnumMailPrivilege.REMITTANCE_POSTAGE_PERCENT.id(), MailConfig.REMITTANCE_POSTAGE_PERCENT.asInt()));
            if (CurrencyHelperServer.enoughCurrency(playerUUID, this.value + postage, this.currencyIndex)) {
                CurrencyHelperServer.removeCurrency(playerUUID, this.value + postage, this.currencyIndex);
                SoundEventHelperServer.playSoundClient(playerMP, OxygenSoundEffects.RINGING_COINS.getId());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean receive(EntityPlayerMP playerMP, Mail mail) {
        CurrencyHelperServer.addCurrency(CommonReference.getPersistentUUID(playerMP), this.value, this.currencyIndex);
        SoundEventHelperServer.playSoundClient(playerMP, OxygenSoundEffects.RINGING_COINS.getId());
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public long getPostage() {
        return MathUtils.percentValueOf(this.value, PrivilegesProviderClient.getAsInt(EnumMailPrivilege.REMITTANCE_POSTAGE_PERCENT.id(), MailConfig.REMITTANCE_POSTAGE_PERCENT.asInt()));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canSend() {
        return WatcherHelperClient.getLong(this.currencyIndex) >= this.value;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void sent() {}

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canReceive() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void received() {}

    @Override
    @SideOnly(Side.CLIENT)
    public void draw(GUISimpleElement widget, int mouseX, int mouseY) {
        Minecraft mc = ClientReference.getMinecraft();

        CurrencyProperties currencyProperties = OxygenHelperClient.getCurrencyProperties(this.currencyIndex);

        GlStateManager.pushMatrix();           
        GlStateManager.translate(widget.getX(), widget.getY(), 0.0F);   

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.enableBlend(); 
        mc.getTextureManager().bindTexture(currencyProperties.getIcon());
        GUIAdvancedElement.drawCustomSizedTexturedRect(
                currencyProperties.getXOffset(), 
                (widget.getHeight() - currencyProperties.getIconHeight()) / 2 + currencyProperties.getYOffset(), 
                0, 
                0, 
                currencyProperties.getIconWidth(), 
                currencyProperties.getIconHeight(), 
                currencyProperties.getIconWidth(), 
                currencyProperties.getIconHeight());                 
        GlStateManager.disableBlend();  

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.pushMatrix();           
        GlStateManager.translate(10.0F, ((float) widget.getHeight() - widget.textHeight(widget.getTextScale())) / 2.0F, 0.0F);            
        GlStateManager.scale(widget.getTextScale(), widget.getTextScale(), 0.0F);   
        mc.fontRenderer.drawString(String.valueOf(this.value), 0, 0, widget.getEnabledTextColor(), false); 
        GlStateManager.popMatrix();   

        GlStateManager.popMatrix();
    }

    @Override
    public void write(BufferedOutputStream bos) throws IOException {
        StreamUtils.write((byte) this.currencyIndex, bos);
        StreamUtils.write(this.value, bos);
    }

    public static Attachment read(BufferedInputStream bis) throws IOException {
        return new AttachmentRemittance(StreamUtils.readByte(bis), StreamUtils.readLong(bis));
    }

    @Override
    public void write(ByteBuf buffer) {
        buffer.writeByte(this.currencyIndex);
        buffer.writeLong(this.value);
    }

    public static Attachment read(ByteBuf buffer) {
        return new AttachmentRemittance(buffer.readByte(), buffer.readLong());
    }

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
        return String.format("[currency index: %d, value: %d]",
                this.currencyIndex, 
                this.value);
    }
}
