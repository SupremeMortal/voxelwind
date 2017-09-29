package com.voxelwind.server.network.mcpe.packets;

import com.voxelwind.api.game.item.ItemStack;
import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.mcpe.McpeUtil;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class McpeInventorySlot implements NetworkPackage {
    private int inventoryId;
    private int slot;
    private ItemStack stack;

    @Override
    public void decode(ByteBuf buffer) {
        inventoryId = Varints.decodeSigned(buffer);
        slot = Varints.decodeSigned(buffer);
        stack = McpeUtil.readItemStack(buffer);
    }

    @Override
    public void encode(ByteBuf buffer) {
        Varints.encodeSigned(buffer, inventoryId);
        Varints.encodeSigned(buffer, slot);
        McpeUtil.writeItemStack(buffer, stack);
    }
}
