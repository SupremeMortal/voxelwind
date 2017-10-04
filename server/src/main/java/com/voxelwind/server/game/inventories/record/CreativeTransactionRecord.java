package com.voxelwind.server.game.inventories.record;

import com.voxelwind.nbt.util.Varints;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class CreativeTransactionRecord extends TransactionRecord{
    private int inventoryId;

    @Override
    public void write(ByteBuf buffer){
        Varints.encodeUnsigned(buffer, inventoryId);
        super.write(buffer);
    }

    @Override
    public void read(ByteBuf buffer){
        inventoryId = 0x79; //(int) Varints.decodeUnsigned(buffer);
        super.read(buffer);
    }
}
