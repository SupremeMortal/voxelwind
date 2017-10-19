package com.voxelwind.server.game.inventories.transaction.type;

import com.voxelwind.server.network.mcpe.packets.McpeInventoryTransaction;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class InventoryMismatchTransactionType extends TransactionType {
    private static final McpeInventoryTransaction.Type type = McpeInventoryTransaction.Type.INVENTORY_MISMATCH;

    @Override
    public void read(ByteBuf buffer){
    }

    @Override
    public void write(ByteBuf buffer){
    }

    public McpeInventoryTransaction.Type getType(){
        return type;
    }
}
