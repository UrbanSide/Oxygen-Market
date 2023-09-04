package austeretony.oxygen_mail.common.mail;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.UUID;

import austeretony.alternateui.screen.core.GUIAdvancedElement;
import austeretony.alternateui.screen.core.GUISimpleElement;
import austeretony.oxygen_core.client.api.ClientReference;
import austeretony.oxygen_core.client.api.InventoryProviderClient;
import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.client.api.PrivilegesProviderClient;
import austeretony.oxygen_core.client.api.WatcherHelperClient;
import austeretony.oxygen_core.client.currency.CurrencyProperties;
import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.main.EnumOxygenStatusMessage;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.sound.OxygenSoundEffects;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_core.common.util.StreamUtils;
import austeretony.oxygen_core.server.OxygenManagerServer;
import austeretony.oxygen_core.server.api.CurrencyHelperServer;
import austeretony.oxygen_core.server.api.InventoryProviderServer;
import austeretony.oxygen_core.server.api.PrivilegesProviderServer;
import austeretony.oxygen_core.server.api.SoundEventHelperServer;
import austeretony.oxygen_mail.client.MailManagerClient;
import austeretony.oxygen_mail.common.config.MailConfig;
import austeretony.oxygen_mail.common.main.EnumMailPrivilege;
import austeretony.oxygen_mail.common.main.EnumMailStatusMessage;
import austeretony.oxygen_mail.server.MailManagerServer;
import austeretony.oxygen_mail.server.api.MailHelperServer;
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

public class AttachmentCOD implements Attachment {

    public final ItemStackWrapper stackWrapper;

    public final int amount, currencyIndex;

    private final long value;

    public AttachmentCOD(ItemStackWrapper stackWrapper, int itemAmount, int currencyIndex, long value) {
        this.stackWrapper = stackWrapper;
        this.amount = itemAmount;
        this.currencyIndex = currencyIndex;
        this.value = value;
    }

    @Override
    public boolean send(EntityPlayerMP playerMP, Mail mail) {
        if (this.value <= 0L || CurrencyHelperServer.getCurrencyProvider(this.currencyIndex) == null)
            return false;

        if (this.stackWrapper == null || (this.stackWrapper.getItemId() == Item.getIdFromItem(Items.AIR) || this.amount <= 0)) {
            MailManagerServer.instance().sendStatusMessages(playerMP, EnumMailStatusMessage.PARCEL_DAMAGED);
            return false;
        }
        if (MailManagerServer.instance().getItemsBlackList().isBlackListed(Item.getItemById(this.stackWrapper.getItemId()))) {
            MailManagerServer.instance().sendStatusMessages(playerMP, EnumMailStatusMessage.ITEM_BLACKLISTED);
            return false;
        }

        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        if (this.value <= PrivilegesProviderServer.getAsLong(playerUUID, EnumMailPrivilege.COD_MAX_VALUE.id(), MailConfig.COD_MAX_VALUE.asLong())) {
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
        }
        return false;
    }

