package com.duelplugin.arena;

import org.bukkit.Location;

public class Arena {

    private final String name;
    private Location pos1;
    private Location pos2;
    private Location spawn1;
    private Location spawn2;
    private boolean inUse;

    public Arena(String name) {
        this.name = name;
        this.inUse = false;
    }

    public String getName() {
        return name;
    }

    public Location getPos1() {
        return pos1;
    }

    public void setPos1(Location pos1) {
        this.pos1 = pos1;
    }

    public Location getPos2() {
        return pos2;
    }

    public void setPos2(Location pos2) {
        this.pos2 = pos2;
    }

    public Location getSpawn1() {
        return spawn1;
    }

    public void setSpawn1(Location spawn1) {
        this.spawn1 = spawn1;
    }

    public Location getSpawn2() {
        return spawn2;
    }

    public void setSpawn2(Location spawn2) {
        this.spawn2 = spawn2;
    }

    public boolean isInUse() {
        return inUse;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    /** An arena needs both corners and both spawn points set before it can be used. */
    public boolean isComplete() {
        return pos1 != null && pos2 != null && spawn1 != null && spawn2 != null;
    }

    /** Whether the given location falls within this arena's registered bounding box. */
    public boolean contains(Location loc) {
        if (pos1 == null || pos2 == null || loc == null || loc.getWorld() == null
                || !loc.getWorld().equals(pos1.getWorld())) {
            return false;
        }

        double minX = Math.min(pos1.getX(), pos2.getX());
        double maxX = Math.max(pos1.getX(), pos2.getX());
        double minY = Math.min(pos1.getY(), pos2.getY());
        double maxY = Math.max(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());

        return loc.getX() >= minX && loc.getX() <= maxX
                && loc.getY() >= minY && loc.getY() <= maxY
                && loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }
}
