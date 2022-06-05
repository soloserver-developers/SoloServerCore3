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

import lombok.val;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
import page.nafuchoco.soloservercore.data.PlayersTeam;
import page.nafuchoco.soloservercore.data.TeamMessage;

import java.util.*;

public class MessageCommand implements CommandExecutor, TabCompleter {
    private final Map<Player, TeamMessage.TeamMessageBuilder> makingMessage = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            val soloServerApi = SoloServerApi.getInstance();
            if (args.length == 0) {
                // TODO: 2021/10/26 Add Help
            } else if (!sender.hasPermission("messageboard." + args[0])) {
                sender.sendMessage(ChatColor.RED + "You can't run this command because you don't have permission.");
            } else switch (args[0]) {
                case "create": {
                    val joinedTeam = soloServerApi.getPlayersTeam(player);
                    if (joinedTeam != null) {
                        TeamMessage.TeamMessageBuilder builder = new TeamMessage.TeamMessageBuilder(player.getUniqueId());
                        builder.setTargetTeam(joinedTeam);
                        makingMessage.put(player, builder);
                        player.sendMessage(SoloServerCore.getMessage(player, "command.message.create.start"));
                        sendMessageEditor(player);
                    } else {
                        player.sendMessage(SoloServerCore.getMessage(player, "command.message.create.warn.team"));
                    }
                }
                break;

                case "subject":
                    if (args.length >= 2) {
                        TeamMessage.TeamMessageBuilder builder = makingMessage.get(player);
                        if (builder != null) {
                            builder.setSubject(args[1]);
                            sendMessageEditor(player);
                        } else {
                            player.sendMessage(SoloServerCore.getMessage(player, "command.message.create.warn"));
                        }
                    }
                    break;

                case "message":
                    if (args.length >= 3) {
                        switch (args[1]) {
                            case "add" -> {
                                TeamMessage.TeamMessageBuilder builder = makingMessage.get(player);
                                if (builder != null) {
                                    builder.addMessageLine(args[2]);
                                    sendMessageEditor(player);
                                } else {
                                    player.sendMessage(SoloServerCore.getMessage(player, "command.message.create.warn"));
                                }
                            }
                            case "remove" -> {
                                TeamMessage.TeamMessageBuilder builder = makingMessage.get(player);
                                if (builder != null) {
                                    try {
                                        builder.removeMessageLine(Integer.parseInt(args[2]));
                                        sendMessageEditor(player);
                                    } catch (NumberFormatException e) {
                                        player.sendMessage(SoloServerCore.getMessage(player, "command.message.linenumber"));
                                    }
                                } else {
                                    player.sendMessage(SoloServerCore.getMessage(player, "command.message.create.warn"));
                                }
                            }
                            default -> {
                            }
                        }
                    } else if (args[1].equals("add")) {
                        TeamMessage.TeamMessageBuilder builder = makingMessage.get(player);
                        if (builder != null) {
                            builder.addMessageLine("");
                            sendMessageEditor(player);
                        } else {
                            player.sendMessage(SoloServerCore.getMessage(player, "command.message.create.warn"));
                        }
                    }
                    break;

                case "send":
                    TeamMessage.TeamMessageBuilder builder = makingMessage.get(player);
                    if (builder != null) {
                        TeamMessage teamMessage = builder.build();
                        SoloServerApi.getInstance().getPlayersTeam(teamMessage.getTargetTeam()).addTeamMessage(teamMessage);
                        makingMessage.remove(player);
                        player.sendMessage(SoloServerCore.getMessage(player, "command.message.send"));
                    } else {
                        player.sendMessage(SoloServerCore.getMessage(player, "command.message.create.warn"));
                    }
                    break;

                case "check": {
                    PlayersTeam joinedTeam = soloServerApi.getPlayersTeam(player);
                    if (joinedTeam != null) {
                        List<TeamMessage> messages = joinedTeam.getMessages();
                        sendMessageList(player, messages);
                    }
                }
                break;

                case "read": {
                    PlayersTeam joinedTeam = soloServerApi.getPlayersTeam(player);

                    TeamMessage message =
                            joinedTeam.getMessages().stream().
                                    filter(m -> m.getId().equals(UUID.fromString(args[1])))
                                    .findFirst().orElse(null);
                    if (message != null)
                        sendMessageViewer(player, message);
                    else
                        player.sendMessage(SoloServerCore.getMessage(player, "command.message.notfound"));
                }
                break;

                case "delete": {
                    PlayersTeam joinedTeam = soloServerApi.getPlayersTeam(player);
                    TeamMessage message =
                            joinedTeam.getMessages().stream().
                                    filter(m -> m.getId().equals(UUID.fromString(args[1])))
                                    .findFirst().orElse(null);
                    if (message != null) {
                        if (message.getSenderPlayer().equals(player.getUniqueId())) {
                            joinedTeam.deleteTeamMessage(message);
                            player.sendMessage(SoloServerCore.getMessage(player, "command.message.delete.deleted"));
                        } else {
                            player.sendMessage(SoloServerCore.getMessage(player, "command.message.delete.warn"));
                        }
                    }
                }
                break;

                default:
                    break;
            }
        } else {
            sender.sendMessage(ChatColor.RED + "This command must be executed in-game.");
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "subject", "message", "send", "check", "read", "delete");
        } else if (args.length >= 2 && (args[0].equals("check") || args[0].equals("delete"))) {
            List<String> ids = new LinkedList<>();
            if (sender instanceof Player player) {
                PlayersTeam joinedTeam = SoloServerApi.getInstance().getPlayersTeam(player);
                if (joinedTeam != null) {
                    List<TeamMessage> messages = joinedTeam.getMessages();
                    messages.forEach(message -> ids.add(message.getId().toString()));
                    return ids;
                }
            }
        }
        return null;
    }

    public void sendMessageEditor(Player player) {
        TeamMessage.TeamMessageBuilder builder = makingMessage.get(player);
        player.sendMessage(ChatColor.AQUA + "====== Message Edit ======");
        player.sendMessage("from: " + player.getDisplayName() + ", To: Your Team.");
        player.sendMessage("------");
        player.sendMessage("Subject: " + builder.getSubject());
        player.sendMessage("------");
        player.sendMessage("Message:");
        builder.getMessage().forEach(player::sendMessage);
    }

    public void sendMessageViewer(Player player, TeamMessage message) {
        player.sendMessage(ChatColor.AQUA + "====== " + message.getId() + " ======");
        player.sendMessage("from: " + Bukkit.getOfflinePlayer(message.getSenderPlayer()).getName() + ", To: Your Team.");
        player.sendMessage("------");
        player.sendMessage("Subject: " + message.getSubject());
        player.sendMessage("------");
        player.sendMessage("Message:");
        message.getMessage().forEach(player::sendMessage);
    }

    public void sendMessageList(Player player, List<TeamMessage> messages) {
        PlayersTeam joinedTeam = SoloServerApi.getInstance().getPlayersTeam(player);
        if (joinedTeam != null) {
            player.sendMessage(ChatColor.AQUA + "====== Team Message! ======");
            messages.forEach(message -> {
                val component = new TextComponent();
                component.setText("[" + message.getId().toString().split("-")[0] + "] ");
                component.setBold(true);
                component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/messageboard read " + message.getId()));
                component.addExtra(message.getSubject());
                player.spigot().sendMessage(component);
            });
        }
    }
}
