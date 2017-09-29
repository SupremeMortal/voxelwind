package com.voxelwind.server.game.level.util;

public class Gamerule<T> {
    private String name;
    private T value;

    public Gamerule(String name, T value){
        this.name = name;
        this.value = value;
    }

    public String getName(){
        return this.name;
    }

    public T getValue(){
        return this.value;
    }

    @Override
    public String toString(){
        return "{" + name + ":" + value + "}";
    }
}
