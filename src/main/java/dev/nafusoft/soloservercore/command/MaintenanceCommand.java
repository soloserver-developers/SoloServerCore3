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

package dev.nafusoft.soloservercore.command;

import dev.nafusoft.soloservercore.SoloServerApi;
import dev.nafusoft.soloservercore.SoloServerCore;
import dev.nafusoft.soloservercore.database.PlayersTable;
import dev.nafusoft.soloservercore.database.PlayersTeamsTable;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class MaintenanceCommand implements CommandExecutor, TabCompleter {
    private final PlayersTable playersTable;
    private final PlayersTeamsTable teamsTable;

    public MaintenanceCommand(PlayersTable playersTable, PlayersTeamsTable teamsTable) {
        this.playersTable = playersTable;
        this.teamsTable = teamsTable;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        var soloServerApi = SoloServerApi.getInstance();

        if (!sender.hasPermission("soloservercore.maintenance")) {
            sender.sendMessage(ChatColor.RED + "You can't run this command because you don't have permission.");
        } else if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Insufficient arguments to execute the command.");
        } else switch (args[0]) {
            case "clear" -> {
                try {
                    val playerId = UUID.fromString(args[1]);
                    val target = soloServerApi.getOfflineSSCPlayer(playerId);
                    if (target != null) {
                        val targetPlayer = Bukkit.getPlayer(playerId);
                        if (targetPlayer != null)
                            targetPlayer.kickPlayer("[SSC] The player data has been deleted by the administrator.");

                        // チームに所属している場合は脱退、オーナーの場合はチームの削除
                        if (target.getJoinedTeam() != null) {
                            val joinedTeam = soloServerApi.getPlayersTeam(target.getJoinedTeamId());
                            if (joinedTeam.getOwner().equals(target.getId())) {
                                teamsTable.deleteTeam(joinedTeam.getId());
                                joinedTeam.getMembers().forEach(m -> {
                                            try {
                                                playersTable.updateJoinedTeam(m, null);
                                            } catch (SQLException e) {
                                                SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to update the team data.", e);
                                            }
                                        }
                                );
                            } else {
                                val members = joinedTeam.getMembers();
                                members.remove(playerId);
                                teamsTable.updateMembers(joinedTeam.getId(), members);
                            }
                        }
                        playersTable.deletePlayer(target);
                        sender.sendMessage(ChatColor.GREEN + "Player data has been deleted.");
                    }
                } catch (SQLException e) {
                    SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to update the team data.", e);
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ChatColor.RED + "You must specify the exact UUID of the player whose data you want to delete.");
                }
            }
            case "show" -> {
                val offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                if (offlinePlayer.hasPlayedBefore()) {
                    val player = soloServerApi.getOfflineSSCPlayer(offlinePlayer.getUniqueId());
                    if (player != null) {
                        String sb = ChatColor.AQUA + "====== Maintenance Player Information ======\n" + ChatColor.RESET +
                                "UUID: " + player.getId() + "\n" +
                                "SpawnLocation: " + player.getSpawnLocation() + "\n" +
                                "JoinedTeam: " + player.getJoinedTeamId() + "\n" +
                                "FirstJoinDate: " + offlinePlayer.getFirstPlayed();
                        sender.sendMessage(sb);
                    } else {
                        sender.sendMessage("[SSC] This player has found data on the server, but SoloServerCore data does not exist.");
                    }
                } else {
                    sender.sendMessage("[SSC] This player has never played on this server.");
                }
            }
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return null;
    }
}
