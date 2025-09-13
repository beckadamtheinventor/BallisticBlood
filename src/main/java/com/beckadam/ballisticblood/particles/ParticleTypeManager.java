package com.beckadam.ballisticblood.particles;

import java.util.HashMap;

public class ParticleTypeManager {
    private int count;
    private final HashMap<String, Integer> types;

    public ParticleTypeManager() {
        types = new HashMap<>();
        count = 0;
    }
    public void init() {
        types.clear();
        count = 0;
    }
    public int add(String type) {
        types.put(type, count);
        return count++;
    }
    public int get(String type) {
        return types.get(type);
    }
}
