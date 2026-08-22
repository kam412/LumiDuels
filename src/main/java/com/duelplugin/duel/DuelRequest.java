package com.duelplugin.duel;

import java.util.UUID;

public class DuelRequest {

    private final UUID sender;
    private final UUID target;
    private final String kitName;
    private final long expiresAt;

    public DuelRequest(UUID sender, UUID target, String kitName, long expiresAt) {
        this.sender = sender;
        this.target = target;
        this.kitName = kitName;
        this.expiresAt = expiresAt;
    }

    public UUID getSender() {
        return sender;
    }

    public UUID getTarget() {
        return target;
    }

    public String getKitName() {
        return kitName;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }
}
