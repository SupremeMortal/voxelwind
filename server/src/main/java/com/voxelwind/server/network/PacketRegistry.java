package com.voxelwind.server.network;

import com.voxelwind.server.network.mcpe.annotations.DisallowWrapping;
import com.voxelwind.server.network.mcpe.packets.*;
import com.voxelwind.server.network.raknet.packets.*;
import gnu.trove.TCollections;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import lombok.extern.log4j.Log4j2;

@Log4j2
@SuppressWarnings("unchecked")
public class PacketRegistry {
    private static final Class<? extends NetworkPackage>[] RAKNET_PACKETS = new Class[256];
    private static final Class<? extends NetworkPackage>[] MCPE_PACKETS = new Class[256];
    private static final TObjectIntMap<Class<? extends NetworkPackage>> PACKAGE_MAPPING;

    static {
        RAKNET_PACKETS[0x00] = ConnectedPingPacket.class;
        RAKNET_PACKETS[0x01] = UnconnectedPingPacket.class;
        RAKNET_PACKETS[0x03] = ConnectedPongPacket.class;
        RAKNET_PACKETS[0x05] = OpenConnectionRequest1Packet.class;
        RAKNET_PACKETS[0x06] = OpenConnectionResponse1Packet.class;
        RAKNET_PACKETS[0x07] = OpenConnectionRequest2Packet.class;
        RAKNET_PACKETS[0x08] = OpenConnectionResponse2Packet.class;
        RAKNET_PACKETS[0x09] = ConnectionRequestPacket.class;
        RAKNET_PACKETS[0x10] = ConnectionResponsePacket.class;
        RAKNET_PACKETS[0x13] = NewIncomingConnectionPacket.class;
        RAKNET_PACKETS[0x14] = NoFreeIncomingConnectionsPacket.class;
        RAKNET_PACKETS[0x15] = DisconnectNotificationPacket.class;
        RAKNET_PACKETS[0x17] = ConnectionBannedPacket.class;
        RAKNET_PACKETS[0x1a] = IpRecentlyConnectedPacket.class;
        RAKNET_PACKETS[0x1c] = UnconnectedPongPacket.class;
        RAKNET_PACKETS[0xa0] = NakPacket.class;
        RAKNET_PACKETS[0xc0] = AckPacket.class;
        RAKNET_PACKETS[0xfe] = McpeWrapper.class; // Technically not an MCPE packet, but here for convenience

        MCPE_PACKETS[0x01] = McpeLogin.class;
        MCPE_PACKETS[0x02] = McpePlayStatus.class;
        MCPE_PACKETS[0x03] = McpeServerToClientHandshake.class;
        MCPE_PACKETS[0x04] = McpeClientToServerHandshake.class;
        MCPE_PACKETS[0x05] = McpeDisconnect.class;
        MCPE_PACKETS[0x06] = McpeResourcePacksInfo.class;
        MCPE_PACKETS[0x07] = McpeResourcePackStack.class;
        MCPE_PACKETS[0x08] = McpeResourcePackClientResponse.class;
        MCPE_PACKETS[0x09] = McpeText.class;
        MCPE_PACKETS[0x0a] = McpeSetTime.class;
        MCPE_PACKETS[0x0b] = McpeStartGame.class;
        MCPE_PACKETS[0x0c] = McpeAddPlayer.class;
        MCPE_PACKETS[0x0d] = McpeAddEntity.class;
        MCPE_PACKETS[0x0e] = McpeRemoveEntity.class;
        MCPE_PACKETS[0x0f] = McpeAddItemEntity.class;
        MCPE_PACKETS[0x10] = McpeAddHangingEntity.class;
        MCPE_PACKETS[0x11] = McpeTakeItemEntity.class;
        MCPE_PACKETS[0x12] = McpeMoveEntity.class;
        MCPE_PACKETS[0x13] = McpeMovePlayer.class;
        MCPE_PACKETS[0x14] = McpeRiderJump.class;
        MCPE_PACKETS[0x15] = McpeUpdateBlock.class;
        MCPE_PACKETS[0x16] = McpeAddPainting.class;
        MCPE_PACKETS[0x17] = McpeExplode.class;
        MCPE_PACKETS[0x18] = McpeLevelSoundEvent.class;
        MCPE_PACKETS[0x19] = McpeLevelEvent.class;
        MCPE_PACKETS[0x1a] = McpeBlockEvent.class;
        MCPE_PACKETS[0x1b] = McpeEntityEvent.class;
        MCPE_PACKETS[0x1c] = McpeMobEffect.class;
        MCPE_PACKETS[0x1d] = McpeUpdateAttributes.class;
        MCPE_PACKETS[0x1e] = McpeInventoryTransaction.class;
        MCPE_PACKETS[0x1f] = McpeMobEquipment.class;
        MCPE_PACKETS[0x20] = McpeMobArmorEquipment.class;
        MCPE_PACKETS[0x21] = McpeInteract.class;
        //MCPE_PACKETS[0x22] = McpeBlockPickRequest.class;
        //MCPE_PACKETS[0x23] = McpeEntityPickRequest.class;
        MCPE_PACKETS[0x24] = McpePlayerAction.class;
        //MCPE_PACKETS[0x25] = McpeEntityFall.class;
        //MCPE_PACKETS[0x26] = McpeHurtArmor.class
        MCPE_PACKETS[0x27] = McpeSetEntityData.class;
        MCPE_PACKETS[0x28] = McpeSetEntityMotion.class;
        //MCPE_PACKETS[0x29] = McpeSetEntityLink.class;
        MCPE_PACKETS[0x2a] = McpeSetHealth.class;
        MCPE_PACKETS[0x2b] = McpeSetSpawnPosition.class;
        MCPE_PACKETS[0x2c] = McpeAnimate.class;
        MCPE_PACKETS[0x2d] = McpeRespawn.class;
        MCPE_PACKETS[0x2e] = McpeContainerOpen.class;
        MCPE_PACKETS[0x2f] = McpeContainerClose.class;
        MCPE_PACKETS[0x30] = McpePlayerHotbar.class;
        MCPE_PACKETS[0x31] = McpeInventoryContent.class;
        MCPE_PACKETS[0x32] = McpeInventorySlot.class;
        MCPE_PACKETS[0x33] = McpeContainerSetData.class;
        //MCPE_PACKETS[0x34] = McpeCraftingData.class
        //MCPE_PACKETS[0x35] = McpeCraftingEvent.class
        //MCPE_PACKETS[0x36] = McpeGuiDataPickItem.class;
        MCPE_PACKETS[0x37] = McpeAdventureSettings.class;
        MCPE_PACKETS[0x38] = McpeBlockEntityData.class;
        //MCPE_PACKETS[0x39] = McpePlayerInput.class;
        MCPE_PACKETS[0x3a] = McpeFullChunkData.class;
        MCPE_PACKETS[0x3b] = McpeSetCommandsEnabled.class;
        MCPE_PACKETS[0x3c] = McpeSetDifficulty.class;
        MCPE_PACKETS[0x3d] = McpeChangeDimension.class;
        MCPE_PACKETS[0x3e] = McpeSetPlayerGameType.class;
        MCPE_PACKETS[0x3f] = McpePlayerList.class;
        //MCPE_PACKETS[0x40] = McpeSimpleEvent.class;
        //MCPE_PACKETS[0x41] = McpeTelemetryEvent.class;
        //MCPE_PACKETS[0x42] = McpeSpawnExperienceOrb.class;
        //MCPE_PACKETS[0x43] = McpeClientboundMapItemData.class;
        //MCPE_PACKETS[0x44] = McpeMapInfoRequest.class;
        MCPE_PACKETS[0x45] = McpeRequestChunkRadius.class;
        MCPE_PACKETS[0x46] = McpeChunkRadiusUpdate.class;
        //MCPE_PACKETS[0x47] = McpeItemFrameDropItem.class;
        //MCPE_PACKETS[0x48] = McpeGameRulesChanged.class;
        //MCPE_PACKETS[0x49] = McpeCamera.class;
        //MCPE_PACKETS[0x4a] = McpeBossEvent.class;
        //MCPE_PACKETS[0x4b] = McpeShowCredits.class;
        MCPE_PACKETS[0x4c] = McpeAvailableCommands.class;
        MCPE_PACKETS[0x4d] = McpeCommandRequest.class; //McpeCommandStep.class
        //MCPE_PACKETS[0x4e] = McpeCommandBlockUpdate.class;
        //MCPE_PACKETS[0x4f] = McpeCommandOutput.class;
        //MCPE_PACKETS[0x50] = McpeUpdateTrade.class;
        //MCPE_PACKETS[0x51] = McpeUpdateEquip.class;
        //MCPE_PACKETS[0x52] = McpeResourcePackDataInfo.class;
        //MCPE_PACKETS[0x53] = McpeResourcePackChunkData.class;
        //MCPE_PACKETS[0x54] = McpeResourcePackChunkRequest.class;
        //MCPE_PACKETS[0x55] = McpeTransfer.class;
        //MCPE_PACKETS[0x56] = McpePlaySound.class;
        //MCPE_PACKETS[0x57] = MspeStopSound.class;
        //MCPE_PACKETS[0x58] = McpeSetTitle.class;
        //MCPE_PACKETS[0x59] = McpeAddBehaviorTree.class;
        //MCPE_PACKETS[0x5a] = McpeStructureBlockUpdate.class;
        //MCPE_PACKETS[0x5b] = McpeShowStoreOffer.class;
        //MCPE_PACKETS[0x5c] = McpePurchaseReceipt.class;
        //MCPE_PACKETS[0x5d] = McpePlayerSkin.class;
        MCPE_PACKETS[0x5e] = McpeSubClientLogin.class;
        //MCPE_PACKETS[0x5f] = McpeWSConnect.class;
        //MCPE_PACKETS[0x60] = McpeSetLastHurtBy.class;
        //MCPE_PACKETS[0x61] = McpeBookEdit.class;
        //MCPE_PACKETS[0x62] = McpeNpcRequest.class;
        //MCPE_PACKETS[0x63] = McpePhotoTransfer.class;
        //MCPE_PACKETS[0x64] = McpeModalFormRequest.class;
        //MCPE_PACKETS[0x65] = McpeModalFormResponse.class;
        //MCPE_PACKETS[0x66] = McpeServerSettingsRequest.class;
        //MCPE_PACKETS[0x67] = McpeServerSettingsResponse.class;
        //MCPE_PACKETS[0x68] = McpeShowProfile.class;

        TObjectIntMap<Class<? extends NetworkPackage>> classToIdMap = new TObjectIntHashMap<>(64, 0.75f, -1);
        for (int i = 0; i < RAKNET_PACKETS.length; i++) {
            Class clazz = RAKNET_PACKETS[i];
            if (clazz != null) {
                classToIdMap.put(clazz, i);
            }
        }

        for (int i = 0; i < MCPE_PACKETS.length; i++) {
            Class clazz = MCPE_PACKETS[i];
            if (clazz != null) {
                classToIdMap.put(clazz, i);
            }
        }

        PACKAGE_MAPPING = TCollections.unmodifiableMap(classToIdMap);
    }

