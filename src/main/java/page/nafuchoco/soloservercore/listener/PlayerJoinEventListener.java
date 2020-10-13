/*
 * Copyright 2020 NAFU_at
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package page.nafuchoco.soloservercore.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import page.nafuchoco.soloservercore.SoloServerCore;
import page.nafuchoco.soloservercore.database.PlayerAndTeamsBridge;
import page.nafuchoco.soloservercore.database.PlayerData;
import page.nafuchoco.soloservercore.database.PlayersTable;
import page.nafuchoco.soloservercore.database.TeamsPlayerData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerJoinEventListener implements Listener {
    private final PlayersTable playersTable;
    private final PlayerAndTeamsBridge playerAndTeamsBridge;

    public PlayerJoinEventListener(PlayersTable playersTable, PlayerAndTeamsBridge playerAndTeamsBridge) {
        this.playersTable = playersTable;
        this.playerAndTeamsBridge = playerAndTeamsBridge;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        event.setJoinMessage("");

        if (!event.getPlayer().hasPlayedBefore()) {
            PlayerData playerData = playersTable.getPlayerData(event.getPlayer());
            event.getPlayer().teleport(playerData.getSpawnLocationLocation());
        }

        TeamsPlayerData teamsPlayerData = playerAndTeamsBridge.getPlayerData(event.getPlayer());
        List<UUID> bypass;
        if (teamsPlayerData != null) {
            bypass = teamsPlayerData.getMembers();
            bypass.add(teamsPlayerData.getTeamOwner());
        } else {
            bypass = new ArrayList<>();
        }

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        players.forEach(player -> {
            if (!player.equals(event.getPlayer()) && !bypass.contains(player.getUniqueId())) {
                event.getPlayer().hidePlayer(SoloServerCore.getInstance(), player);
                player.hidePlayer(SoloServerCore.getInstance(), event.getPlayer());
            }
        });
    }
}
