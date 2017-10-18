package com.voxelwind.server.game.inventories.transaction.type;

import com.flowpowered.math.vector.Vector3f;
import com.voxelwind.api.game.item.ItemStack;
import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.mcpe.McpeUtil;
import com.voxelwind.server.network.mcpe.packets.McpeInventoryTransaction;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public abstract class TransactionType {
    private int slot;
    private ItemStack item;
    private Vector3f fromPosition;

    public void read(ByteBuf buffer){
        slot = Varints.decodeSigned(buffer);
        item = McpeUtil.readItemStack(buffer);
        fromPosition = McpeUtil.readVector3f(buffer);
    }

    public void write(ByteBuf buffer){
        Varints.encodeSigned(buffer, slot);
        McpeUtil.writeItemStack(buffer, item);
        McpeUtil.writeVector3f(buffer, fromPosition);
    }

    public abstract McpeInventoryTransaction.Type getType();
}
