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
import page.nafuchoco.soloservercore.SoloServerCore;
import page.nafuchoco.soloservercore.database.PlayerData;
import page.nafuchoco.soloservercore.database.PlayersTable;
import page.nafuchoco.soloservercore.database.PlayersTeamsTable;
import page.nafuchoco.soloservercore.team.PlayersTeam;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class TeamCommand implements CommandExecutor, TabCompleter {
    private final PlayersTable playersTable;
    private final PlayersTeamsTable teamsTable;

    private final Map<UUID, UUID> invited;

    public TeamCommand(PlayersTable playersTable, PlayersTeamsTable teamsTable) {
        this.playersTable = playersTable;
        this.teamsTable = teamsTable;
        invited = new HashMap<>();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            if (args.length == 0) {
                // Show Team Status
            }
            if (!sender.hasPermission("soloservercore.team." + args[0])) {
                sender.sendMessage(ChatColor.RED + "You can't run this command because you don't have permission.");
            } else switch (args[0]) {
                case "create":
                    try {
                        UUID id = UUID.randomUUID();
                        teamsTable.registerTeam(id, ((Player) sender).getUniqueId(), null);
                        playersTable.updateJoinedTeam(((Player) sender).getUniqueId(), id);
                        sender.sendMessage(ChatColor.GREEN + "[Teams] あなたのチームが作成されました！\n" +
                                "/team invite [player] で他のプレイヤーを招待しましょう！");
                    } catch (SQLException throwables) {
                        sender.sendMessage(ChatColor.RED + "チームデータの保存に失敗しました。");
                        SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to save the team data.", throwables);
                    }
                    break;

                case "invite":
                    if (args.length >= 2) {
                        args = Arrays.copyOfRange(args, 1, args.length);
                        UUID teamId = teamsTable.searchTeamFromOwner(((Player) sender).getUniqueId());
                        if (teamId != null) {
                            for (String arg : args) {
                                Player player = Bukkit.getPlayer(arg);
                                if (player != null) {
                                    invited.put(player.getUniqueId(), teamId);
                                    player.sendMessage(ChatColor.GREEN + "[Teams]" + ((Player) sender).getDisplayName() +
                                            " さんからチームに招待されました。\n" +
                                            "参加するには /team accept を実行してください。");
                                }
                            }
                        } else {
                            sender.sendMessage(ChatColor.YELLOW + "[Teams] チームに招待するためには自分がオーナーのチームに所属している必要があります。\n" +
                                    "チームを作成するには /team create を実行してください。");
                        }
                    } else {
                        sender.sendMessage(ChatColor.YELLOW + "[Teams] 招待するプレイヤーを指定してください。");
                    }
                    break;

                case "accept":
                    UUID id = invited.remove(((Player) sender).getUniqueId());
                    if (id != null) {
                        PlayersTeam team = teamsTable.getPlayersTeam(id);
                        team.joinTeam((Player) sender);
                    } else {
                        sender.sendMessage(ChatColor.YELLOW + "[Teams] あなたはまだ招待を受け取っていません！");
                    }
                    break;

                case "leave":
                    PlayerData playerData = playersTable.getPlayerData((Player) sender);
                    UUID joinedTeam = playerData.getJoinedTeam();
                    if (joinedTeam != null) {
                        PlayersTeam team = teamsTable.getPlayersTeam(joinedTeam);
                        team.leaveTeam((Player) sender);
                        sender.sendMessage(ChatColor.GREEN + "[Teams] チームから脱退しました。");
                    }
                    break;
            }
        } else {
            Bukkit.getLogger().info("This command must be executed in-game.");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1)
            return Arrays.asList("create", "invite", "accept", "leave");
        return null;
    }
}
