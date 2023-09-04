package austeretony.oxygen_permissions.common.network.operation;

public enum PermissionsOperation {

    //client -> server
    CHANGE_FORMATTING_ROLE,

    REQUEST_MANAGEMENT_DATA,
    CREATE_ROLE,
    EDIT_ROLE,
    REMOVE_ROLE,
    ADD_ROLE_PERMISSION,
    REMOVE_ROLE_PERMISSIONS,
    ADD_ROLES_TO_PLAYER,
    REMOVE_ROLES_FROM_PLAYER,

    //server -> client
    SYNC_ROLES_DATA,
    SYNC_PLAYER_ROLES,
    FORMATTING_ROLE_CHANGED,

    SYNC_MANAGEMENT_DATA
}
