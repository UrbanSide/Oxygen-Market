package austeretony.oxygen_trade.server.command;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.command.ArgumentExecutor;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_trade.common.config.TradeConfig;
import austeretony.oxygen_trade.common.main.TradeMain;
import austeretony.oxygen_trade.common.network.client.CPOpenTradeMenu;
import austeretony.oxygen_trade.server.TradeManagerServer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class TradeArgumentOperator implements ArgumentExecutor {

    @Override
    public String getName() {
        return "trade";
    }

    @Override
    public void process(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {   
        if (args.length >= 2) {
            EntityPlayerMP senderPlayerMP = null, targetPlayerMP;
            if (sender instanceof EntityPlayerMP)
                senderPlayerMP = CommandBase.getCommandSenderAsPlayer(sender);

            if (args[1].equals("-open-menu")) {
                if (args.length == 3) {
                    targetPlayerMP = CommandBase.getPlayer(server, sender, args[2]);
                    OxygenHelperServer.resetTimeOut(CommonReference.getPersistentUUID(targetPlayerMP), TradeMain.TRADE_MENU_TIMEOUT_ID);
                    OxygenMain.network().sendTo(new CPOpenTradeMenu(), targetPlayerMP);
                }
            } else if (args[1].equals("-cancel-offer")) {
                if (args.length == 4) {
                    long offerId = CommandBase.parseLong(args[2], 0L, Long.MAX_VALUE);
                    boolean returnItemToSeller = CommandBase.parseBoolean(args[3]);
                    TradeManagerServer.instance().getOffersManager().cancel(senderPlayerMP, offerId, true, returnItemToSeller);
                }
            } else if (args[1].equals("-clear-history")) {

                if (args.length == 4) {
                    int period = CommandBase.parseInt(args[3], - 1, TradeConfig.SALES_HISTORY_EXPIRE_TIME_HOURS.asInt());
                    if (args[2].equals("global")) {
                        TradeManagerServer.instance().getSalesHistoryManager().clearSalesHistoryGlobal(period);

                        if (sender instanceof EntityPlayerMP)
                            senderPlayerMP.sendMessage(new TextComponentTranslation("oxygen_trade.message.command.oxygens.trade.clearHistoryGlobal", 
                                    period)); 
                        else
                            server.sendMessage(new TextComponentString(String.format("Sales history cleared for last <%s> hours.", 
                                    period)));
                    } else if (args[2].equals("item")) {
                        if (!(sender instanceof EntityPlayerMP))
                            throw new WrongUsageException("Command available only for player!");
                        if (senderPlayerMP.getHeldItemMainhand() != ItemStack.EMPTY)
                            TradeManagerServer.instance().getSalesHistoryManager().clearSalesHistoryForItem(period, 
                                    ItemStackWrapper.getFromStack(senderPlayerMP.getHeldItemMainhand()));
                        else
                            throw new WrongUsageException("Main hand is empty!");    

                        senderPlayerMP.sendMessage(new TextComponentTranslation("oxygen_trade.message.command.oxygens.trade.clearHistoryItem", 
                                period,
                                senderPlayerMP.getHeldItemMainhand().getDisplayName())); 
                    }
                } else
                    throw new WrongUsageException("Invalid parameters for: -clear-history.");
            }
        }
    }
}
