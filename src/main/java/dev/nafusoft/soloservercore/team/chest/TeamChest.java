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

import dev.nafusoft.soloservercore.data.PlayersTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

// TODO: 2022/12/12 InventoryHolderの拡張は公式的に非推奨だが、今のところ問題ないのでそのままにしておく。
public class TeamChest implements InventoryHolder {
    private final PlayersTeam ownedTeam;
    private final Inventory inventory;
    private ChestState state = ChestState.CLOSE;

    public TeamChest(PlayersTeam ownedTeam) {
        this.ownedTeam = ownedTeam;
        this.inventory = Bukkit.getServer().createInventory(this, 54, ChatColor.DARK_AQUA + ownedTeam.getTeamDisplayName() + "'s Team Chest");
    }

    public TeamChest(PlayersTeam ownedTeam, Inventory inventory) {
        this.ownedTeam = ownedTeam;
        this.inventory = Bukkit.getServer().createInventory(this, inventory.getSize(), ChatColor.DARK_AQUA + ownedTeam.getTeamDisplayName() + "'s Team Chest");
        this.inventory.setContents(inventory.getContents());
    }

    public PlayersTeam getOwnedTeam() {
        return ownedTeam;
    }

    public ChestState getChestState() {
        return state;
    }

    public void setState(ChestState state) {
        this.state = state;
    }

    public void openInventory(Player player) {
        player.openInventory(inventory);
    }


    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
