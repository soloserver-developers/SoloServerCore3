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
import dev.nafusoft.soloservercore.data.TempSSCPlayer;
import dev.nafusoft.soloservercore.database.PlayersTable;
import dev.nafusoft.soloservercore.database.PluginSettingsManager;
import lombok.val;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public class ReTeleportCommand implements CommandExecutor, TabCompleter {
    private final PluginSettingsManager settingsManager;
    private final PlayersTable playersTable;
    private final World spawnWorld;

    public ReTeleportCommand(PluginSettingsManager settingsManager, PlayersTable playersTable, World spawnWorld) {
        this.settingsManager = settingsManager;
        this.playersTable = playersTable;
        this.spawnWorld = spawnWorld;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (!sender.hasPermission("soloservercore.reteleport")) {
                sender.sendMessage(ChatColor.RED + "You can't run this command because you don't have permission.");
            } else if (args.length == 0) {
                val sscPlayer = SoloServerApi.getInstance().getSSCPlayer(player);
                if (!(sscPlayer instanceof TempSSCPlayer)) {
                    if (!sscPlayer.getSpawnLocationObject().getWorld().equals(spawnWorld)) {
                        sender.sendMessage(SoloServerCore.getMessage(player, "command.teleport.new-world.confirm"));
                    } else {
                        sender.sendMessage(SoloServerCore.getMessage(player, "command.teleport.new-world.warn.notfound"));
                    }
                }
            } else if ("confirm".equalsIgnoreCase(args[0])) {
                val sscPlayer = SoloServerApi.getInstance().getOfflineSSCPlayer(player.getUniqueId());

                // チーム情報を確認し所属している場合は脱退
                if (sscPlayer.getJoinedTeam() != null) {
                    sscPlayer.getJoinedTeam().leaveTeam(player);
                    player.sendMessage(SoloServerCore.getMessage(player, "teams.leave"));
                }

                // ベッドスポーンの上書き
                player.setBedSpawnLocation(null);

                // プレイヤーの初期化
                if (settingsManager.isReteleportResetAll()) {
                    player.getInventory().clear();
                    player.getEnderChest().clear();
                    player.setLevel(0);
                    player.setExp(0F);
                    player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType()));
                    player.setFireTicks(0);
                    player.setHealth(20D);
                    player.setFoodLevel(20);
                    player.setSaturation(20F);
                }

                // プレイヤーを切断
                player.kickPlayer(SoloServerCore.getMessage(player, "command.teleport.new-world.in-process"));

                // 旧プレイヤーデータの削除
                try {
                    playersTable.deletePlayer(sscPlayer);
                } catch (SQLException e) {
                    SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to delete the player data.", e);
                }
                // TODO: 2022/12/12 非同期で先行して座標を生成するようにしたい。
            }
        } else {
            sender.sendMessage("This command must be executed in-game.");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1)
            return List.of("confirm");
        return null;
    }
}
