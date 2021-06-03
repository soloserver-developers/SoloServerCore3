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

package page.nafuchoco.soloservercore.database;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.val;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import page.nafuchoco.soloservercore.SoloServerCore;
import page.nafuchoco.soloservercore.data.OfflineSSCPlayer;
import page.nafuchoco.soloservercore.data.SSCPlayer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class PlayersTable extends DatabaseTable {

    public PlayersTable(String tablename, DatabaseConnector connector) {
        super(tablename, connector);
    }

    public void createTable() throws SQLException {
        super.createTable("id VARCHAR(36) PRIMARY KEY, spawn_location TEXT NOT NULL, joined_team VARCHAR(36)");
    }

    public List<OfflineSSCPlayer> getPlayers() {
        List<OfflineSSCPlayer> players = new ArrayList<>();
        try (var connection = getConnector().getConnection();
             var ps = connection.prepareStatement(
                     "SELECT * FROM " + getTablename()
             )) {
            try (var resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    val id = UUID.fromString(resultSet.getString("id"));
                    val spawnLocation = resultSet.getString("spawn_location");
                    UUID joinedTeam = null;
                    val teamUUID = resultSet.getString("joined_team");
                    if (teamUUID != null)
                        joinedTeam = UUID.fromString(teamUUID);
                    players.add(new OfflineSSCPlayer(id, spawnLocation, joinedTeam));
                }
            }
        } catch (SQLException e) {
            SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to get player data.", e);
        }
        return players;
    }

    public OfflineSSCPlayer getPlayerData(@NotNull UUID uuid) {
        try (var connection = getConnector().getConnection();
             var ps = connection.prepareStatement(
                     "SELECT * FROM " + getTablename() + " WHERE id = ?"
             )) {
            ps.setString(1, uuid.toString());
            try (var resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    val spawnLocation = resultSet.getString("spawn_location");
                    UUID joinedTeam = null;
                    val teamUUID = resultSet.getString("joined_team");
                    if (teamUUID != null)
                        joinedTeam = UUID.fromString(teamUUID);
                    return new OfflineSSCPlayer(uuid, spawnLocation, joinedTeam);
                }
            }
        } catch (SQLException e) {
            SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to get player data.", e);
        }
        return null;
    }

    public void registerPlayer(@NotNull SSCPlayer sscPlayer) throws SQLException {
        try (var connection = getConnector().getConnection();
             var ps = connection.prepareStatement(
                     "INSERT INTO " + getTablename() + " (id, spawn_location, joined_team) " +
                             "VALUES (?, ?, ?)"
             )) {
            ps.setString(1, sscPlayer.getId().toString());
            ps.setString(2, sscPlayer.getSpawnLocation());
            if (sscPlayer.getJoinedTeamId() != null)
                ps.setString(3, sscPlayer.getJoinedTeamId().toString());
            else
                ps.setString(3, null);
            ps.execute();
        }
    }

    public void updateSpawnLocation(@NotNull UUID uuid, @NotNull Location location) throws SQLException {
        val locationJson = new JsonObject();
        locationJson.addProperty("World", location.getWorld().getName());
        locationJson.addProperty("X", location.getBlockX());
        locationJson.addProperty("Y", location.getBlockY());
        locationJson.addProperty("Z", location.getBlockZ());
        val stringLocation = new Gson().toJson(locationJson);

        try (var connection = getConnector().getConnection();
             var ps = connection.prepareStatement(
                     "UPDATE " + getTablename() + " SET spawn_location = ? WHERE id = ?"
             )) {
            ps.setString(1, stringLocation);
            ps.setString(2, uuid.toString());
            ps.execute();
        }
    }

    public void updateJoinedTeam(@NotNull UUID uuid, @Nullable UUID joinedTeam) throws SQLException {
        try (var connection = getConnector().getConnection();
             var ps = connection.prepareStatement(
                     "UPDATE " + getTablename() + " SET joined_team = ? WHERE id = ?"
             )) {
            ps.setString(2, uuid.toString());
            if (joinedTeam != null)
                ps.setString(1, joinedTeam.toString());
            else
                ps.setString(1, null);
            ps.execute();
        }
    }

    public void deletePlayer(@NotNull OfflineSSCPlayer sscPlayer) throws SQLException {
        try (var connection = getConnector().getConnection();
             var ps = connection.prepareStatement(
                     "DELETE FROM " + getTablename() + " WHERE id = ?"
             )) {
            ps.setString(1, sscPlayer.getId().toString());
            ps.execute();
        }
    }
}
