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

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import page.nafuchoco.soloservercore.SoloServerCore;
import page.nafuchoco.soloservercore.database.PluginSettingsManager;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class SettingsCommand implements CommandExecutor, TabCompleter {
    private static final String UPDATED_MESSAGE = ChatColor.GREEN + "[SSC] Option updated.";

    private final PluginSettingsManager settingsManager;

    public SettingsCommand(PluginSettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("soloservercore.settings")) {
            sender.sendMessage(ChatColor.RED + "You can't run this command because you don't have permission.");
        } else {
            try {
                if (args.length <= 1) {
                    sender.sendMessage(ChatColor.AQUA + "======== SoloServerCore Settings ========");
                    sender.sendMessage("checkBlock: " + settingsManager.isCheckBlock() + "\n" +
                            "protectionPeriod: " + settingsManager.getProtectionPeriod() + "\n" +
                            "teamSpawnCollect: " + settingsManager.isTeamSpawnCollect() + "\n" +
                            "stockSpawnPoint: " + settingsManager.getStockSpawnPoint() + "\n" +
                            "broadcastBedCount: " + settingsManager.isBroadcastBedCount() + "\n" +
                            "useAfkCount: " + settingsManager.isUseAfkCount() + "\n" +
                            "afkTimeThreshold: " + settingsManager.getAfkTimeThreshold() + "\n" +
                            "reteleportResetAll: " + settingsManager.isReteleportResetAll());
                } else switch (args[0]) {
                    case "checkBlock":
                        settingsManager.setCheckBlock(Boolean.parseBoolean(args[1]));
                        sender.sendMessage(UPDATED_MESSAGE);
                        break;

                    case "protectionPeriod":
                        settingsManager.setProtectionPeriod(Integer.parseInt(args[1]));
                        sender.sendMessage(UPDATED_MESSAGE);
                        break;

                    case "teamSpawnCollect":
                        settingsManager.setTeamSpawnCollect(Boolean.parseBoolean(args[1]));
                        sender.sendMessage(UPDATED_MESSAGE);
                        break;

                    case "stockSpawnPoint":
                        settingsManager.setStockSpawnPoint(Integer.parseInt(args[1]));
                        sender.sendMessage(UPDATED_MESSAGE);
                        break;

                    case "broadcastBedCount":
                        settingsManager.setBroadcastBedCount(Boolean.parseBoolean(args[1]));
                        sender.sendMessage(UPDATED_MESSAGE);
                        break;

                    case "useAfkCount":
                        settingsManager.setUseAfkCount(Boolean.parseBoolean(args[1]));
                        sender.sendMessage(UPDATED_MESSAGE);
                        break;

                    case "afkTimeThreshold":
                        settingsManager.setAfkTimeThreshold(Integer.parseInt(args[1]));
                        sender.sendMessage(UPDATED_MESSAGE);
                        break;

                    case "reteleportResetAll":
                        settingsManager.setReteleportResetAll(Boolean.parseBoolean(args[1]));
                        sender.sendMessage(UPDATED_MESSAGE);
                        break;

                    default:
                        sender.sendMessage(ChatColor.RED + "[SSC] Unknown option.");
                        break;
                }
            } catch (SQLException e) {
                sender.sendMessage(ChatColor.RED + "[SSC] An error occurred while updating the plugin settings.");
                SoloServerCore.getInstance().getLogger().log(Level.WARNING, "An error occurred while updating the plugin settings.", e);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "[SSC] この設定は整数値で指定してください。");
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1)
            return Arrays.asList(PluginSettingsManager.getSettingsKeys());
        return null;
    }
}
