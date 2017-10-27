package com.voxelwind.server.game.inventories.transaction.record;

import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.session.PlayerSession;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
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

    @Override
    public void execute(PlayerSession session) {
        switch (SlotAction.values()[getSlot()]) {
            case DROP_ITEM:
                session.handledropItem(this);
                break;
            case PICKUP_ITEM:
        }
    }

    public enum SlotAction {
        DROP_ITEM,
        PICKUP_ITEM
    }
}
