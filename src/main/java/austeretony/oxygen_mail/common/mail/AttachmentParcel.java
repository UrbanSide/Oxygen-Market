package austeretony.oxygen_mail.common.mail;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.UUID;

import austeretony.alternateui.screen.core.GUISimpleElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.InventoryProviderClient;
import austeretony.oxygen_core.client.api.PrivilegesProviderClient;
import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.sound.OxygenSoundEffects;
import austeretony.oxygen_core.common.util.StreamUtils;
import austeretony.oxygen_core.server.api.CurrencyHelperServer;
import austeretony.oxygen_core.server.api.InventoryProviderServer;
import austeretony.oxygen_core.server.api.PrivilegesProviderServer;
import austeretony.oxygen_core.server.api.SoundEventHelperServer;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.main.EnumMailPrivilege;
import austeretony.oxygen_mail.common.main.EnumMailStatusMessage;
import austeretony.oxygen_mail.server.MailManagerServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AttachmentParcel implements Attachment {

    public final ItemStackWrapper stackWrapper;

    public final int amount;

    public AttachmentParcel(ItemStackWrapper stackWrapper, int amount) {
        this.stackWrapper = stackWrapper;
        this.amount = amount;
    }

    @Override
    public boolean send(EntityPlayerMP playerMP, Mail mail) {
        if (this.stackWrapper == null || (this.stackWrapper.getItemId() == Item.getIdFromItem(Items.AIR) || this.amount <= 0)) {
            MailManagerServer.instance().sendStatusMessages(playerMP, EnumMailStatusMessage.PARCEL_DAMAGED);
            return false;
        }
        if (MailManagerServer.instance().getItemsBlackList().isBlackListed(Item.getItemById(this.stackWrapper.getItemId()))) {
            MailManagerServer.instance().sendStatusMessages(playerMP, EnumMailStatusMessage.ITEM_BLACKLISTED);
            return false;
        }

        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        int maxAmount = PrivilegesProviderServer.getAsInt(playerUUID, EnumMailPrivilege.PARCEL_MAX_AMOUNT.id(), MailConfig.PARCEL_MAX_AMOUNT.asInt());
        final ItemStack itemStack = this.stackWrapper.getItemStack();
        if (maxAmount < 0) 
            maxAmount = itemStack.getMaxStackSize();
        if (this.amount <= maxAmount) {
            long postage = PrivilegesProviderServer.getAsLong(playerUUID, EnumMailPrivilege.PARCEL_POSTAGE_VALUE.id(), MailConfig.PARCEL_POSTAGE_VALUE.asLong());
            boolean postageExist = postage > 0;
            if (InventoryProviderServer.getPlayerInventory().getEqualItemAmount(playerMP, this.stackWrapper) >= this.amount
                    && (!postageExist || CurrencyHelperServer.enoughCurrency(playerUUID, postage, OxygenMain.COMMON_CURRENCY_INDEX))) {
                final int amount = this.amount;
                InventoryProviderServer.getPlayerInventory().removeItem(playerMP, this.stackWrapper, amount);

                if (postageExist) {
                    CurrencyHelperServer.removeCurrency(playerUUID, postage, OxygenMain.COMMON_CURRENCY_INDEX);
                    SoundEventHelperServer.playSoundClient(playerMP, OxygenSoundEffects.RINGING_COINS.getId());
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean receive(EntityPlayerMP playerMP, Mail mail) {
        if (InventoryProviderServer.getPlayerInventory().haveEnoughSpace(playerMP,  this.stackWrapper, this.amount)) {
            InventoryProviderServer.getPlayerInventory().addItem(playerMP, this.stackWrapper, this.amount);       

            SoundEventHelperServer.playSoundClient(playerMP, OxygenSoundEffects.INVENTORY_OPERATION.getId());
            return true;
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public long getPostage() {
        return PrivilegesProviderClient.getAsLong(EnumMailPrivilege.PARCEL_POSTAGE_VALUE.id(), MailConfig.PARCEL_POSTAGE_VALUE.asLong());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canSend() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void sent() {
        MailManagerClient.instance().getMenuManager().removeItemStack(this.stackWrapper, this.amount);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canReceive() {
        return InventoryProviderClient.getPlayerInventory().haveEnoughSpace(ClientReference.getClientPlayer(), this.stackWrapper, this.amount);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void received() {
        MailManagerClient.instance().getMenuManager().addItemStack(this.stackWrapper, this.amount);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void draw(GUISimpleElement widget, int mouseX, int mouseY) {
        Minecraft mc = ClientReference.getMinecraft();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        RenderHelper.enableGUIStandardItemLighting();            
        GlStateManager.enableDepth();
        mc.getRenderItem().renderItemAndEffectIntoGUI(this.stackWrapper.getCachedItemStack(), widget.getX(), widget.getY());   

        if (widget.isDebugMode()) {
            FontRenderer font = this.stackWrapper.getCachedItemStack().getItem().getFontRenderer(this.stackWrapper.getCachedItemStack());
            if (font == null) 
                font = mc.fontRenderer;
            mc.getRenderItem().renderItemOverlayIntoGUI(font, this.stackWrapper.getCachedItemStack(), widget.getX(), widget.getY(), null);
        }

        GlStateManager.disableDepth();
        RenderHelper.disableStandardItemLighting();

        GlStateManager.pushMatrix();           
        GlStateManager.translate(widget.getX(), widget.getY(), 0.0F);   

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.pushMatrix();           
        GlStateManager.translate(14.0F, 10.0F, 0.0F);            
        GlStateManager.scale(widget.getTextScale(), widget.getTextScale(), 0.0F);   
        mc.fontRenderer.drawString(String.valueOf(this.amount), 0, 0, widget.getEnabledTextColor(), false); 
        GlStateManager.popMatrix();  

        GlStateManager.popMatrix();
    }

    @Override
    public void write(BufferedOutputStream bos) throws IOException {
        this.stackWrapper.write(bos);
        StreamUtils.write((short) this.amount, bos);
    }

    public static Attachment read(BufferedInputStream bis) throws IOException {
        return new AttachmentParcel(ItemStackWrapper.read(bis), StreamUtils.readShort(bis));
    }

    @Override
    public void write(ByteBuf buffer) {
        this.stackWrapper.write(buffer);
        buffer.writeShort(this.amount);
    }

    public static Attachment read(ByteBuf buffer) {
        return new AttachmentParcel(ItemStackWrapper.read(buffer), buffer.readShort());
    }

    @Override
    public Attachment toParcel() {
        return Attachments.parcel(this.stackWrapper, this.amount);
    }

    @Override
    public ItemStack getItemStack() {
        return this.stackWrapper.getCachedItemStack();
    }

    @Override
    public String toString() {
        return String.format("[stack wrapper: %s, amount: %d]",
                this.stackWrapper, 
                this.amount);
    }
}
