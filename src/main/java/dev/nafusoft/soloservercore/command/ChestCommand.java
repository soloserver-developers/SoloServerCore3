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

import dev.nafusoft.soloservercore.MessageManager;
import dev.nafusoft.soloservercore.SoloServerApi;
import dev.nafusoft.soloservercore.SoloServerCore;
import dev.nafusoft.soloservercore.data.PlayersTeam;
import dev.nafusoft.soloservercore.exception.PlayerDataSynchronizingException;
import dev.nafusoft.soloservercore.team.chest.TeamChestManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class ChestCommand implements CommandExecutor {
    private final TeamChestManager chestManager;

    public ChestCommand(TeamChestManager chestManager) {
        this.chestManager = chestManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player player && sender.hasPermission("soloservercore.team.chest")) {
            PlayersTeam joinedTeam = SoloServerApi.getInstance().getPlayersTeam(player);
            if (joinedTeam != null) {
                try {
                    chestManager.getTeamChest(joinedTeam).openInventory(player);
                } catch (PlayerDataSynchronizingException e) {
                    sender.sendMessage(MessageManager.getMessage("teams.chest.syncing-failed"));
                    SoloServerCore.getInstance().getLogger().log(Level.WARNING, "An error occurred while fetching inventory data.", e);
                }
            } else {
                player.sendMessage(ChatColor.YELLOW + "[TeamChest] チームチェストを使用するにはチームに所属している必要があります。");
            }
        }
        return true;
    }
}
