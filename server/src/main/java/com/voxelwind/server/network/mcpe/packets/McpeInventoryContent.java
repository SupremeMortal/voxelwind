package com.voxelwind.server.network.mcpe.packets;

import com.voxelwind.api.game.item.ItemStack;
import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.mcpe.McpeUtil;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class McpeInventoryContent implements NetworkPackage {
    private int inventoryId;
    private ItemStack[] stacks;

    @Override
    public void decode(ByteBuf buffer) {
        inventoryId = (int) Varints.decodeUnsigned(buffer);
        int stacksToRead = (int) Varints.decodeUnsigned(buffer);
        stacks = new ItemStack[stacksToRead];
        for (int i = 0; i < stacksToRead; i++) {
            stacks[i] = McpeUtil.readItemStack(buffer);
        }
    }

    @Override
    public void encode(ByteBuf buffer) {
        Varints.encodeUnsigned(buffer, inventoryId);
        Varints.encodeUnsigned(buffer, stacks.length);
        for (ItemStack stack : stacks) {
            McpeUtil.writeItemStack(buffer, stack);
        }
    }
}
