package com.magusgeek.brutaltester.util;

public class Mutable<T> {

    T value;

    public Mutable(T value) {
        this.value = value;
    }
    
    public Mutable() {
        
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

}
