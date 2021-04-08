/*
 * Copyright 2021 NAFU_at
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

package page.nafuchoco.soloservercore.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import page.nafuchoco.soloservercore.SoloServerApi;
import page.nafuchoco.soloservercore.SoloServerCore;
import page.nafuchoco.soloservercore.data.OfflineSSCPlayer;
import page.nafuchoco.soloservercore.data.PlayersTeam;
import page.nafuchoco.soloservercore.database.PlayersTable;
import page.nafuchoco.soloservercore.database.PlayersTeamsTable;

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
        // TODO: 2021/02/15 いずれ。
        SoloServerApi soloServerApi = SoloServerApi.getInstance();
        // Map<CommandSender, Object> confirmWaitList = new HashMap<>();

        if (!sender.hasPermission("soloservercore.maintenance")) {
            sender.sendMessage(ChatColor.RED + "You can't run this command because you don't have permission.");
        } else if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Insufficient arguments to execute the command.");
        } else switch (args[0]) {
            case "clear":
                try {
                    UUID playerId = UUID.fromString(args[1]);
                    OfflineSSCPlayer target = soloServerApi.getOfflineSSCPlayer(playerId);
                    if (target != null) {
                        Player targetPlayer = Bukkit.getPlayer(playerId);
                        if (targetPlayer != null)
                            targetPlayer.kickPlayer("[SSC] The player data has been deleted by the administrator.");
                        try {
                            // チームに所属している場合は脱退、オーナーの場合はチームの削除
                            if (target.getJoinedTeam() != null) {
                                PlayersTeam joinedTeam = soloServerApi.getPlayersTeam(target.getJoinedTeamId());
                                if (joinedTeam.getOwner().equals(target.getId())) {
                                    teamsTable.deleteTeam(joinedTeam.getId());
                                    if (!joinedTeam.getMembers().isEmpty()) {
                                        joinedTeam.getMembers().forEach(m -> {
                                                    try {
                                                        playersTable.updateJoinedTeam(m, null);
                                                    } catch (SQLException e) {
                                                        SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to update the team data.", e);
                                                    }
                                                }
                                        );
                                    }
                                } else {
                                    List<UUID> members = joinedTeam.getMembers();
                                    members.remove(playerId);
                                    teamsTable.updateMembers(joinedTeam.getId(), members);
                                }
                            }
                            playersTable.deletePlayer(target);
                            sender.sendMessage(ChatColor.GREEN + "Player data has been deleted.");
                        } catch (SQLException e) {
                            SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to update the team data.", e);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ChatColor.RED + "You must specify the exact UUID of the player whose data you want to delete.");
                }
                break;

            default:
                break;
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return null;
    }
}
