package austeretony.oxygen_mail.server;

import java.util.UUID;

import austeretony.oxygen_mail.common.EnumMessageOperation;

public class QueuedMailOperation {

    final UUID playerUUID;

    final long messageId;

    final EnumMessageOperation operation;

    protected QueuedMailOperation(UUID playerUUID, long messageId, EnumMessageOperation operation) {
        this.playerUUID = playerUUID;
        this.messageId = messageId;
        this.operation = operation;
    }
}