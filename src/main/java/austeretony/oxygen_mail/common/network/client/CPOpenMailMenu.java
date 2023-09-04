package austeretony.oxygen_mail.common.network.client;

import austeretony.oxygen_core.client.api.OxygenHelperClient;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_mail.client.MailMenuManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;

public class CPOpenMailMenu extends Packet {

    public CPOpenMailMenu() {}

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {}

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        OxygenHelperClient.addRoutineTask(MailMenuManager::openMailMenuDelegated);
    }
}
