package com.beckadam.ballisticblood.particles;

import java.util.HashMap;

public class ParticleTypeManager {
    private int count = 0;
    private final HashMap<String, Integer> types = new HashMap<>();
    public ParticleTypeManager() {}
    public void init() {
        types.clear();
        count = 0;
    }
    public int add(String type) {
        types.put(type, count);
        return count++;
    }
    public int get(String type) {
        return types.getOrDefault(type, 0);
    }
}
