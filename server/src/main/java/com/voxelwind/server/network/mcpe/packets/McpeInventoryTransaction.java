package com.voxelwind.server.network.mcpe.packets;

import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.game.inventories.record.*;
import com.voxelwind.server.game.inventories.transaction.record.*;
import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.mcpe.McpeUtil;
import com.voxelwind.server.game.inventories.InventoryTransaction;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class McpeInventoryTransaction implements NetworkPackage{
    private InventoryTransaction transaction;

    @Override
    public void decode(ByteBuf buffer) {
        InventoryTransaction transaction = new InventoryTransaction();
        TransactionType transactionType = TransactionType.values()[(int)Varints.decodeUnsigned(buffer)];
        transaction.setTransactionType(transactionType);

        int count = (int) Varints.decodeUnsigned(buffer);
        for(int i = 0; i < count; i++){
            TransactionRecord record = null;
            int sourceTypeValue = (int) Varints.decodeUnsigned(buffer);
            InventorySourceType sourceType = InventorySourceType.values()[(sourceTypeValue == 99999) ? 4 : sourceTypeValue]; // Makes it easier for now

            switch(sourceType){
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
                    new CreativeTransactionRecord();
                    break;
                case CRAFTING:
                    record = new CraftTransactionRecord();
                    break;
            }
            record.read(buffer);
            record.setSource(sourceType);
            transaction.getTransactions().add(record);

            switch(transaction.getTransactionType()){
                case NORMAL:
                case INVENTORY_MISTMATCH:
                    break;
                case ITEM_USE:
                    transaction.setActionType((int) Varints.decodeUnsigned(buffer));
                    transaction.setPosition(McpeUtil.readBlockCoords(buffer));
                    transaction.setFace(Varints.decodeSigned(buffer));
                    transaction.setSlot(Varints.decodeSigned(buffer));
                    transaction.setItem(McpeUtil.readItemStack(buffer));
                    transaction.setFromPosition(McpeUtil.readVector3f(buffer));
                    transaction.setClickPosition(McpeUtil.readVector3f(buffer));
                    break;
                case ITEM_USE_ON_ENTITY:
                    transaction.setEntityId(Varints.decodeUnsigned(buffer));
                    transaction.setActionType((int) Varints.decodeUnsigned(buffer));
                    transaction.setSlot(Varints.decodeSigned(buffer));
                    transaction.setItem(McpeUtil.readItemStack(buffer));
                    transaction.setFromPosition(McpeUtil.readVector3f(buffer));
                    break;
                case ITEM_RELEASE:
                    transaction.setActionType((int) Varints.decodeUnsigned(buffer));
                    transaction.setSlot(Varints.decodeSigned(buffer));
                    transaction.setItem(McpeUtil.readItemStack(buffer));
                    transaction.setFromPosition(McpeUtil.readVector3f(buffer));
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void encode(ByteBuf buffer) {
        Varints.encodeUnsigned(buffer, transaction.getTransactionType().ordinal());
        Varints.encodeUnsigned(buffer, transaction.getTransactions().size());
        for(TransactionRecord record : transaction.getTransactions()){
            record.write(buffer);
        }
        switch(transaction.getTransactionType()){
            case NORMAL:
            case INVENTORY_MISTMATCH:
                break;
            case ITEM_USE:
                Varints.encodeUnsigned(buffer, transaction.getActionType());
                McpeUtil.writeBlockCoords(buffer, transaction.getPosition());
                Varints.encodeSigned(buffer, transaction.getFace());
                Varints.encodeSigned(buffer, transaction.getSlot());
                McpeUtil.writeItemStack(buffer, transaction.getItem());
                McpeUtil.writeVector3f(buffer, transaction.getFromPosition());
                McpeUtil.writeVector3f(buffer, transaction.getClickPosition());
                break;
            case ITEM_USE_ON_ENTITY:
                Varints.encodeUnsigned(buffer, transaction.getEntityId());
                Varints.encodeUnsigned(buffer, transaction.getActionType());
                Varints.encodeSigned(buffer, transaction.getSlot());
                McpeUtil.writeItemStack(buffer, transaction.getItem());
                McpeUtil.writeVector3f(buffer, transaction.getFromPosition());
                break;
            case ITEM_RELEASE:
                Varints.encodeUnsigned(buffer, transaction.getActionType());
                Varints.encodeSigned(buffer, transaction.getSlot());
                McpeUtil.writeItemStack(buffer, transaction.getItem());
                McpeUtil.writeVector3f(buffer, transaction.getFromPosition());
                break;
            default:
                break;
        }
    }

    public enum TransactionType{
        NORMAL,
        INVENTORY_MISTMATCH,
        ITEM_USE,
        ITEM_USE_ON_ENTITY,
        ITEM_RELEASE
    }
    public enum InventorySourceType{
        CONTAINER,
        GLOBAL,
        WORLD_INTERACTION,
        CREATIVE,
        CRAFTING
    }
    public enum NormalAction{
        PUT_SLOT(3),
        GET_SLOT(5),
        GET_RESULT(7),
        CRAFT_USE(9),
        ENCHANT_ITEM(29),
        ENCHANT_LAPIS(31),
        ENCHANT_RESULT(33),
        DROP(199);

        private int intVal;

        NormalAction(int intVal){
            this.intVal = intVal;
        }

        public int getIntVal() {
            return  intVal;
        }
    }
    public enum ItemReleaseAction{
        RELEASE,
        USE
    }
    public enum ItemUseAction
    {
        PLACE,
        USE,
        DESTROY
    }
    public enum ItemUseOnEntityAction
    {
        INTERACT,
        ATTACK,
        ITEM_INTERACT
    }
}
