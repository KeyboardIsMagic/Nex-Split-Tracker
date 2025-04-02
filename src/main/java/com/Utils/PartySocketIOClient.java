package com.Utils;

import com.osrs_splits.OsrsSplitPlugin;
import com.osrs_splits.OsrsSplitPluginPanel;
import com.osrs_splits.PartyManager.PlayerInfo;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


import javax.swing.*;
import java.net.URISyntaxException;

public class PartySocketIOClient
{
    private final OsrsSplitPlugin plugin;
    private Socket socket;
    private String lastAction = null;
    private static final Logger log = LoggerFactory.getLogger(PartySocketIOClient.class);

    public PartySocketIOClient(String serverUrl, OsrsSplitPlugin plugin)
    {
        this.plugin = plugin;

        try
        {
            socket = IO.socket(serverUrl);

            socket.on(Socket.EVENT_CONNECT, args -> {

            });

            // Listen for party_update
            socket.on("party_update", args -> {
                try
                {
                    JSONObject partyData = new JSONObject(args[0].toString());

                    processPartyUpdate(partyData);
                    socket.emit("ack_party_update", "Party update processed successfully");
                }
                catch (Exception ignored)
                {
                    //logging not req
                }
            });

            // Listen for joinPartyError
            socket.on("joinPartyError", args -> {
                SwingUtilities.invokeLater(() -> {
                    try
                    {
                        JSONObject obj = new JSONObject(args[0].toString());
                        String msg = obj.optString("message", "Party join error");


                        plugin.getPartyManager().setCurrentPartyPassphrase(null);

                        plugin.getPartyManagerPanel().getCreatePartyButton().setEnabled(true);
                        plugin.getPartyManagerPanel().getJoinPartyButton().setEnabled(true);

                        plugin.getPartyManagerPanel().getStatusLabel().setText("Error: " + msg);
                        plugin.getPartyManagerPanel().getStatusLabel().setVisible(true);
                    }
                    catch (Exception ignored)
                    {
                        //logging not req
                    }
                });
            });

            // Listen for "response" from server
            socket.on("response", args -> {
                SwingUtilities.invokeLater(() -> {
                    try {
                        JSONObject obj = new JSONObject(args[0].toString());
                        String status = obj.optString("status", "");
                        String message = obj.optString("message", "");


                        OsrsSplitPluginPanel panel = plugin.getPartyManagerPanel();

                        if ("success".equalsIgnoreCase(status)) {
                            // if user just created or joined
                            if ("create-party".equalsIgnoreCase(lastAction) || "join-party".equalsIgnoreCase(lastAction)) {
                                String passphrase = panel.getLastProposedPassphrase();
                                String localPlayer = (plugin.getClient().getLocalPlayer() != null)
                                        ? plugin.getClient().getLocalPlayer().getName()
                                        : null;
                                boolean externalSharing = plugin.getConfig().enableExternalSharing();


                                // set passphrase in the local manager so that we accept the next update :)
                                plugin.getPartyManager().setCurrentPartyPassphrase(passphrase);
                                // create local placeholder
                                plugin.getPartyManager().createParty(localPlayer, passphrase, externalSharing);


                                // *** Force server to broadcast final party data
                                JSONObject fetchPayload = new JSONObject();
                                fetchPayload.put("passphrase", passphrase);
                                fetchPayload.put("apiKey", plugin.getConfig().apiKey());
                                requestPartyUpdate(fetchPayload);
                            }

                            panel.getStatusLabel().setText("");
                            panel.getStatusLabel().setVisible(false);
                            panel.enableLeaveParty();
                            panel.updatePartyMembers();
                        }
                        else
                        {
                            // revert logic
                            plugin.getPartyManager().setCurrentPartyPassphrase(null);
                            plugin.getPartyManager().clearMembers();

                            panel.getStatusLabel().setText("Error: " + message);
                            panel.getStatusLabel().setVisible(true);

                            panel.getCreatePartyButton().setEnabled(true);
                            panel.getJoinPartyButton().setEnabled(true);
                            panel.getLeavePartyButton().setVisible(false);
                            panel.getScreenshotButton().setVisible(false);


                        }

                        lastAction = null;
                    }
                    catch (Exception ignored)
                    {
                        //logging not req
                    }
                });
            });




            socket.connect();
        }
        catch (URISyntaxException e)
        {
            log.error("Invalid Socket.IO URI: {}", e.getMessage(), e);
        }
    }



    public PartySocketIOClient(OsrsSplitPlugin plugin)
    {
        this.plugin = plugin;
    }

    public void requestPartyUpdate(JSONObject payload) {

        socket.emit("request_party_update", payload);
    }

    public void sendCreateParty(String passphrase, String rsn, int world, String apiKey, boolean externalSharing)
    {
        // Mark the action
        lastAction = "create-party";

        JSONObject payload = new JSONObject();
        payload.put("passphrase", passphrase);
        payload.put("rsn", rsn);
        payload.put("world", world);
        payload.put("apiKey", apiKey);
        payload.put("externalSharingEnabled", externalSharing);

        plugin.getPartyManagerPanel().setLastProposedPassphrase(passphrase);


        socket.emit("create-party", payload);
    }


    public void sendJoinParty(String passphrase, String rsn, int world, String apiKey, boolean externalSharing)
    {
        lastAction = "join-party";

        JSONObject payload = new JSONObject();
        payload.put("passphrase", passphrase);
        payload.put("rsn", rsn);
        payload.put("world", world);
        payload.put("apiKey", apiKey);
        payload.put("externalSharingEnabled", externalSharing);

        plugin.getPartyManagerPanel().setLastProposedPassphrase(passphrase);

        socket.emit("join-party", payload);
    }

