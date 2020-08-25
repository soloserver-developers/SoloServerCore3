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

package page.nafuchoco.soloservercore.listener;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import page.nafuchoco.soloservercore.SoloServerCore;
import page.nafuchoco.soloservercore.SpawnPointLoader;
import page.nafuchoco.soloservercore.database.PlayerData;
import page.nafuchoco.soloservercore.database.PlayersTable;

import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;

public class PlayerLoginEventListener implements Listener {
    private final PlayersTable playersTable;
    private final SpawnPointLoader loader;

    public PlayerLoginEventListener(PlayersTable playersTable, SpawnPointLoader loader) {
        this.playersTable = playersTable;
        this.loader = loader;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLoginEvent(PlayerLoginEvent event) {
        if (!loader.isDone()) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "System is in preparation.");
            return;
        }

        if (!event.getPlayer().hasPlayedBefore() || playersTable.getPlayerData(event.getPlayer()) == null) {
            Location location = loader.getNewLocation();
            PlayerData playerData = new PlayerData(event.getPlayer().getUniqueId(),
                    event.getPlayer().getName(),
                    location,
                    new Date(),
                    null);
            try {
                playersTable.registerPlayer(playerData);
            } catch (SQLException throwables) {
                SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to save the player data.\n" +
                        "New data will be regenerated next time.", throwables);
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "The login process was interrupted due to a system problem.");
            }
            event.getPlayer().teleport(location);
        } else {
            try {
                playersTable.updateJoinedDate(event.getPlayer().getUniqueId(), new Date());
            } catch (SQLException throwables) {
                SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to update the player data.", throwables);
            }
        }
    }
}
