package com.voxelwind.server.game.inventories.transaction;

import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector3i;
import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.mcpe.McpeUtil;
import com.voxelwind.server.network.session.McpeSession;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class ItemUseTransaction extends InventoryTransaction {
    private static final Type type = Type.ITEM_USE;
    private Action action;
    private Vector3i position;
    private int face;
    private Vector3f clickPosition;

    public void read(ByteBuf buffer){
        action = Action.values()[(int) Varints.decodeUnsigned(buffer)];
        position = McpeUtil.readBlockCoords(buffer);
        face = Varints.decodeSigned(buffer);
        super.read(buffer);
        clickPosition = McpeUtil.readVector3f(buffer);
    }

    public void write(ByteBuf buffer){
        Varints.encodeUnsigned(buffer, action.ordinal());
        McpeUtil.writeBlockCoords(buffer, position);
        Varints.encodeSigned(buffer, face);
        super.write(buffer);
        McpeUtil.writeVector3f(buffer, clickPosition);
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void handle(McpeSession session) {
        session.getHandler().handle(this);
    }

    public enum Action {
        PLACE,
        USE,
        BREAK
    }
}
