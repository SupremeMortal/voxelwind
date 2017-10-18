package com.voxelwind.server.network.mcpe.packets;

import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.game.inventories.transaction.record.*;
import com.voxelwind.server.game.inventories.transaction.type.*;
import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.mcpe.McpeUtil;
import com.voxelwind.server.game.inventories.transaction.InventoryTransaction;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class McpeInventoryTransaction implements NetworkPackage{
    private InventoryTransaction transaction;

    @Override
    public void decode(ByteBuf buffer) {
        transaction = new InventoryTransaction();
        Type type = Type.values()[(int)Varints.decodeUnsigned(buffer)];

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
                case UNSPECIFIED:
                    record = new CraftTransactionRecord();
                    break;
                default:
                    break;
            }
            record.read(buffer);
            record.setSource(sourceType);
            transaction.getTransactions().add(record);
        }
        TransactionType transactionType = null;
        switch(type){
            case NORMAL:
                transactionType = new NormalTransactionType();
                break;
            case INVENTORY_MISMATCH:
                transactionType = new InventoryMismatchTransactionType();
                break;
            case ITEM_USE:
                transactionType = new ItemUseTransactionType();
                break;
            case ITEM_USE_ON_ENTITY:
                transactionType = new ItemUseOnEntityTransactionType();
                break;
            case ITEM_RELEASE:
                transactionType = new ItemReleaseTransactionType();
                break;
        }
        transactionType.read(buffer);
        transaction.setTransactionType(transactionType);
    }

    @Override
    public void encode(ByteBuf buffer) {
        Varints.encodeUnsigned(buffer, transaction.getType().ordinal());
        Varints.encodeUnsigned(buffer, transaction.getTransactions().size());
        for(TransactionRecord record : transaction.getTransactions()){
            record.write(buffer);
        }
        transaction.getTransactionType().write(buffer);
    }

    public enum Type{
        NORMAL,
        INVENTORY_MISMATCH,
        ITEM_USE,
        ITEM_USE_ON_ENTITY,
        ITEM_RELEASE
    }
    public enum InventorySourceType{
        CONTAINER,
        GLOBAL,
        WORLD_INTERACTION,
        CREATIVE,
        UNSPECIFIED
    }
}
