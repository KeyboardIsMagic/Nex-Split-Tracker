package com.osrs_splits.PartyManager;

import com.Utils.PlayerVerificationStatus;
import com.osrs_splits.OsrsSplitPlugin;
import com.osrs_splits.OsrsSplitsConfig;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PartyManager
{
    @Getter
    private String passphrase;
    @Getter
    private String leader;

    private final Map<String, PlayerVerificationStatus> verificationCache = new HashMap<>();
    @Getter
    private final Map<String, PlayerInfo> members = new HashMap<>();

    private final OsrsSplitPlugin plugin;
    private final OsrsSplitsConfig config;

    @Getter @Setter
    private String currentPartyPassphrase;

    public PartyManager(OsrsSplitsConfig config, OsrsSplitPlugin plugin)
    {
        this.config = config;
        this.plugin = plugin;
    }

    public boolean createParty(String leaderName, String passphrase, boolean externalSharing)
    {
        if (this.leader != null)
        {

            clearMembers();
        }

        if (leaderName == null || leaderName.isEmpty() || passphrase == null || passphrase.isEmpty())
        {

            return false;
        }

        int world = plugin.getClient().getWorld();

        // default rank/verified, rely on server for correct values
        PlayerInfo pLeader = new PlayerInfo(
                leaderName,
                world,
                -1,
                false,
                false,
                externalSharing
        );
        //pLeader.setExternalSharingEnabled(externalSharing);

        this.leader = leaderName;
        this.passphrase = passphrase;
        this.currentPartyPassphrase = passphrase;

        members.clear();
        members.put(leaderName, pLeader);
        System.out.println("DEBUG createParty(...) externalSharing = " + externalSharing);

        return true;
    }

    public void updateCurrentParty(String passphrase, Map<String, PlayerInfo> newMembers)
    {
        if (passphrase == null || newMembers == null)
        {

            return;
        }
        this.currentPartyPassphrase = passphrase;
        this.members.clear();
        this.members.putAll(newMembers);


    }



    public void updatePlayerData(String playerName, int newWorld)
    {
        PlayerInfo p = members.get(playerName);
        if (p != null)
        {
            // remove local verification logic
            p.setWorld(newWorld);
            synchronizePartyWithRedis();
        }
    }

    public boolean isInParty(String playerName)
    {
        return members.containsKey(playerName);
    }


    public boolean allPlayersConfirmedAndSameWorld()
    {
        if (members.isEmpty()) return false;
        int firstWorld = members.values().iterator().next().getWorld();
        for (PlayerInfo p : members.values())
        {
            if (!p.isConfirmedSplit() || p.getWorld() != firstWorld) return false;
        }
        return true;
    }

    public void clearMembers()
    {
        members.clear();

    }



    public void synchronizePartyWithRedis()
    {


        // Build full party data
        JSONObject payload = new JSONObject();
        payload.put("passphrase", passphrase);
        payload.put("leader", leader);

        JSONArray arr = new JSONArray();
        for (PlayerInfo m : members.values())
        {

            JSONObject o = new JSONObject();
            o.put("name", m.getName());
            o.put("world", m.getWorld());
            o.put("rank", m.getRank());
            o.put("verified", m.isVerified());
            o.put("confirmedSplit", m.isConfirmedSplit());
            o.put("externalSharingEnabled", m.isExternalSharingEnabled());
            arr.put(o);
        }
        payload.put("members", arr);

        payload.put("apiKey", plugin.getConfig().apiKey());

        plugin.getSocketIoClient().send("party_update", payload.toString());
    }



    public void setLeader(String leader)
    {
        if (leader == null || leader.isEmpty())
        {

            this.leader = null;
        }
        else
        {
            this.leader = leader;

        }
    }

    public void updateLocalExternalSharing(boolean externalSharing)
    {
        String localPlayer = plugin.getClient().getLocalPlayer().getName();
        PlayerInfo info = members.get(localPlayer);
        if (info != null)
        {
            info.setExternalSharingEnabled(externalSharing);
        }
    }


}
