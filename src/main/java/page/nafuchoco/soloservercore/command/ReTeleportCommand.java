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
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import page.nafuchoco.soloservercore.SoloServerApi;
import page.nafuchoco.soloservercore.SoloServerCore;
import page.nafuchoco.soloservercore.SpawnPointLoader;
import page.nafuchoco.soloservercore.database.PlayersTable;
import page.nafuchoco.soloservercore.event.PlayerMoveToNewWorldEvent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class ReTeleportCommand implements CommandExecutor, TabCompleter {
    private final PlayersTable playersTable;
    private final SpawnPointLoader loader;
    private final World spawnWorld;
    private final List<Player> waitList;

    public ReTeleportCommand(PlayersTable playersTable, SpawnPointLoader loader, World spawnWorld) {
        this.playersTable = playersTable;
        this.loader = loader;
        this.spawnWorld = spawnWorld;
        waitList = new ArrayList<>();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            val player = (Player) sender;
            if (!sender.hasPermission("soloservercore.reteleport")) {
                sender.sendMessage(ChatColor.RED + "You can't run this command because you don't have permission.");
            } else if (args.length == 0) {
                val sscPlayer = SoloServerApi.getInstance().getSSCPlayer(player);
                if (!sscPlayer.getSpawnLocationObject().getWorld().equals(spawnWorld)) {
                    sender.sendMessage(ChatColor.YELLOW + "[SSC] 新しいワールドへ移動します。\n" +
                            "一度移動すると元のワールドに戻ることはできません。\n" +
                            "本当によろしいですか？移動する場合は /reteleport confirm を実行して下さい。");
                    waitList.add(player);
                } else {
                    sender.sendMessage(ChatColor.RED + "[SSC] 新しいワールドが用意された場合のみ実行することができます。");
                }
            } else switch (args[0].toLowerCase()) {
                case "confirm":
                    if (waitList.contains(player))
                        reTeleport(player);
                    break;

                default:
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
            return Arrays.asList("confirm");
        return null;
    }

    private void reTeleport(Player player) {
        // チーム情報を確認し所属している場合は脱退
        val sscPlayer = SoloServerApi.getInstance().getSSCPlayer(player);
        if (sscPlayer.getJoinedTeam() != null) {
            sscPlayer.getJoinedTeam().leaveTeam(player);
            player.sendMessage(ChatColor.GREEN + "[Teams] チームから脱退しました。");
        }

        // 新規座標への移動
        val location = loader.getNewLocation();
        player.teleport(location);
        player.setCompassTarget(location);

        // イベントの発火
        val moveToNewWorldEvent = new PlayerMoveToNewWorldEvent(player, sscPlayer.getSpawnLocationObject().getWorld(), location.getWorld());
        Bukkit.getPluginManager().callEvent(moveToNewWorldEvent);

        // ベッドスポーンの上書き
        player.setBedSpawnLocation(null);

        // データの上書き
        try {
            playersTable.updateSpawnLocation(player.getUniqueId(), location);
        } catch (SQLException e) {
            SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to update the player data.", e);
        }

        // ログの出力
        SoloServerCore.getInstance().getLogger().log(Level.INFO,
                player.getName() +
                        " has been successfully teleported to " +
                        location.getBlockX() + ", " +
                        location.getBlockY() + ", " +
                        location.getBlockZ());
    }
}
