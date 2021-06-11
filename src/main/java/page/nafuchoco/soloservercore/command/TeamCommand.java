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

import lombok.val;
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
import page.nafuchoco.soloservercore.data.PlayersTeam;
import page.nafuchoco.soloservercore.database.PluginSettingsManager;
import page.nafuchoco.soloservercore.event.PlayersTeamCreateEvent;
import page.nafuchoco.soloservercore.event.PlayersTeamStatusUpdateEvent;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class TeamCommand implements CommandExecutor, TabCompleter {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

    private final PluginSettingsManager settingsManager;
    private final Map<UUID, PlayersTeam> invited;

    public TeamCommand(PluginSettingsManager settingsManager) {
        this.settingsManager = settingsManager;
        invited = new HashMap<>();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            val player = (Player) sender;
            val soloServerApi = SoloServerApi.getInstance();
            if (args.length == 0) {
                // Show Team Status
                val sscPlayer = soloServerApi.getSSCPlayer(player);
                if (sscPlayer.getJoinedTeam() != null) {
                    val team = sscPlayer.getJoinedTeam();
                    sender.sendMessage(ChatColor.AQUA + "======== PlayersTeam Infomation ========");
                    val builder = new StringBuilder();
                    if (team.getTeamName() == null)
                        builder.append("JoinedTeam: " + team.getId() + "\n");
                    else
                        builder.append("JoinedTeam: " + team.getTeamName() + "\n");
                    builder.append("TeamOwner: " + Bukkit.getOfflinePlayer(team.getOwner()).getName() + ChatColor.GRAY +
                            " [" + dateFormat.format(Bukkit.getOfflinePlayer(team.getOwner()).getLastPlayed()) + "]" + "\n");
                    builder.append(ChatColor.RESET + "TeamMembers: \n" + team.getMembers().stream()
                            .map(u -> Bukkit.getOfflinePlayer(u))
                            .map(p -> p.getName() + ChatColor.GRAY +
                                    " [" + dateFormat.format(p.getLastPlayed()) + "]" + ChatColor.WHITE)
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
                    val sscPlayer = soloServerApi.getSSCPlayer(player);
                    if (sscPlayer.getJoinedTeam() != null) {
                        val team = sscPlayer.getJoinedTeam();
                        if (player.getUniqueId().equals(team.getOwner())) {
                            sender.sendMessage(ChatColor.RED + "[Teams] 既にあなたがオーナーのチームを持っています！");
                            break;
                        } else {
                            team.leaveTeam(player);
                            sender.sendMessage(ChatColor.GREEN + "[Teams] これまで入っていたチームから脱退しました。");
                        }
                    }

                    val id = UUID.randomUUID();
                    val createEvent = new PlayersTeamCreateEvent(new PlayersTeam(id, (player).getUniqueId()), player);
                    Bukkit.getServer().getPluginManager().callEvent(createEvent);
                    if (!createEvent.isCancelled())
                        sender.sendMessage(ChatColor.GREEN + "[Teams] あなたのチームが作成されました！\n" +
                                "/team invite [player] で他のプレイヤーを招待しましょう！");
                    else
                        sender.sendMessage(ChatColor.RED + "チームの作成に失敗しました。");
                }
                break;

                case "invite":
                    if (args.length >= 2) {
                        args = Arrays.copyOfRange(args, 1, args.length);
                        val playersTeam = soloServerApi.getPlayersTeam(player);
                        if (playersTeam != null) {
                            Arrays.stream(args)
                                    .map(Bukkit::getPlayer)
                                    .filter(Objects::nonNull)
                                    .forEach(target -> {
                                        if (target.equals(player)) {
                                            sender.sendMessage(ChatColor.RED +
                                                    "[Teams] 自分自身を招待することはできません！");
                                        } else if (player.getWorld().equals(target.getWorld())) {
                                            invited.put(target.getUniqueId(), playersTeam);
                                            target.sendMessage(ChatColor.GREEN + "[Teams]" + player.getDisplayName() +
                                                    " さんからチームに招待されました。\n" +
                                                    "参加するには /team accept を実行してください。");
                                        } else {
                                            sender.sendMessage(ChatColor.RED + "[Teams] 招待するプレイヤーは同じワールドにいる必要があります。");
                                        }
                                    });
                        } else {
                            sender.sendMessage(ChatColor.YELLOW + "[Teams] チームに招待するためには自分がオーナーのチームに所属している必要があります。\n" +
                                    "チームを作成するには /team create を実行してください。");
                        }
                    } else {
                        sender.sendMessage(ChatColor.YELLOW + "[Teams] 招待するプレイヤーを指定してください。");
                    }
                    break;

                case "accept":
                    val invitedTeam = invited.remove((player).getUniqueId());
                    if (invitedTeam != null) {
                        // すでにチームに所属している場合は
                        val sscPlayer = soloServerApi.getSSCPlayer(player);
                        if (sscPlayer.getJoinedTeam() != null) {
                            sscPlayer.getJoinedTeam().leaveTeam(player);
                            sender.sendMessage(ChatColor.GREEN + "[Teams] これまで入っていたチームから脱退しました。");
                        }

                        invitedTeam.joinTeam(player);
                    } else {
                        sender.sendMessage(ChatColor.YELLOW + "[Teams] あなたはまだ招待を受け取っていません！");
                    }
                    break;

                case "leave": {
                    val sscPlayer = soloServerApi.getSSCPlayer(player);
                    if (sscPlayer.getJoinedTeam() != null) {
                        val team = sscPlayer.getJoinedTeam();
                        if (settingsManager.isTeamSpawnCollect()
                                && team.getOwner().equals(player.getUniqueId())
                                && !team.getMembers().isEmpty()) {
                            invited.put(sscPlayer.getJoinedTeamId(), null);
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
                    val sscPlayer = soloServerApi.getSSCPlayer(player);
                    if (sscPlayer.getJoinedTeam() != null && invited.remove(sscPlayer.getJoinedTeamId()) != null) {
                        val team = sscPlayer.getJoinedTeam();
                        team.leaveTeam(player);
                        sender.sendMessage(ChatColor.GREEN + "[Teams] チームから脱退しました。");
                    }
                }
                break;

                case "transfer":
                    if (args.length >= 2) {
                        val originalTeam = soloServerApi.getPlayersTeam(player);
                        if (originalTeam != null) {
                            val target = Bukkit.getPlayer(args[1]);
                            if (target != null && !player.equals(target)) {
                                val sscPlayer = soloServerApi.getSSCPlayer(target);
                                if (originalTeam.getId().equals(sscPlayer.getJoinedTeamId())) {
                                    val transferredTeam = new PlayersTeam(originalTeam.getId(), target.getUniqueId());
                                    transferredTeam.setMembers(originalTeam.getMembers());
                                    val updateEvent = new PlayersTeamStatusUpdateEvent(
                                            originalTeam,
                                            player,
                                            PlayersTeamStatusUpdateEvent.UpdatedState.OWNER,
                                            originalTeam,
                                            transferredTeam);
                                    Bukkit.getServer().getPluginManager().callEvent(updateEvent);
                                } else {
                                    sender.sendMessage(ChatColor.YELLOW + "[Teams] オーナーを譲渡するためには対象がチームのメンバーである必要があります。");
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
                    val targetTeam = soloServerApi.getPlayersTeam(player);
                    if (targetTeam != null) {
                        if (args.length >= 2)
                            targetTeam.updateTeamName(player, args[1]);
                        else
                            targetTeam.updateTeamName(player, null);
                        sender.sendMessage(ChatColor.GREEN + "[Teams] チーム名を変更が変更されました。");
                    } else {
                        sender.sendMessage(ChatColor.YELLOW + "[Teams] チーム名を変更するには自分がオーナーのチームに所属している必要があります。");
                    }
                    break;
            }
        } else {
            sender.sendMessage("[Teams] This command must be executed in-game.");
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
