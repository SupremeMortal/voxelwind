package com.voxelwind.server.game.inventories.transaction.record;

import com.voxelwind.api.game.item.ItemStack;
import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.mcpe.McpeUtil;
import com.voxelwind.server.network.session.PlayerSession;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public abstract class TransactionRecord{
    private int slot;
    private ItemStack oldItem;
    private ItemStack newItem;

    public void write(ByteBuf buffer){
        Varints.encodeUnsigned(buffer, slot);
        McpeUtil.writeItemStack(buffer, oldItem);
        McpeUtil.writeItemStack(buffer, newItem);
    }

    public void read(ByteBuf buffer){
        slot = (int) Varints.decodeUnsigned(buffer);
        oldItem = McpeUtil.readItemStack(buffer);
        newItem = McpeUtil.readItemStack(buffer);
    }

    public abstract void execute(PlayerSession session);
}

