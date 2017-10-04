package com.voxelwind.server.game.inventories.record;

import com.voxelwind.nbt.util.Varints;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class WorldInteractionTransactionRecord extends TransactionRecord{
    private int flags;

    @Override
    public void write(ByteBuf buffer){
        Varints.encodeUnsigned(buffer, flags);
        super.write(buffer);
    }

    @Override
    public void read(ByteBuf buffer) {
        flags = (int) Varints.decodeUnsigned(buffer);
        super.read(buffer);
    }
}
