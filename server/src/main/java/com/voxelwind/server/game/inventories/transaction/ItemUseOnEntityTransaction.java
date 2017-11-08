package com.voxelwind.server.game.inventories.transaction;

import com.flowpowered.math.vector.Vector3f;
import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.mcpe.McpeUtil;
import com.voxelwind.server.network.session.McpeSession;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class ItemUseOnEntityTransaction extends InventoryTransaction {
    private static final Type type = Type.ITEM_USE_ON_ENTITY;
    private long entityId;
    private Action action;
    private Vector3f clickPosition;

    @Override
    public void read(ByteBuf buffer){
        entityId = Varints.decodeUnsigned(buffer);
        action = Action.values()[(int) Varints.decodeUnsigned(buffer)];
        super.read(buffer);
        clickPosition = McpeUtil.readVector3f(buffer);
    }

    @Override
    public void write(ByteBuf buffer){
        Varints.encodeUnsigned(buffer, entityId);
        Varints.encodeUnsigned(buffer, action.ordinal());
        super.write(buffer);
        McpeUtil.writeVector3f(buffer, clickPosition);
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void handle(McpeSession session) {
        session.getHandler().handle(this);
    }

    public enum Action {
        INTERACT,
        ATTACK,
        ITEM_INTERACT
    }
}
