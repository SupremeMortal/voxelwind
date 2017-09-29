package com.voxelwind.server.network.mcpe.packets;

import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.NetworkPackage;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class McpeMobEffect implements NetworkPackage {
    private long runtimeEntityId;
    private byte eventId;
    private int effectId;
    private int amplifier;
    private boolean particles;
    private int duration;

    @Override
    public void decode(ByteBuf buffer) {
        runtimeEntityId = Varints.decodeUnsigned(buffer);
        eventId = buffer.readByte();
        effectId = Varints.decodeSigned(buffer);
        amplifier = Varints.decodeSigned(buffer);
        particles = buffer.readBoolean();
        duration = Varints.decodeSigned(buffer);
    }

    @Override
    public void encode(ByteBuf buffer) {
        Varints.encodeUnsigned(buffer, runtimeEntityId);
        buffer.writeByte(eventId);
        Varints.encodeSigned(buffer, eventId);
        Varints.encodeSigned(buffer, amplifier);
        buffer.writeBoolean(particles);
        Varints.encodeSigned(buffer, duration);
    }
}
