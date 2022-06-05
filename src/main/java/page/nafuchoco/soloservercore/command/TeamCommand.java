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
import page.nafuchoco.soloservercore.MessageManager;
import page.nafuchoco.soloservercore.SoloServerApi;
import page.nafuchoco.soloservercore.SoloServerCore;
import page.nafuchoco.soloservercore.data.PlayersTeam;
import page.nafuchoco.soloservercore.data.TempSSCPlayer;
import page.nafuchoco.soloservercore.database.PluginSettingsManager;
import page.nafuchoco.soloservercore.event.team.PlayersTeamCreateEvent;
import page.nafuchoco.soloservercore.event.team.PlayersTeamStatusUpdateEvent;

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
        if (sender instanceof Player player) {
            val soloServerApi = SoloServerApi.getInstance();
            val sscPlayer = soloServerApi.getSSCPlayer(player);
            if (sscPlayer instanceof TempSSCPlayer) {
                return true;
            }

            if (args.length == 0) {
                // Show Team Status
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
                            .map(Bukkit::getOfflinePlayer)
                            .map(p -> p.getName() + ChatColor.GRAY +
                                    " [" + dateFormat.format(p.getLastPlayed()) + "]" + ChatColor.WHITE)
                            .collect(Collectors.joining("\n")));
                    sender.sendMessage(builder.toString());
                } else {
                    sender.sendMessage(SoloServerCore.getMessage(player, "command.team.no-affiliation"));
                }
            } else if (!sender.hasPermission("soloservercore.team." + args[0])) {
                sender.sendMessage(ChatColor.RED + "You can't run this command because you don't have permission.");
            } else switch (args[0]) {
                case "create": {
                    // すでにチームに所属している場合は
                    if (sscPlayer.getJoinedTeam() != null) {
                        val team = sscPlayer.getJoinedTeam();
                        if (player.getUniqueId().equals(team.getOwner())) {
                            sender.sendMessage(SoloServerCore.getMessage(player, "teams.create.warn.already"));
                            break;
                        } else {
                            team.leaveTeam(player);
                            sender.sendMessage(SoloServerCore.getMessage(player, "teams.leave"));
                        }
                    }

                    val id = UUID.randomUUID();
                    val createEvent = new PlayersTeamCreateEvent(new PlayersTeam(id, (player).getUniqueId()), sscPlayer);
                    Bukkit.getServer().getPluginManager().callEvent(createEvent);
                    if (!createEvent.isCancelled())
                        sender.sendMessage(SoloServerCore.getMessage(player, "teams.create.created"));
                    else
                        sender.sendMessage(SoloServerCore.getMessage(player, "teams.create.fail"));
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
                                            sender.sendMessage(SoloServerCore.getMessage(player, "teams.invite.warn.self"));
                                        } else if (player.getWorld().equals(target.getWorld())) {
                                            invited.put(target.getUniqueId(), playersTeam);
                                            target.sendMessage(MessageManager.format(SoloServerCore.getMessage(target, "teams.invite.receive"),
                                                    player.getDisplayName()));
                                        } else {
                                            sender.sendMessage(SoloServerCore.getMessage(target, "teams.invite.warn.world"));
                                        }
                                    });
                        } else {
                            sender.sendMessage(SoloServerCore.getMessage(player, "teams.invite.warn.owner"));
                        }
                    } else {
                        sender.sendMessage(SoloServerCore.getMessage(player, "teams.invite.warn.select"));
                    }
                    break;

                case "accept":
                    val invitedTeam = invited.remove((player).getUniqueId());
                    if (invitedTeam != null) {
                        // すでにチームに所属している場合は
                        if (sscPlayer.getJoinedTeam() != null) {
                            sscPlayer.getJoinedTeam().leaveTeam(player);
                            sender.sendMessage(SoloServerCore.getMessage(player, "teams.leave"));
                        }

                        invitedTeam.joinTeam(player);
                    } else {
                        sender.sendMessage(SoloServerCore.getMessage(player, "teams.invite.warn.notfound"));
                    }
                    break;

                case "leave": {
                    if (sscPlayer.getJoinedTeam() != null) {
                        val team = sscPlayer.getJoinedTeam();
                        if (settingsManager.isTeamSpawnCollect()
                                && team.getOwner().equals(player.getUniqueId())
                                && !team.getMembers().isEmpty()) {
                            invited.put(sscPlayer.getJoinedTeamId(), null);
                            sender.sendMessage(SoloServerCore.getMessage(player, "teams.leave.confirm"));
                        } else {
                            team.leaveTeam(player);
                            sender.sendMessage(SoloServerCore.getMessage(player, "teams.leave"));
                        }

                    }
                }
                break;

                case "confirm": {
                    if (sscPlayer.getJoinedTeam() != null && invited.remove(sscPlayer.getJoinedTeamId()) != null) {
                        val team = sscPlayer.getJoinedTeam();
                        team.leaveTeam(player);
                        sender.sendMessage(SoloServerCore.getMessage(player, "teams.leave"));
                    }
                }
                break;

                case "transfer":
                    if (args.length >= 2) {
                        val originalTeam = soloServerApi.getPlayersTeam(player);
                        if (originalTeam != null) {
                            val target = Bukkit.getPlayer(args[1]);
                            if (target != null && !player.equals(target)) {
                                val targetPlayer = soloServerApi.getSSCPlayer(target);
                                if (!(targetPlayer instanceof TempSSCPlayer)
                                        && originalTeam.getId().equals(targetPlayer.getJoinedTeamId())) {
                                    val transferredTeam = new PlayersTeam(originalTeam.getId(), target.getUniqueId());
                                    transferredTeam.setMembers(originalTeam.getMembers());
                                    val updateEvent = new PlayersTeamStatusUpdateEvent(
                                            originalTeam,
                                            targetPlayer,
                                            PlayersTeamStatusUpdateEvent.UpdatedState.OWNER,
                                            originalTeam,
                                            transferredTeam);
                                    Bukkit.getServer().getPluginManager().callEvent(updateEvent);
                                } else {
                                    sender.sendMessage(SoloServerCore.getMessage(player, "teams.transfer.warn.member"));
                                }
                            }
                        } else {
                            sender.sendMessage(SoloServerCore.getMessage(player, "teams.transfer.warn.owner"));
                        }
                    } else {
                        sender.sendMessage(SoloServerCore.getMessage(player, "teams.transfer.warn.select"));
                    }
                    break;

                case "name":
                    val targetTeam = soloServerApi.getPlayersTeam(player);
                    if (targetTeam != null) {
                        if (args.length >= 2)
                            targetTeam.updateTeamName(player, args[1]);
                        else
                            targetTeam.updateTeamName(player, null);
                        sender.sendMessage(SoloServerCore.getMessage(player, "teams.name.changed"));
                    } else {
                        sender.sendMessage(SoloServerCore.getMessage(player, "teams.name.warn.owner"));
                    }
                    break;

                default:
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
            return Bukkit.getServer().getOnlinePlayers().stream().map(Player::getName).toList();
        else
            return null;
    }
}
