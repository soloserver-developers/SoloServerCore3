/*
 * Copyright 2022 Nafu Satsuki
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

package dev.nafusoft.soloservercore.listener.internal;

import dev.nafusoft.soloservercore.MessageManager;
import dev.nafusoft.soloservercore.SoloServerApi;
import dev.nafusoft.soloservercore.SoloServerCore;
import dev.nafusoft.soloservercore.data.PlayersTeam;
import dev.nafusoft.soloservercore.database.MessagesTable;
import dev.nafusoft.soloservercore.database.PlayersTable;
import dev.nafusoft.soloservercore.database.PlayersTeamsTable;
import dev.nafusoft.soloservercore.database.PluginSettingsManager;
import dev.nafusoft.soloservercore.event.team.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;

public class PlayersTeamEventListener implements Listener {
    private final PlayersTable playersTable;
    private final PlayersTeamsTable teamsTable;
    private final PluginSettingsManager settingsManager;
    private final MessagesTable messagesTable;

    public PlayersTeamEventListener(
            PlayersTable playersTable,
            PlayersTeamsTable teamsTable,
            PluginSettingsManager settingsManager,
            MessagesTable messagesTable) {
        this.playersTable = playersTable;
        this.teamsTable = teamsTable;
        this.settingsManager = settingsManager;
        this.messagesTable = messagesTable;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayersTeamCreateEvent(PlayersTeamCreateEvent event) {
        if (!event.isCancelled()) {
            try {
                teamsTable.registerTeam(event.getPlayersTeam());
                playersTable.updateJoinedTeam(event.getPlayer().getId(), event.getPlayersTeam().getId());
                event.getPlayer().setJoinedTeam(event.getPlayersTeam());
            } catch (SQLException e) {
                SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to save the team data.", e);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayersTeamJoinEvent(PlayersTeamJoinEvent event) {
        if (!event.isCancelled()) {
            try {
                playersTable.updateJoinedTeam(event.getPlayer().getId(), event.getPlayersTeam().getId());
                teamsTable.updateMembers(event.getPlayersTeam().getId(), event.getPlayersTeam().getMembers());
                event.getPlayer().setJoinedTeam(event.getPlayersTeam());
            } catch (SQLException e) {
                SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to update the team data.", e);
                event.setCancelled(true);
                return;
            }

            var owner = Bukkit.getPlayer(event.getPlayersTeam().getOwner());
            if (owner != null) {
                owner.sendMessage(MessageManager.format(SoloServerCore.getMessage(owner, "teams.join.announce.owner"), event.getBukkitPlayer().getDisplayName()));
                event.getBukkitPlayer().showPlayer(SoloServerCore.getInstance(), owner);
                owner.showPlayer(SoloServerCore.getInstance(), event.getBukkitPlayer());
            }
            event.getPlayersTeam().getMembers().forEach(uuid -> {
                var player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.sendMessage(MessageManager.format(SoloServerCore.getMessage(player, "teams.join.announce"), event.getBukkitPlayer().getDisplayName()));
                    event.getBukkitPlayer().showPlayer(SoloServerCore.getInstance(), player);
                    player.showPlayer(SoloServerCore.getInstance(), event.getBukkitPlayer());
                }
            });

            if (settingsManager.isTeamSpawnCollect())
                event.getBukkitPlayer().teleport(SoloServerApi.getInstance().getSpawn(event.getPlayersTeam().getOwner()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayersTeamLeaveEvent(PlayersTeamLeaveEvent event) {
        if (!event.isCancelled()) {
            try {
                playersTable.updateJoinedTeam(event.getPlayer().getId(), null);
                teamsTable.updateMembers(event.getPlayersTeam().getId(), event.getPlayersTeam().getMembers());
                event.getPlayer().setJoinedTeam(event.getPlayersTeam());
            } catch (SQLException e) {
                SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to update the team data.", e);
                event.setCancelled(true);
                return;
            }

            var owner = Bukkit.getPlayer(event.getPlayersTeam().getOwner());
            if (owner != null) {
                owner.sendMessage(MessageManager.format(SoloServerCore.getMessage(owner, "teams.leave.announce.owner"), event.getBukkitPlayer().getDisplayName()));
                event.getBukkitPlayer().hidePlayer(SoloServerCore.getInstance(), owner);
                owner.hidePlayer(SoloServerCore.getInstance(), event.getBukkitPlayer());
            }
            event.getPlayersTeam().getMembers().forEach(uuid -> {
                var player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.sendMessage(MessageManager.format(SoloServerCore.getMessage(player, "teams.leave.announce"), event.getBukkitPlayer().getDisplayName()));
                    event.getBukkitPlayer().hidePlayer(SoloServerCore.getInstance(), player);
                    player.hidePlayer(SoloServerCore.getInstance(), event.getBukkitPlayer());
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayersTeamMessageCreate(PlayersTeamMessageCreateEvent event) {
        if (!event.isCancelled()) {
            try {
                messagesTable.registerMessage(event.getCreateTeamMessage());
            } catch (SQLException e) {
                SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to update the team data.", e);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayersTeamMessageDelete(PlayersTeamMessageDeleteEvent event) {
        if (!event.isCancelled()) {
            try {
                messagesTable.deleteMessage(event.getDeleteMessage().getId());
            } catch (SQLException e) {
                SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to update the team data.", e);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayersTeamDisappearanceEvent(PlayersTeamDisappearanceEvent event) {
        try {
            playersTable.updateJoinedTeam(event.getPlayer().getId(), null);
            event.getPlayersTeam().getMembers().forEach(uuid -> {
                try {
                    playersTable.updateJoinedTeam(uuid, null);
                    event.getPlayer().setJoinedTeam(null);
                    var player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        player.sendMessage(ChatColor.RED + "[Teams] オーナーがチームから脱退したためチームが解散されました。");
                        event.getBukkitPlayer().hidePlayer(SoloServerCore.getInstance(), player);
                        player.hidePlayer(SoloServerCore.getInstance(), event.getBukkitPlayer());
                        event.getPlayersTeam().getMembers().forEach(member -> {
                            if (!member.equals(uuid)) {
                                var memberPlayer = Bukkit.getPlayer(member);
                                if (memberPlayer != null) {
                                    player.hidePlayer(SoloServerCore.getInstance(), memberPlayer);
                                    memberPlayer.hidePlayer(SoloServerCore.getInstance(), player);
                                }
                            }
                        });
                    }
                } catch (SQLException e) {
                    SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to update the team data.", e);
                }
            });
            teamsTable.deleteTeam(event.getPlayersTeam().getId());
            messagesTable.deleteAllMessages(event.getPlayersTeam().getId());
        } catch (SQLException e) {
            SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to update the team data.", e);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayersTeamStatusUpdateEvent(PlayersTeamStatusUpdateEvent event) {
        try {
            switch (event.getState()) {
                case NAME ->
                        teamsTable.updateTeamName(event.getPlayersTeam().getId(), event.getPlayersTeam().getTeamName());
                case OWNER -> {
                    var ownerPlayer = Bukkit.getPlayer(((PlayersTeam) event.getAfter()).getOwner());
                    teamsTable.updateTeamOwner(event.getPlayersTeam().getId(), ownerPlayer.getUniqueId());
                    ownerPlayer.sendMessage(MessageManager.format(SoloServerCore.getMessage(ownerPlayer, "teams.transfer.announce"), ownerPlayer.getDisplayName()));
                    event.getPlayersTeam().getMembers().stream()
                            .map(Bukkit::getPlayer)
                            .filter(Objects::nonNull)
                            .forEach(member ->
                                    member.sendMessage(MessageManager.format(SoloServerCore.getMessage(member, "teams.transfer.announce"), ownerPlayer.getDisplayName())));
                }
            }
        } catch (SQLException e) {
            SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to update the team data.", e);
        }
    }
}
