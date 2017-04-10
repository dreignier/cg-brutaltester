package com.magusgeek.brutaltester;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PlayerStats {
    private static final Log LOG = LogFactory.getLog(PlayerStats.class);
    
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
                LOG.info(" Position " + (i + 1) + " : " + stats[i] + " (" + (stats[i]*100 / total) + "%)");
            }
        }
    }
}
