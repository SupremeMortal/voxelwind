package com.voxelwind.server.game.inventories.transaction;

import com.voxelwind.server.network.session.McpeSession;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class NormalTransaction extends InventoryTransaction {
    private static final Type type = Type.NORMAL;

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

    public static final int ACTION_PUT_SLOT = -2;
    public static final int ACTION_GET_SLOT = -3;
    public static final int ACTION_GET_RESULT = -4;
    public static final int ACTION_CRAFT_USE = -5;
    public static final int ACTION_ENCHANT_ITEM = 29;
    public static final int ACTION_ENCHANT_LAPIS = 31;
    public static final int ACTION_ENCHANT_RESULT = 33;
    public static final int ACTION_DROP = 199;
}
