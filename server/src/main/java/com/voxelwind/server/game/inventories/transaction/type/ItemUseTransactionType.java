package com.voxelwind.server.game.inventories.transaction.type;

import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector3i;
import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.mcpe.McpeUtil;
import com.voxelwind.server.network.mcpe.packets.McpeInventoryTransaction;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ItemUseTransactionType extends TransactionType {
    private static final McpeInventoryTransaction.Type type = McpeInventoryTransaction.Type.ITEM_USE;
    private Action actionType;
    private Vector3i position;
    private int face;
    private Vector3f clickPosition;

    public void read(ByteBuf buffer){
        actionType = Action.values()[(int) Varints.decodeUnsigned(buffer)];
        position = McpeUtil.readBlockCoords(buffer);
        face = Varints.decodeSigned(buffer);
        super.read(buffer);
        clickPosition = McpeUtil.readVector3f(buffer);
    }

    public void write(ByteBuf buffer){
        Varints.encodeUnsigned(buffer, actionType.ordinal());
        McpeUtil.writeBlockCoords(buffer, position);
        Varints.encodeSigned(buffer, face);
        super.write(buffer);
        McpeUtil.writeVector3f(buffer, clickPosition);
    }

    @Override
    public McpeInventoryTransaction.Type getType(){
        return type;
    }

    public enum Action {
        PLACE,
        USE,
        DESTROY
    }
}
