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

package dev.nafusoft.soloservercore.team.chest;

import dev.nafusoft.soloservercore.InventoryEncoder;
import dev.nafusoft.soloservercore.MessageManager;
import dev.nafusoft.soloservercore.SoloServerCore;
import dev.nafusoft.soloservercore.data.PlayersTeam;
import dev.nafusoft.soloservercore.database.ChestsTable;
import dev.nafusoft.soloservercore.event.team.PlayersTeamDisappearanceEvent;
import dev.nafusoft.soloservercore.exception.ItemProcessingException;
import dev.nafusoft.soloservercore.exception.PlayerDataSynchronizingException;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class TeamChestManager implements Listener {
    private final ChestsTable chestsTable;
    private final Map<PlayersTeam, TeamChest> teamChests = new HashMap<>();

    public TeamChestManager(ChestsTable chestsTable) {
        this.chestsTable = chestsTable;
    }

    public TeamChest getTeamChest(PlayersTeam team) throws PlayerDataSynchronizingException {
        TeamChest teamChest = teamChests.get(team);

        if (teamChest == null) {
            try {
                byte[] inventoryBytes = chestsTable.getTeamChestInventory(team);
                if (inventoryBytes == null) {
                    teamChest = new TeamChest(team);
                } else {
                    teamChest = new TeamChest(team, InventoryEncoder.decodeInventory(inventoryBytes, true));
                }
            } catch (SQLException e) {
                throw new PlayerDataSynchronizingException("An error occurred while fetching inventory data.", e);
            } catch (ItemProcessingException e) {
                throw new PlayerDataSynchronizingException("An error occurred while processing inventory data.", e);
            }

            teamChests.put(team, teamChest);
        }

        return teamChest;
    }

    public void deleteTeamChest(PlayersTeam team) throws SQLException {
        chestsTable.deleteTeamChest(team);
        teamChests.remove(team);
    }

    public void saveAllOpenedChest() {
        teamChests.values().stream()
                .filter(teamChest -> teamChest.getChestState() == ChestState.OPEN)
                .forEach(chest -> {
                    chest.getInventory().close();
                    try {
                        byte[] inventoryBytes = InventoryEncoder.encodeInventory(chest.getInventory(), true);
                        chestsTable.saveTeamChestInventory(chest.getOwnedTeam(), inventoryBytes);
                    } catch (ItemProcessingException | SQLException e) {
                        SoloServerCore.getInstance().getLogger().log(Level.WARNING, "An error occurred while saving team chest inventory.", e);
                    }
                });
    }


    @EventHandler(ignoreCancelled = true)
    public void onInventoryOpenEvent(InventoryOpenEvent event) {
        if (event.getInventory().getHolder() instanceof TeamChest holder) {
            switch (holder.getChestState()) {
                case CLOSE -> {
                    holder.setState(ChestState.OPEN);
                }
                case OPEN -> {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(MessageManager.getMessage("teams.chest.opened"));
                }
                case SYNCING -> {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(MessageManager.getMessage("teams.chest.syncing"));
                }
            }
        }
    }

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof TeamChest holder) {
            holder.setState(ChestState.SYNCING);
            Bukkit.getServer().getScheduler().runTaskAsynchronously(SoloServerCore.getInstance(), () -> {
                try {
                    byte[] inventoryBytes = InventoryEncoder.encodeInventory(holder.getInventory(), true);
                    chestsTable.saveTeamChestInventory(holder.getOwnedTeam(), inventoryBytes);
                    holder.setState(ChestState.CLOSE);
                } catch (ItemProcessingException | SQLException e) {
                    event.getPlayer().sendMessage(MessageManager.getMessage("teams.chest.syncing-failed"));
                    SoloServerCore.getInstance().getLogger().log(Level.WARNING, "An error occurred while saving team chest inventory.", e);
                    // TODO: 2022/12/12 保存に失敗した場合のバックアップを考える
                }
            });
        }
    }

    @EventHandler
    public void onPlayersTeamDisappearanceEvent(PlayersTeamDisappearanceEvent event) {
        try {
            deleteTeamChest(event.getPlayersTeam());
        } catch (SQLException e) {
            SoloServerCore.getInstance().getLogger().log(Level.WARNING, "An error occurred while deleting inventory data.", e);
        }
    }
}