    private PacketRegistry() {

    }

    public static NetworkPackage tryDecode(ByteBuf buf, PacketType type) {
        return tryDecode(buf, type, false);
    }

    public static NetworkPackage tryDecode(ByteBuf buf, PacketType type, boolean fromBatch) {
        int id = buf.readUnsignedByte();
        Class<? extends NetworkPackage> pkgClass;
        switch (type) {
            case RAKNET:
                pkgClass = RAKNET_PACKETS[id];
                break;
            case MCPE:
                pkgClass = MCPE_PACKETS[id];
                break;
            default:
                throw new IllegalArgumentException("Invalid PacketType");
        }

        if (pkgClass == null) {
            return null;
        }

        if (fromBatch) {
            if (pkgClass.isAnnotationPresent(DisallowWrapping.class)) {
                return null;
            }
            buf.skipBytes(2); // Two byte space between ID and Data
        }

        NetworkPackage netPackage;
        try {
            netPackage = pkgClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Unable to create packet instance", e);
        }

        netPackage.decode(buf);

        if (log.isDebugEnabled()) {
            if (buf.readableBytes() > 0) {
                log.debug(netPackage.getClass().getSimpleName() + " still has " + buf.readableBytes() + " bytes to read!");
            }
        }

        return netPackage;
    }

    public static int getId(NetworkPackage pkg) {
        Class<? extends NetworkPackage> pkgClass = pkg.getClass();
        int res = PACKAGE_MAPPING.get(pkgClass);
        if (res == -1) {
            throw new IllegalArgumentException("Packet ID for " + pkgClass.getName() + " does not exist.");
        }
        return res;
    }

    public static ByteBuf tryEncode(NetworkPackage pkg){
        return tryEncode(pkg, false);
    }

    public static ByteBuf tryEncode(NetworkPackage pkg, boolean fromBatch) {
        int id = getId(pkg);

        ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer();
        buf.writeByte((id & 0xFF));
        if(fromBatch){
            buf.writeBytes(new byte[]{0x00, 0x00});
        }
        pkg.encode(buf);

        return buf;
    }
}
