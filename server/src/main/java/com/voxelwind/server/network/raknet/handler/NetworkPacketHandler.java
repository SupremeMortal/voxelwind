package com.voxelwind.server.network.raknet.handler;

import com.voxelwind.server.network.mcpe.packets.*;

public interface NetworkPacketHandler {
    void handle(McpeLogin packet);

    void handle(McpeSubClientLogin packet);

    void handle(McpeClientToServerHandshake packet);

    void handle(McpeRequestChunkRadius packet);

    void handle(McpePlayerAction packet);

    void handle(McpeAnimate packet);

    void handle(McpeText packet);

    void handle(McpeMovePlayer packet);

    void handle(McpeContainerClose packet);

    void handle(McpeInventorySlot packet);

    void handle(McpeMobEquipment packet);

    void handle(McpeInventoryTransaction packet);

    void handle(McpeResourcePackClientResponse packet);

    void handle(McpeCommandRequest packet);

    void handle(McpeAdventureSettings packet);
}