    @Override
    public boolean receive(EntityPlayerMP playerMP, Mail mail) {
        if (!InventoryProviderServer.getPlayerInventory().haveEnoughSpace(playerMP, this.stackWrapper, this.amount)) {
            OxygenManagerServer.instance().sendStatusMessage(playerMP, EnumOxygenStatusMessage.INVENTORY_FULL);
            return false;
        }
        UUID playerUUID = CommonReference.getPersistentUUID(playerMP);
        if (CurrencyHelperServer.enoughCurrency(playerUUID, this.value, this.currencyIndex)) {
            CurrencyHelperServer.removeCurrency(playerUUID, this.value, this.currencyIndex);
            long codPostage = MathUtils.percentValueOf(
                    this.value, 
                    PrivilegesProviderServer.getAsInt(mail.getSenderUUID(), EnumMailPrivilege.COD_POSTAGE_PERCENT.id(), MailConfig.COD_POSTAGE_PERCENT.asInt()));
            ItemStack itemStack = this.stackWrapper.getItemStack();
            MailHelperServer.sendSystemMail(
                    mail.getSenderUUID(),
                    OxygenMain.SYSTEM_SENDER, 
                    EnumMail.REMITTANCE,
                    "mail.cod.pay.s", 
                    Attachments.remittance(this.currencyIndex, this.value - codPostage),
                    true,
                    "mail.cod.pay.m",
                    CommonReference.getName(playerMP), 
                    String.valueOf(this.amount),
                    itemStack.getDisplayName());       
            InventoryProviderServer.getPlayerInventory().addItem(playerMP, this.stackWrapper, this.amount);     

            SoundEventHelperServer.playSoundClient(playerMP, OxygenSoundEffects.INVENTORY_OPERATION.getId());
            return true;
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public long getPostage() {
        return PrivilegesProviderClient.getAsLong(EnumMailPrivilege.PARCEL_POSTAGE_VALUE.id(), MailConfig.PARCEL_POSTAGE_VALUE.asLong())
                + MathUtils.percentValueOf(this.value, PrivilegesProviderClient.getAsInt(EnumMailPrivilege.COD_POSTAGE_PERCENT.id(), MailConfig.COD_POSTAGE_PERCENT.asInt()));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canSend() {
        return WatcherHelperClient.getLong(this.currencyIndex) >= this.value;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void sent() {
        MailManagerClient.instance().getMenuManager().removeItemStack(this.stackWrapper, this.amount);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canReceive() {
        return WatcherHelperClient.getLong(this.currencyIndex) >= this.value
                && InventoryProviderClient.getPlayerInventory().haveEnoughSpace(ClientReference.getClientPlayer(), this.stackWrapper, this.amount);
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

        CurrencyProperties currencyProperties = OxygenHelperClient.getCurrencyProperties(this.currencyIndex);

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

        GlStateManager.pushMatrix();           
        GlStateManager.translate(30.0F, 2.0F, 0.0F);            
        GlStateManager.scale(widget.getTextScale(), widget.getTextScale(), 0.0F);   
        mc.fontRenderer.drawString(ClientReference.localize("oxygen_mail.gui.mail.attachment.cost"), 0, 0, widget.getEnabledTextColor(), false); 
        GlStateManager.popMatrix();  

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);  

        GlStateManager.enableBlend(); 
        mc.getTextureManager().bindTexture(currencyProperties.getIcon());
        GUIAdvancedElement.drawCustomSizedTexturedRect(
                30 + currencyProperties.getXOffset(), 
                4 + (widget.getHeight() - currencyProperties.getIconHeight()) / 2 + currencyProperties.getYOffset(), 
                0, 
                0, 
                currencyProperties.getIconWidth(), 
                currencyProperties.getIconHeight(), 
                currencyProperties.getIconWidth(), 
                currencyProperties.getIconHeight());            
        GlStateManager.disableBlend();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.pushMatrix();           
        GlStateManager.translate(41.0F, 4.0F + ((float) widget.getHeight() - widget.textHeight(widget.getTextScale())) / 2.0F, 0.0F);            
        GlStateManager.scale(widget.getTextScale(), widget.getTextScale(), 0.0F);   
        mc.fontRenderer.drawString(String.valueOf(this.value), 0, 0, widget.isEnabled() ? widget.getEnabledTextColor() : widget.getStaticBackgroundColor(), false); 
        GlStateManager.popMatrix();   

        GlStateManager.popMatrix();
    }

    @Override
    public void write(BufferedOutputStream bos) throws IOException {
        this.stackWrapper.write(bos);
        StreamUtils.write((short) this.amount, bos);
        StreamUtils.write((byte) this.currencyIndex, bos);
        StreamUtils.write(this.value, bos);
    }

    public static Attachment read(BufferedInputStream bis) throws IOException {
        return new AttachmentCOD(ItemStackWrapper.read(bis), StreamUtils.readShort(bis), StreamUtils.readByte(bis), StreamUtils.readLong(bis));
    }

    @Override
    public void write(ByteBuf buffer) {
        this.stackWrapper.write(buffer);
        buffer.writeShort(this.amount);
        buffer.writeByte(this.currencyIndex);
        buffer.writeLong(this.value);
    }

    public static Attachment read(ByteBuf buffer) {
        return new AttachmentCOD(ItemStackWrapper.read(buffer), buffer.readShort(), buffer.readByte(), buffer.readLong());
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
        return String.format("[stack wrapper: %s, amount: %d, currency index: %d, price: %d]",
                this.stackWrapper, 
                this.amount,
                this.currencyIndex, 
                this.value);
    }
}
