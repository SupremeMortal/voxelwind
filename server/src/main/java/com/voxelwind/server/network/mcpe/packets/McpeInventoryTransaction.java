package com.voxelwind.server.network.mcpe.packets;

import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.game.inventories.transaction.*;
import com.voxelwind.server.game.inventories.transaction.record.*;
import com.voxelwind.server.network.NetworkPackage;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class McpeInventoryTransaction implements NetworkPackage{
    private InventoryTransaction transaction;

    @Override
    public void decode(ByteBuf buffer) {
        InventoryTransaction.Type type = InventoryTransaction.Type.values()[(int) Varints.decodeUnsigned(buffer)];

        switch (type) {
            case NORMAL:
                transaction = new NormalTransaction();
                break;
            case INVENTORY_MISMATCH:
                transaction = new InventoryMismatchTransaction();
                break;
            case ITEM_USE:
                transaction = new ItemUseTransaction();
                break;
            case ITEM_USE_ON_ENTITY:
                transaction = new ItemUseOnEntityTransaction();
                break;
            case ITEM_RELEASE:
                transaction = new ItemReleaseTransaction();
                break;
        }

        int count = (int) Varints.decodeUnsigned(buffer);
        for(int i = 0; i < count; i++) {
            TransactionRecord record = null;
            int sourceTypeValue = (int) Varints.decodeUnsigned(buffer);
            InventorySourceType sourceType = InventorySourceType.values()[(sourceTypeValue == 99999) ? 4 : sourceTypeValue]; // Makes it easier for now

            switch (sourceType) {
                case CONTAINER:
                    record = new ContainerTransactionRecord();
                    break;
                case GLOBAL:
                    record = new GlobalTransactionRecord();
                    break;
                case WORLD_INTERACTION:
                    record = new WorldInteractionTransactionRecord();
                    break;
                case CREATIVE:
                    record = new CreativeTransactionRecord();
                    break;
                case CRAFT:
                    record = new CraftTransactionRecord();
                    break;
                default:
                    break;
            }
            record.read(buffer);
            transaction.getRecords().add(record);
        }
        transaction.read(buffer);
    }

    @Override
    public void encode(ByteBuf buffer) {
        Varints.encodeUnsigned(buffer, transaction.getType().ordinal());
        Varints.encodeUnsigned(buffer, transaction.getRecords().size());
        for (TransactionRecord record : transaction.getRecords()) {
            record.write(buffer);
        }
        transaction.write(buffer);
    }

    public enum InventorySourceType{
        CONTAINER,
        GLOBAL,
        WORLD_INTERACTION,
        CREATIVE,
        CRAFT
    }
}
