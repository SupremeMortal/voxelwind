package com.voxelwind.server.game.inventories.transaction;

import com.voxelwind.server.network.session.McpeSession;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class InventoryMismatchTransaction extends InventoryTransaction {
    private static final Type type = Type.INVENTORY_MISMATCH;

    @Override
    public void read(ByteBuf buffer){
    }

    @Override
    public void write(ByteBuf buffer){
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void handle(McpeSession session) {
        session.getHandler().handle(this);
    }
}
