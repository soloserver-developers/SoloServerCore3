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

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import page.nafuchoco.soloservercore.database.PlayersTable;
import page.nafuchoco.soloservercore.database.PlayersTeamsTable;

import java.util.List;

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
        if (!sender.hasPermission("soloservercore.maintenance")) {
            sender.sendMessage(ChatColor.RED + "You can't run this command because you don't have permission.");
        } else if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Insufficient arguments to execute the command.");
        } else switch (args[0]) {
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
