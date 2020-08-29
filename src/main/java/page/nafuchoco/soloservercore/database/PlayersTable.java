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

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import page.nafuchoco.soloservercore.SoloServerCore;

import java.sql.*;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;

public class PlayersTable extends DatabaseTable {

    public PlayersTable(String tablename, DatabaseConnector connector) {
        super(tablename, connector);
    }

    public void createTable() throws SQLException {
        super.createTable("id VARCHAR(36) PRIMARY KEY, player_name VARCHAR(16) NOT NULL, " +
                "spawn_location TEXT NOT NULL, last_joined TIMESTAMP, joined_team VARCHAR(36)");
    }

    public UUID searchPlayerFromName(@NotNull String playerName) {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT id FROM " + getTablename() + " WHERE player_name = ?"
             )) {
            ps.setString(1, playerName);
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    return UUID.fromString(resultSet.getString("id"));
                }
            }
        } catch (SQLException throwables) {
            SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to get player data.", throwables);
        }
        return null;
    }

    public PlayerData getPlayerData(@NotNull Player player) {
        return getPlayerData(player.getUniqueId());
    }

    public PlayerData getPlayerData(@NotNull UUID uuid) {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT * FROM " + getTablename() + " WHERE id = ?"
             )) {
            ps.setString(1, uuid.toString());
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    String playerName = resultSet.getString("player_name");
                    String spawnLocation = resultSet.getString("spawn_location");
                    Date lastJoined = new Date(resultSet.getTimestamp("last_joined").getTime());
                    UUID joinedTeam = null;
                    String teamUUID = resultSet.getString("joined_team");
                    if (teamUUID != null)
                        joinedTeam = UUID.fromString(resultSet.getString("joined_team"));
                    return new PlayerData(uuid, playerName, spawnLocation, lastJoined, joinedTeam);
                }
            }
        } catch (SQLException throwables) {
            SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to get player data.", throwables);
        }
        return null;
    }

    public void registerPlayer(@NotNull PlayerData playerData) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT INTO " + getTablename() + " (id, player_name, spawn_location, last_joined, joined_team) " +
                             "VALUES (?, ?, ?, ?, ?)"
             )) {
            ps.setString(1, playerData.getId().toString());
            ps.setString(2, playerData.getPlayerName());
            ps.setString(3, playerData.getSpawnLocation());
            ps.setTimestamp(4, new Timestamp(playerData.getLastJoined().getTime()));
            if (playerData.getJoinedTeam() != null)
                ps.setString(5, playerData.getJoinedTeam().toString());
            else
                ps.setString(5, null);
            ps.execute();
        }
    }

    public void updatePlayerName(@NotNull UUID uuid, @NotNull String playerName) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "UPDATE " + getTablename() + " SET player_name = ? WHERE id = ?"
             )) {
            ps.setString(1, playerName);
            ps.setString(2, uuid.toString());
            ps.execute();
        }
    }

    public void updateJoinedDate(@NotNull UUID uuid, @NotNull Date lastJoined) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "UPDATE " + getTablename() + " SET last_joined = ? WHERE id = ?"
             )) {
            ps.setTimestamp(1, new Timestamp(lastJoined.getTime()));
            ps.setString(2, uuid.toString());
            ps.execute();
        }
    }

    public void updateJoinedTeam(@NotNull UUID uuid, @Nullable UUID joinedTeam) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "UPDATE " + getTablename() + " SET joined_team = ? WHERE id = ?"
             )) {
            ps.setString(1, joinedTeam.toString());
            if (joinedTeam != null)
                ps.setString(2, uuid.toString());
            else
                ps.setString(2, null);
            ps.execute();
        }
    }
}
