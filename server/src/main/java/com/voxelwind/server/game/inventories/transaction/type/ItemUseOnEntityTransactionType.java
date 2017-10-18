package com.voxelwind.server.game.inventories.transaction.type;

import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.mcpe.packets.McpeInventoryTransaction;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class ItemUseOnEntityTransactionType extends TransactionType {
    private static final McpeInventoryTransaction.Type type = McpeInventoryTransaction.Type.ITEM_USE_ON_ENTITY;
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

    public McpeInventoryTransaction.Type getType(){
        return type;
    }

    public enum Action {
        INTERACT,
        ATTACK,
        ITEM_INTERACT
    }
}
