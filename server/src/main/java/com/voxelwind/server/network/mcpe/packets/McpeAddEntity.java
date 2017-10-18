package com.voxelwind.server.network.mcpe.packets;

import com.flowpowered.math.vector.Vector3f;
import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.game.level.util.EntityAttribute;
import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.mcpe.McpeUtil;
import com.voxelwind.server.network.mcpe.util.metadata.MetadataDictionary;
import io.netty.buffer.ByteBuf;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class McpeAddEntity implements NetworkPackage {
    private long entityId;
    private long runtimeEntityId;
    private int entityType;
    private Vector3f position;
    private Vector3f velocity;
    private float yaw;
    private float pitch;
    private final List<EntityAttribute> attributes = new ArrayList<>();
    private final MetadataDictionary metadata = new MetadataDictionary();
    // private Links link; TODO

    @Override
    public void decode(ByteBuf buffer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void encode(ByteBuf buffer) {
        Varints.encodeSignedLong(buffer, entityId);
        Varints.encodeUnsigned(buffer, runtimeEntityId);
        Varints.encodeUnsigned(buffer, entityType);
        McpeUtil.writeVector3f(buffer, position);
        McpeUtil.writeVector3f(buffer, velocity);
        McpeUtil.writeFloatLE(buffer, yaw);
        McpeUtil.writeFloatLE(buffer, pitch);
        McpeUtil.writeEntityAttributes(buffer, attributes);
        metadata.writeTo(buffer);
        Varints.encodeUnsigned(buffer, 0); // links, todo
    }
}
