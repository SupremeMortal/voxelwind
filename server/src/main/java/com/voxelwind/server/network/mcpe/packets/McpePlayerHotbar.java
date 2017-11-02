package com.voxelwind.server.network.mcpe.packets;

import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.NetworkPackage;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class McpePlayerHotbar implements NetworkPackage{
    private int selectedSlot;
    private byte windowId;
    private int[] hotbarData = new int[0];
    private boolean selectHotbarSlot;

    @Override
    public void decode(ByteBuf buffer) {
        selectedSlot = (int) Varints.decodeUnsigned(buffer);
        windowId = buffer.readByte();
        int hotbarEntriesToRead = (int) Varints.decodeUnsigned(buffer);
        hotbarData = new int[hotbarEntriesToRead];
        for (int i = 0; i < hotbarEntriesToRead; i++) {
            hotbarData[i] = Varints.decodeSigned(buffer);
        }
        selectHotbarSlot = buffer.readBoolean();
    }

    @Override
    public void encode(ByteBuf buffer) {
        Varints.encodeUnsigned(buffer, selectedSlot);
        buffer.writeByte(windowId);
        Varints.encodeUnsigned(buffer, hotbarData.length);
        for (int i : hotbarData) {
            Varints.encodeSigned(buffer, i);
        }
        buffer.writeBoolean(selectHotbarSlot);
    }
}
