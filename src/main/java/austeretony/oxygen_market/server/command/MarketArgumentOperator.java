package austeretony.oxygen_market.server.command;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import austeretony.oxygen_core.common.api.CommonReference;
import austeretony.oxygen_core.common.command.ArgumentExecutor;
import austeretony.oxygen_core.common.item.ItemStackWrapper;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.server.api.OxygenHelperServer;
import austeretony.oxygen_market.common.config.MarketConfig;
import austeretony.oxygen_market.common.main.MarketMain;
import austeretony.oxygen_market.common.network.client.CPOpenMarketMenu;
import austeretony.oxygen_market.server.MarketManagerServer;
import austeretony.oxygen_market.server.market.OfferServer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class MarketArgumentOperator implements ArgumentExecutor {

    @Override
    public String getName() {
        return "market";
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
                    OxygenHelperServer.resetTimeOut(CommonReference.getPersistentUUID(targetPlayerMP), MarketMain.MARKET_MENU_TIMEOUT_ID);
                    OxygenMain.network().sendTo(new CPOpenMarketMenu(), targetPlayerMP);

                    if (MarketConfig.ADVANCED_LOGGING.asBoolean())
                        OxygenMain.LOGGER.info("[Market] (Operator/Console) {} opened trade menu for player {}/{}.", 
                                sender.getName(),
                                CommonReference.getName(targetPlayerMP),
                                CommonReference.getPersistentUUID(targetPlayerMP));
                }
            } else if (args[1].equals("-cancel-offer")) {
                if (args.length == 4) {
                    long offerId = CommandBase.parseLong(args[2], 0L, Long.MAX_VALUE);
                    boolean returnItemToSeller = CommandBase.parseBoolean(args[3]);
                    OfferServer offer = MarketManagerServer.instance().getOffersManager().cancelOfferOp(offerId, returnItemToSeller);
                    if (offer != null) {                   
                        if (MarketConfig.ADVANCED_LOGGING.asBoolean())
                            OxygenMain.LOGGER.info("[Market] (Operator/Console) {} canceled offer: {}.", 
                                    sender.getName(),
                                    offer);
                    } else
                        OxygenMain.LOGGER.info("[Market] (Operator/Console) {} failed to cancel offer: {}.", 
                                sender.getName(),
                                offerId);
                }
            } else if (args[1].equals("-clear-history")) {

                if (args.length == 4) {
                    int period = CommandBase.parseInt(args[3], - 1, MarketConfig.SALES_HISTORY_EXPIRE_TIME_HOURS.asInt());
                    if (args[2].equals("-global")) {
                        MarketManagerServer.instance().getSalesHistoryManager().clearSalesHistoryGlobal(period);

                        if (sender instanceof EntityPlayerMP)
                            senderPlayerMP.sendMessage(new TextComponentTranslation("oxygen_trade.message.command.oxygens.trade.clearHistoryGlobal", 
                                    period)); 
                        else
                            server.sendMessage(new TextComponentString(String.format("Sales history cleared for last <%s> hours.", 
                                    period)));

                        if (MarketConfig.ADVANCED_LOGGING.asBoolean())
                            OxygenMain.LOGGER.info("[Market] (Operator/Console) {} cleared global sales history for last {} hours.", 
                                    sender.getName(),
                                    period);
                    } else if (args[2].equals("-item")) {
                        if (!(sender instanceof EntityPlayerMP))
                            throw new WrongUsageException("Command available only for player!");

                        if (senderPlayerMP.getHeldItemMainhand() != ItemStack.EMPTY)
                            MarketManagerServer.instance().getSalesHistoryManager().clearSalesHistoryForItem(period, 
                                    ItemStackWrapper.of(senderPlayerMP.getHeldItemMainhand()));
                        else
                            throw new WrongUsageException("Main hand is empty!");    

                        senderPlayerMP.sendMessage(new TextComponentTranslation("oxygen_trade.message.command.oxygens.trade.clearHistoryItem", 
                                period,
                                senderPlayerMP.getHeldItemMainhand().getDisplayName())); 

                        if (MarketConfig.ADVANCED_LOGGING.asBoolean())
                            OxygenMain.LOGGER.info("[Market] (Operator) Player {}/{} cleared global sales history for item <{}> for last {} hours..", 
                                    CommonReference.getName(senderPlayerMP),
                                    CommonReference.getPersistentUUID(senderPlayerMP),
                                    senderPlayerMP.getHeldItemMainhand().getDisplayName(),
                                    period);
                    }
                } else
                    throw new WrongUsageException("Invalid parameters for: -clear-history.");
            }
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 2)
            return CommandBase.getListOfStringsMatchingLastWord(args, "-open-menu", "-cancel-offer", "-clear-history", "-clear-history");
        else if (args.length >= 3)
            if (args[1].equals("-clear-history"))
                return CommandBase.getListOfStringsMatchingLastWord(args, "-global", "-item");
        return Collections.<String>emptyList();
    }
}