    public void sendLeaveParty(String passphrase, String rsn)
    {
        lastAction = "leave-party";

        JSONObject payload = new JSONObject();
        payload.put("passphrase", passphrase);
        payload.put("rsn", rsn);


        socket.emit("leave-party", payload);
    }


    public void disconnect() {
        if (socket != null && socket.connected()) {
            socket.disconnect();

        } else {

        }
    }

    private void processPartyUpdate(JSONObject json)
    {
        SwingUtilities.invokeLater(() ->
        {
            try
            {
                String action = json.optString("action", "");
                String passphrase = json.optString("passphrase", "");
                String leader = json.optString("leader", null);
                JSONArray membersArray = json.optJSONArray("members");

                //  Grab local passphrase first
                String localPassphrase = plugin.getPartyManager().getCurrentPartyPassphrase();

                // If incoming passphrase doesn't match our local passphrase, ignore
                if (!passphrase.equals(localPassphrase))
                {
                    return;
                }

                // If "party_disband" => we disband the local party
                if ("party_disband".equals(action))
                {

                    plugin.getPartyManager().clearMembers();
                    plugin.getPartyManager().setCurrentPartyPassphrase(null);
                    plugin.getPartyManager().setLeader(null);

                    plugin.getPartyManagerPanel().getCreatePartyButton().setEnabled(true);
                    plugin.getPartyManagerPanel().getJoinPartyButton().setEnabled(true);
                    plugin.getPartyManagerPanel().getLeavePartyButton().setVisible(false);
                    plugin.getPartyManagerPanel().getScreenshotButton().setVisible(false);

                    plugin.getPartyManagerPanel().updatePartyMembers();
                    plugin.getPartyManagerPanel().updatePassphraseLabel("");
                    plugin.getPartyManagerPanel().getPassphraseLabel().setVisible(false);
                    return;
                }

                // --- Build updated members
                Map<String, PlayerInfo> updatedMembers = new HashMap<>();
                if (membersArray != null)
                {
                    for (int i = 0; i < membersArray.length(); i++)
                    {
                        JSONObject mem = membersArray.getJSONObject(i);
                        PlayerInfo pInfo = new PlayerInfo(
                                mem.getString("name"),
                                mem.optInt("world", -1),
                                mem.optInt("rank", -1),
                                mem.optBoolean("verified", false),
                                mem.optBoolean("confirmedSplit", false),
                                mem.optBoolean("externalSharingEnabled", false)
                        );
                        //pInfo.setExternalSharingEnabled(ext);
                        updatedMembers.put(pInfo.getName(), pInfo);
                    }
                }

                // If empty => disband
                if (updatedMembers.isEmpty())
                {

                    plugin.getPartyManager().clearMembers();
                    plugin.getPartyManager().setCurrentPartyPassphrase(null);
                    plugin.getPartyManager().setLeader(null);

                    plugin.getPartyManagerPanel().getCreatePartyButton().setEnabled(true);
                    plugin.getPartyManagerPanel().getJoinPartyButton().setEnabled(true);
                    plugin.getPartyManagerPanel().getLeavePartyButton().setVisible(false);
                    plugin.getPartyManagerPanel().getScreenshotButton().setVisible(false);

                    plugin.getPartyManagerPanel().updatePartyMembers();
                    plugin.getPartyManagerPanel().updatePassphraseLabel("");
                    plugin.getPartyManagerPanel().getPassphraseLabel().setVisible(false);
                    return;
                }

                // If local user not in updated => parted ways
                String localPlayer = (plugin.getClient().getLocalPlayer() != null)
                        ? plugin.getClient().getLocalPlayer().getName()
                        : null;

                if (localPlayer != null && !updatedMembers.containsKey(localPlayer))
                {

                    plugin.getPartyManager().clearMembers();
                    plugin.getPartyManager().setCurrentPartyPassphrase(null);
                    plugin.getPartyManager().setLeader(null);

                    plugin.getPartyManagerPanel().getCreatePartyButton().setEnabled(true);
                    plugin.getPartyManagerPanel().getJoinPartyButton().setEnabled(true);
                    plugin.getPartyManagerPanel().getLeavePartyButton().setVisible(false);
                    plugin.getPartyManagerPanel().getScreenshotButton().setVisible(false);

                    plugin.getPartyManagerPanel().updatePartyMembers();
                    plugin.getPartyManagerPanel().updatePassphraseLabel("");
                    plugin.getPartyManagerPanel().getPassphraseLabel().setVisible(false);
                    return;
                }

                // normal update => create or update the local membership
                plugin.getPartyManager().updateCurrentParty(passphrase, updatedMembers);
                plugin.getPartyManager().setLeader(leader);


                // Hide “Loading…” text, if any
                plugin.getPartyManagerPanel().getStatusLabel().setText("");
                plugin.getPartyManagerPanel().getStatusLabel().setVisible(false);

                // Show passphrase label & update UI
                plugin.getPartyManagerPanel().updatePassphraseLabel(passphrase);
                plugin.getPartyManagerPanel().getPassphraseLabel().setVisible(true);

                plugin.getPartyManagerPanel().updatePartyMembers();
            }
            catch (Exception ignored)
            {
                //logging not req
            }
        });
    }





    public void send(String event, String payload) {
        socket.emit(event, payload);

    }



}
