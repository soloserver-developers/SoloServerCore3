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

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import page.nafuchoco.soloservercore.SoloServerCore;
import page.nafuchoco.soloservercore.data.PlayersTeam;

import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class PlayersTeamsTable extends DatabaseTable {
    private static final Gson gson = new Gson();

    public PlayersTeamsTable(String tablename, DatabaseConnector connector) {
        super(tablename, connector);
    }

    public void createTable() throws SQLException {
        super.createTable("id VARCHAR(36) PRIMARY KEY, team_name VARCHAR(16), owner VARCHAR(36) NOT NULL, members LONGTEXT");
    }

    public UUID searchTeamFromOwner(@NotNull UUID owner) {
        try (var connection = getConnector().getConnection();
             var ps = connection.prepareStatement(
                     "SELECT id FROM " + getTablename() + " WHERE owner = ?"
             )) {
            ps.setString(1, owner.toString());
            try (var resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    return UUID.fromString(resultSet.getString("id"));
                }
            }
        } catch (SQLException e) {
            SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to get players team data.", e);
        }
        return null;
    }

    public List<PlayersTeam> getPlayersTeams() throws SQLSyntaxErrorException {
        val playersTeams = new ArrayList<PlayersTeam>();
        try (var connection = getConnector().getConnection();
             var ps = connection.prepareStatement(
                     "SELECT * FROM " + getTablename()
             )) {
            try (var resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    val id = UUID.fromString(resultSet.getString("id"));
                    val owner = UUID.fromString(resultSet.getString("owner"));
                    val members = resultSet.getString("members");
                    val team = new PlayersTeam(id, owner);
                    if (!StringUtils.isEmpty(members))
                        team.setMembers(gson.fromJson(members, new TypeToken<List<UUID>>() {
                        }.getType()));
                    team.setTeamName(resultSet.getString("team_name"));
                    playersTeams.add(team);
                }
            }
        } catch (SQLSyntaxErrorException e1) {
            throw e1;
        } catch (SQLException e2) {
            SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to get players team data.", e2);
        }
        return playersTeams;
    }

    public PlayersTeam getPlayersTeam(@NotNull UUID id) {
        try (var connection = getConnector().getConnection();
             var ps = connection.prepareStatement(
                     "SELECT * FROM " + getTablename() + " WHERE id = ?"
             )) {
            ps.setString(1, id.toString());
            try (var resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    val owner = UUID.fromString(resultSet.getString("owner"));
                    val members = resultSet.getString("members");
                    val team = new PlayersTeam(id, owner);
                    if (!StringUtils.isEmpty(members))
                        team.setMembers(gson.fromJson(members, new TypeToken<List<UUID>>() {
                        }.getType()));
                    team.setTeamName(resultSet.getString("team_name"));
                    return team;
                }
            }
        } catch (SQLException e) {
            SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to get players team data.", e);
        }
        return null;
    }

    public void registerTeam(@NotNull PlayersTeam playersTeam) throws SQLException {
        try (var connection = getConnector().getConnection();
             var ps = connection.prepareStatement(
                     "INSERT INTO " + getTablename() + " (id, owner, team_name, members) VALUES (?, ?, ?, ?)"
             )) {
            ps.setString(1, playersTeam.getId().toString());
            ps.setString(2, playersTeam.getOwner().toString());
            ps.setString(3, playersTeam.getTeamName());
            String membersJson = gson.toJson(playersTeam.getMembers());
            ps.setString(4, membersJson);
            ps.execute();
        }
    }

    public void updateTeamOwner(@NotNull UUID id, @NotNull UUID owner) throws SQLException {
        try (var connection = getConnector().getConnection();
             var ps = connection.prepareStatement(
                     "UPDATE " + getTablename() + " SET owner = ? WHERE id = ?"
             )) {
            ps.setString(1, owner.toString());
            ps.setString(2, id.toString());
            ps.execute();
        }
    }

    public void updateTeamName(@NotNull UUID id, @Nullable String name) throws SQLException {
        try (var connection = getConnector().getConnection();
             var ps = connection.prepareStatement(
                     "UPDATE " + getTablename() + " SET team_name = ? WHERE id = ?"
             )) {
            ps.setString(1, name);
            ps.setString(2, id.toString());
            ps.execute();
        }
    }

    public void updateMembers(@NotNull UUID id, @Nullable List<UUID> members) throws SQLException {
        try (var connection = getConnector().getConnection();
             var ps = connection.prepareStatement(
                     "UPDATE " + getTablename() + " SET members = ? WHERE id = ?"
             )) {
            ps.setString(1, gson.toJson(members));
            ps.setString(2, id.toString());
            ps.execute();
        }
    }

    public void deleteTeam(@NotNull UUID id) throws SQLException {
        try (var connection = getConnector().getConnection();
             var ps = connection.prepareStatement(
                     "DELETE FROM " + getTablename() + " WHERE id = ?"
             )) {
            ps.setString(1, id.toString());
            ps.execute();
        }
    }
}
