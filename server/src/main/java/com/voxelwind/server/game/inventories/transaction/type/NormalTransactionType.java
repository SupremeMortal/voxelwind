package com.voxelwind.server.game.inventories.transaction.type;

import com.voxelwind.server.network.mcpe.packets.McpeInventoryTransaction;
import io.netty.buffer.ByteBuf;

public class NormalTransactionType extends TransactionType {
    private static final McpeInventoryTransaction.Type type = McpeInventoryTransaction.Type.NORMAL;

    @Override
    public void read(ByteBuf buffer){
    }

    @Override
    public void write(ByteBuf buffer){
    }

    public McpeInventoryTransaction.Type getType(){
        return type;
    }

    public static final int ACTION_PUT_SLOT = -2;
    public static final int ACTION_GET_SLOT = -3;
    public static final int ACTION_GET_RESULT = -4;
    public static final int ACTION_CRAFT_USE = -5;
    public static final int ACTION_ENCHANT_ITEM = 29;
    public static final int ACTION_ENCHANT_LAPIS = 31;
    public static final int ACTION_ENCHANT_RESULT = 33;
    public static final int ACTION_DROP = 199;
}
