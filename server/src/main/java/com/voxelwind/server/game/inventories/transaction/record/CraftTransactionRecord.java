package com.voxelwind.server.game.inventories.transaction.record;

import com.voxelwind.nbt.util.Varints;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class CraftTransactionRecord extends TransactionRecord{
    private int action;

    @Override
    public void write(ByteBuf buffer){
        Varints.encodeSigned(buffer, action);
        super.write(buffer);
    }

    @Override
    public void read(ByteBuf buffer){
        action = Varints.decodeSigned(buffer);
        super.read(buffer);
    }
}
