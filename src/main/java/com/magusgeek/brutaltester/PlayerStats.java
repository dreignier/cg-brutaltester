package com.magusgeek.brutaltester;

public class PlayerStats {
    private int[] stats = new int[4];
    private int total;
    
    public PlayerStats() {
        total = 0;
        
        for (int i = 0; i < 4; ++i) {
            stats[i] = 0;
        }
    }
    
    public void add(int position) {
        stats[position] += 1;
        total += 1;
    }
    
    public void print() {
        if (total > 0) {
            for (int i = 0; i < 4; ++i) {
                System.out.println(" Position " + (i + 1) + " : " + stats[i] + " (" + (stats[i]*100 / total) + "%)");
            }
        }
    }
}
