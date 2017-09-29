package com.voxelwind.server.network.mcpe.packets;

import com.voxelwind.api.game.item.ItemStack;
import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.mcpe.McpeUtil;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class McpeMobArmorEquipment implements NetworkPackage{
    private long runtimeEntityId;
    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;

    @Override
    public void decode(ByteBuf buffer) {
        runtimeEntityId = Varints.decodeUnsigned(buffer);
        helmet = McpeUtil.readItemStack(buffer);
        chestplate = McpeUtil.readItemStack(buffer);
        leggings = McpeUtil.readItemStack(buffer);
        boots = McpeUtil.readItemStack(buffer);
    }

    @Override
    public void encode(ByteBuf buffer) {
        Varints.encodeUnsigned(buffer, runtimeEntityId);
        McpeUtil.writeItemStack(buffer, helmet);
        McpeUtil.writeItemStack(buffer, chestplate);
        McpeUtil.writeItemStack(buffer, leggings);
        McpeUtil.writeItemStack(buffer, boots);
    }
}
