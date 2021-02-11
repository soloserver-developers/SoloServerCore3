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
import page.nafuchoco.soloservercore.database.PluginSettingsManager;
import page.nafuchoco.soloservercore.team.PlayersTeam;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class TeamCommand implements CommandExecutor, TabCompleter {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

    private final PlayersTable playersTable;
    private final PlayersTeamsTable teamsTable;
    private final PluginSettingsManager settingsManager;

    private final Map<UUID, UUID> invited;

    public TeamCommand(PlayersTable playersTable, PlayersTeamsTable teamsTable, PluginSettingsManager settingsManager) {
        this.playersTable = playersTable;
        this.teamsTable = teamsTable;
        this.settingsManager = settingsManager;
        invited = new HashMap<>();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 0) {
                // Show Team Status
                PlayerData playerData = playersTable.getPlayerData(player);
                UUID joinedTeam = playerData.getJoinedTeam();
                if (joinedTeam != null) {
                    PlayersTeam team = teamsTable.getPlayersTeam(joinedTeam);
                    sender.sendMessage(ChatColor.AQUA + "======== PlayersTeam Infomation ========");
                    StringBuilder builder = new StringBuilder();
                    if (team.getTeamName() == null)
                        builder.append("JoinedTeam: " + team.getId() + "\n");
                    else
                        builder.append("JoinedTeam: " + team.getTeamName() + "\n");
                    builder.append("TeamOwner: " + Bukkit.getOfflinePlayer(team.getOwner()).getName() + "\n");
                    builder.append("TeamMembers: \n" + team.getMembers().stream()
                            .map(m -> Bukkit.getOfflinePlayer(m).getName() + ChatColor.GRAY +
                                    " [" + dateFormat.format(Bukkit.getOfflinePlayer(m).getLastPlayed()) + "]")
                            .collect(Collectors.joining("\n")));
                    sender.sendMessage(builder.toString());
                } else {
                    sender.sendMessage(ChatColor.YELLOW + "[Teams] 所属しているチームがありません！");
                }
            } else if (!sender.hasPermission("soloservercore.team." + args[0])) {
                sender.sendMessage(ChatColor.RED + "You can't run this command because you don't have permission.");
            } else switch (args[0]) {
                case "create": {
                    // すでにチームに所属している場合は
                    PlayerData playerData = playersTable.getPlayerData(player);
                    UUID joinedTeam = playerData.getJoinedTeam();
                    if (joinedTeam != null) {
                        PlayersTeam team = teamsTable.getPlayersTeam(joinedTeam);
                        if (player.getUniqueId().equals(team.getOwner())) {
                            sender.sendMessage(ChatColor.RED + "[Teams] 既にあなたがオーナーのチームを持っています！");
                            break;
                        } else {
                            team.leaveTeam(player);
                            sender.sendMessage(ChatColor.GREEN + "[Teams] これまで入っていたチームから脱退しました。");
                        }
                    }

                    try {
                        UUID id = UUID.randomUUID();
                        teamsTable.registerTeam(id, (player).getUniqueId(), null, null);
                        playersTable.updateJoinedTeam((player).getUniqueId(), id);
                        sender.sendMessage(ChatColor.GREEN + "[Teams] あなたのチームが作成されました！\n" +
                                "/team invite [player] で他のプレイヤーを招待しましょう！");
                    } catch (SQLException throwables) {
                        sender.sendMessage(ChatColor.RED + "チームデータの保存に失敗しました。");
                        SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to save the team data.", throwables);
                    }
                }
                break;

                case "invite":
                    if (args.length >= 2) {
                        args = Arrays.copyOfRange(args, 1, args.length);
                        UUID teamId = teamsTable.searchTeamFromOwner((player).getUniqueId());
                        if (teamId != null) {
                            for (String arg : args) {
                                Player target = Bukkit.getPlayer(arg);
                                if (target != null) {
                                    if (target.equals(player)) {
                                        sender.sendMessage(ChatColor.RED +
                                                "[Teams] 自分自身を招待することはできません！");
                                    } else if (player.getWorld().equals(target.getWorld())) {
                                        invited.put(target.getUniqueId(), teamId);
                                        target.sendMessage(ChatColor.GREEN + "[Teams]" + player.getDisplayName() +
                                                " さんからチームに招待されました。\n" +
                                                "参加するには /team accept を実行してください。");
                                    } else {
                                        sender.sendMessage(ChatColor.RED + "[Teams] 招待するプレイヤーは同じワールドにいる必要があります。");
                                    }
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
                    UUID id = invited.remove((player).getUniqueId());
                    if (id != null) {
                        // すでにチームに所属している場合は
                        PlayerData playerData = playersTable.getPlayerData(player);
                        UUID joinedTeam = playerData.getJoinedTeam();
                        if (joinedTeam != null) {
                            teamsTable.getPlayersTeam(joinedTeam).leaveTeam(player);
                            sender.sendMessage(ChatColor.GREEN + "[Teams] これまで入っていたチームから脱退しました。");
                        }

                        PlayersTeam team = teamsTable.getPlayersTeam(id);
                        team.joinTeam(player);
                    } else {
                        sender.sendMessage(ChatColor.YELLOW + "[Teams] あなたはまだ招待を受け取っていません！");
                    }
                    break;

                case "leave": {
                    PlayerData playerData = playersTable.getPlayerData(player);
                    UUID joinedTeam = playerData.getJoinedTeam();
                    if (joinedTeam != null) {
                        PlayersTeam team = teamsTable.getPlayersTeam(joinedTeam);
                        if (settingsManager.isTeamSpawnCollect()
                                && team.getOwner().equals(player.getUniqueId())
                                && !team.getMembers().isEmpty()) {
                            invited.put(joinedTeam, null);
                            sender.sendMessage(ChatColor.YELLOW + "[Teams] あなたはチームオーナーです。");
                            sender.sendMessage(ChatColor.YELLOW + "チームを解散すると他の所属プレイヤーのスポーンポイントが離散してしまいます。");
                            sender.sendMessage(ChatColor.YELLOW + "本当に解散しますか？解散する場合は /team confirm を実行してください。");
                        } else {
                            team.leaveTeam(player);
                            sender.sendMessage(ChatColor.GREEN + "[Teams] チームから脱退しました。");
                        }

                    }
                }
                break;

                case "confirm": {
                    PlayerData playerData = playersTable.getPlayerData(player);
                    UUID joinedTeam = playerData.getJoinedTeam();
                    if (joinedTeam != null && invited.remove(joinedTeam) != null) {
                        PlayersTeam team = teamsTable.getPlayersTeam(joinedTeam);
                        team.leaveTeam(player);
                        sender.sendMessage(ChatColor.GREEN + "[Teams] チームから脱退しました。");
                    }
                }
                break;

                case "transfer":
                    if (args.length >= 2) {
                        UUID teamId = teamsTable.searchTeamFromOwner((player).getUniqueId());
                        if (teamId != null) {
                            Player target = Bukkit.getPlayer(args[1]);
                            if (target != null && !player.equals(target)) {
                                PlayerData playerData = playersTable.getPlayerData(target);
                                UUID joinedTeam = playerData.getJoinedTeam();
                                if (teamId.equals(joinedTeam)) {
                                    PlayersTeam original = teamsTable.getPlayersTeam(teamId);
                                    List<UUID> members = original.getMembers();
                                    try {
                                        teamsTable.deleteTeam(teamId);
                                        members.remove(target.getUniqueId());
                                        members.add(player.getUniqueId());
                                        teamsTable.registerTeam(teamId, target.getUniqueId(), original.getTeamName(), members);

                                        target.sendMessage(ChatColor.GREEN + "[Teams] チームのオーナが" + target.getDisplayName() + "に譲渡されました。");
                                        members.forEach(uuid -> {
                                            Player member = Bukkit.getPlayer(uuid);
                                            if (member != null)
                                                member.sendMessage(ChatColor.GREEN + "[Teams] チームのオーナが" + target.getDisplayName() + "に譲渡されました。");
                                        });
                                    } catch (SQLException throwables) {
                                        sender.sendMessage(ChatColor.RED + "チームデータの保存に失敗しました。");
                                        SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to save the team data.", throwables);
                                    }
                                }
                            }
                        } else {
                            sender.sendMessage(ChatColor.YELLOW + "[Teams] オーナーを譲渡するためには自分がチームのオーナーである必要があります。");
                        }
                    } else {
                        sender.sendMessage(ChatColor.YELLOW + "[Teams] オーナーを譲渡するプレイヤーを指定してください。");
                    }
                    break;

                case "name":
                    UUID teamId = teamsTable.searchTeamFromOwner((player).getUniqueId());
                    if (teamId != null) {
                        PlayersTeam playersTeam = teamsTable.getPlayersTeam(teamId);
                        if (args.length >= 2)
                            playersTeam.updateTeamName(player, args[1]);
                        else
                            playersTeam.updateTeamName(player, null);
                        sender.sendMessage(ChatColor.GREEN + "[Teams] チーム名を変更が変更されました。");
                    } else {
                        sender.sendMessage(ChatColor.YELLOW + "[Teams] チーム名を変更するには自分がオーナーのチームに所属している必要があります。");
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
        if (args.length <= 1)
            return Arrays.asList("create", "invite", "accept", "leave", "confirm", "transfer", "name");
        else if (args[0].equals("invite"))
            return Bukkit.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        else
            return null;
    }
}
