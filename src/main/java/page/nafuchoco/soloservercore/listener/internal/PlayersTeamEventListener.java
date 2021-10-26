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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import page.nafuchoco.soloservercore.SoloServerCore;
import page.nafuchoco.soloservercore.SpawnPointLoader;
import page.nafuchoco.soloservercore.data.PlayersTeam;
import page.nafuchoco.soloservercore.database.MessagesTable;
import page.nafuchoco.soloservercore.database.PlayersTable;
import page.nafuchoco.soloservercore.database.PlayersTeamsTable;
import page.nafuchoco.soloservercore.database.PluginSettingsManager;
import page.nafuchoco.soloservercore.event.team.*;

import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;

public class PlayersTeamEventListener implements Listener {
    private final PlayersTable playersTable;
    private final PlayersTeamsTable teamsTable;
    private final PluginSettingsManager settingsManager;
    private final MessagesTable messagesTable;
    private final SpawnPointLoader loader;

    public PlayersTeamEventListener(
            PlayersTable playersTable,
            PlayersTeamsTable teamsTable,
            PluginSettingsManager settingsManager,
            MessagesTable messagesTable, SpawnPointLoader loader) {
        this.playersTable = playersTable;
        this.teamsTable = teamsTable;
        this.settingsManager = settingsManager;
        this.messagesTable = messagesTable;
        this.loader = loader;
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
                owner.sendMessage(ChatColor.GREEN + "[Teams] あなたのチームに" + event.getBukkitPlayer().getDisplayName() + "が加わりました！");
                event.getBukkitPlayer().showPlayer(SoloServerCore.getInstance(), owner);
                owner.showPlayer(SoloServerCore.getInstance(), event.getBukkitPlayer());
            }
            event.getPlayersTeam().getMembers().forEach(uuid -> {
                var player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.sendMessage(ChatColor.GREEN + "[Teams] " + event.getBukkitPlayer().getDisplayName() + "がチームに加わりました！");
                    event.getBukkitPlayer().showPlayer(SoloServerCore.getInstance(), player);
                    player.showPlayer(SoloServerCore.getInstance(), event.getBukkitPlayer());
                }
            });

            if (settingsManager.isTeamSpawnCollect())
                event.getBukkitPlayer().teleport(loader.getSpawn(event.getPlayersTeam().getOwner()));
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
                owner.sendMessage(ChatColor.RED + "[Teams] あなたのチームから" + event.getBukkitPlayer().getDisplayName() + "が脱退しました。");
                event.getBukkitPlayer().hidePlayer(SoloServerCore.getInstance(), owner);
                owner.hidePlayer(SoloServerCore.getInstance(), event.getBukkitPlayer());
            }
            event.getPlayersTeam().getMembers().forEach(uuid -> {
                var player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.sendMessage(ChatColor.RED + "[Teams] " + event.getBukkitPlayer().getDisplayName() + "がチームから脱退しました。");
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
                case NAME -> teamsTable.updateTeamName(event.getPlayersTeam().getId(), event.getPlayersTeam().getTeamName());
                case OWNER -> {
                    var ownerPlayer = Bukkit.getPlayer(((PlayersTeam) event.getAfter()).getOwner());
                    teamsTable.updateTeamOwner(event.getPlayersTeam().getId(), ownerPlayer.getUniqueId());
                    ownerPlayer.sendMessage(ChatColor.GREEN + "[Teams] チームのオーナが" + ownerPlayer.getDisplayName() + "に譲渡されました。");
                    event.getPlayersTeam().getMembers().stream()
                            .map(Bukkit::getPlayer)
                            .filter(Objects::nonNull)
                            .forEach(member ->
                                    member.sendMessage(ChatColor.GREEN + "[Teams] チームのオーナが" + ownerPlayer.getDisplayName() + "に譲渡されました。"));
                }
            }
        } catch (SQLException e) {
            SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to update the team data.", e);
        }
    }
}
