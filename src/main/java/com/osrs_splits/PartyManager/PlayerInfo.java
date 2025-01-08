package com.osrs_splits.PartyManager;

import lombok.Getter;
import lombok.Setter;

@Getter
public class PlayerInfo {
    private final String name;
    @Setter
    private int world;
    @Setter
    private int rank;
    @Setter
    private boolean verified;
    @Setter
    private boolean confirmedSplit;
    @Setter
    private boolean externalSharingEnabled;

    public PlayerInfo(String name, int world, int rank, boolean verified, boolean confirmedSplit, boolean externalSharing) {
        this.name = name;
        this.world = world;
        this.rank = rank;
        this.verified = verified;
        this.confirmedSplit = confirmedSplit;
        this.externalSharingEnabled = externalSharing;
    }

}


