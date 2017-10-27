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
public class CraftTransactionRecord extends TransactionRecord{
    private int inventoryId;

    @Override
    public void write(ByteBuf buffer){
        Varints.encodeSigned(buffer, inventoryId);
        super.write(buffer);
    }

    @Override
    public void read(ByteBuf buffer){
        inventoryId = Varints.decodeSigned(buffer);
        super.read(buffer);
    }

    @Override
    public void execute(PlayerSession session) {
        // TODO
    }

    public static final int TYPE_CRAFTING_ADD_INGREDIENT = -2;
    public static final int TYPE_CRAFTING_REMOVE_INGREDIENT = -2;
    public static final int TYPE_CRAFTING_RESULT = -2;
    public static final int TYPE_CRAFTING_USE_INGREDIENT = -2;
    public static final int TYPE_CONTAINER_DROP_CONTENTS = -100;
}
