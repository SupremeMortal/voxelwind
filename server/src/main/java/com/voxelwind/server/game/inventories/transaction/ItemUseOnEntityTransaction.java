package com.voxelwind.server.game.inventories.transaction;

import com.voxelwind.nbt.util.Varints;
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
    private int actionType;

    @Override
    public void read(ByteBuf buffer){
        entityId = Varints.decodeUnsigned(buffer);
        actionType = (int) Varints.decodeUnsigned(buffer);
        super.read(buffer);
    }

    @Override
    public void write(ByteBuf buffer){
        Varints.encodeUnsigned(buffer, entityId);
        Varints.encodeUnsigned(buffer, actionType);
        super.write(buffer);
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
