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

package page.nafuchoco.soloservercore.listener.internal;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import page.nafuchoco.soloservercore.SoloServerCore;
import page.nafuchoco.soloservercore.SpawnPointLoader;
import page.nafuchoco.soloservercore.database.PlayersTable;
import page.nafuchoco.soloservercore.database.PlayersTeamsTable;
import page.nafuchoco.soloservercore.database.PluginSettingsManager;
import page.nafuchoco.soloservercore.event.PlayersTeamDisappearanceEvent;
import page.nafuchoco.soloservercore.event.PlayersTeamJoinEvent;
import page.nafuchoco.soloservercore.event.PlayersTeamLeaveEvent;

import java.sql.SQLException;
import java.util.logging.Level;

public class PlayersTeamEventListener implements Listener {
    private final PlayersTable playersTable;
    private final PlayersTeamsTable teamsTable;
    private final PluginSettingsManager settingsManager;
    private final SpawnPointLoader loader;

    public PlayersTeamEventListener(
            PlayersTable playersTable,
            PlayersTeamsTable teamsTable,
            PluginSettingsManager settingsManager,
            SpawnPointLoader loader) {
        this.playersTable = playersTable;
        this.teamsTable = teamsTable;
        this.settingsManager = settingsManager;
        this.loader = loader;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayersTeamJoinEvent(PlayersTeamJoinEvent event) {
        if (!event.isCancelled()) {
            try {
                playersTable.updateJoinedTeam(event.getPlayer().getUniqueId(), event.getPlayersTeam().getId());
                teamsTable.updateMembers(event.getPlayersTeam().getId(), event.getPlayersTeam().getMembers());
            } catch (SQLException throwables) {
                SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to update the team data.", throwables);
                event.setCancelled(true);
                return;
            }

            Player owner = Bukkit.getPlayer(event.getPlayersTeam().getOwner());
            if (owner != null) {
                owner.sendMessage(ChatColor.GREEN + "[Teams] あなたのチームに" + event.getPlayer().getDisplayName() + "が加わりました！");
                event.getPlayer().showPlayer(SoloServerCore.getInstance(), owner);
                owner.showPlayer(SoloServerCore.getInstance(), event.getPlayer());
            }
            event.getPlayersTeam().getMembers().forEach(uuid -> {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.sendMessage(ChatColor.GREEN + "[Teams] " + event.getPlayer().getDisplayName() + "がチームに加わりました！");
                    event.getPlayer().showPlayer(SoloServerCore.getInstance(), player);
                    player.showPlayer(SoloServerCore.getInstance(), event.getPlayer());
                }
            });

            if (settingsManager.isTeamSpawnCollect())
                event.getPlayer().teleport(loader.getSpawn(event.getPlayersTeam().getOwner()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayersTeamLeaveEvent(PlayersTeamLeaveEvent event) {
        if (!event.isCancelled()) {
            try {
                playersTable.updateJoinedTeam(event.getPlayer().getUniqueId(), null);
                teamsTable.updateMembers(event.getPlayersTeam().getId(), event.getPlayersTeam().getMembers());
            } catch (SQLException throwables) {
                SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to update the team data.", throwables);
                event.setCancelled(true);
                return;
            }

            Player owner = Bukkit.getPlayer(event.getPlayersTeam().getOwner());
            if (owner != null) {
                owner.sendMessage(ChatColor.RED + "[Teams] あなたのチームから" + event.getPlayer().getDisplayName() + "が加脱退しました。");
                event.getPlayer().hidePlayer(SoloServerCore.getInstance(), owner);
                owner.hidePlayer(SoloServerCore.getInstance(), event.getPlayer());
            }
            event.getPlayersTeam().getMembers().forEach(uuid -> {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.sendMessage(ChatColor.RED + "[Teams] " + event.getPlayer().getDisplayName() + "がチームから脱退しました。");
                    event.getPlayer().hidePlayer(SoloServerCore.getInstance(), player);
                    player.hidePlayer(SoloServerCore.getInstance(), event.getPlayer());
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayersTeamDisappearanceEvent(PlayersTeamDisappearanceEvent event) {
        try {
            playersTable.updateJoinedTeam(event.getPlayer().getUniqueId(), null);
            event.getPlayersTeam().getMembers().forEach(uuid -> {
                try {
                    playersTable.updateJoinedTeam(uuid, null);
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        player.sendMessage(ChatColor.RED + "[Teams] オーナーがチームから脱退したためチームが解散されました。");
                        event.getPlayer().hidePlayer(SoloServerCore.getInstance(), player);
                        player.hidePlayer(SoloServerCore.getInstance(), event.getPlayer());
                        event.getPlayersTeam().getMembers().forEach(member -> {
                            if (!member.equals(uuid)) {
                                Player memberPlayer = Bukkit.getPlayer(member);
                                if (memberPlayer != null) {
                                    player.hidePlayer(SoloServerCore.getInstance(), memberPlayer);
                                    memberPlayer.hidePlayer(SoloServerCore.getInstance(), player);
                                }
                            }
                        });
                    }
                } catch (SQLException throwables) {
                    SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to update the team data.", throwables);
                }
            });
            teamsTable.deleteTeam(event.getPlayersTeam().getId());
        } catch (SQLException throwables) {
            SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to update the team data.", throwables);
        }
    }
}
