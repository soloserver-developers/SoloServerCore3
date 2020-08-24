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
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import page.nafuchoco.soloservercore.SoloServerCore;
import page.nafuchoco.soloservercore.team.PlayersTeam;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        super.createTable("id BINARY(16) PRIMARY KEY, owner BINARY(16) NOT NULL, members LONGTEXT");
    }

    public UUID searchTeamFromOwner(@NotNull UUID owner) {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT BIN_TO_UUID(id) FROM " + getTablename() + " WHERE owner = UUID_TO_BIN(?)"
             )) {
            ps.setString(1, owner.toString());
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    return UUID.fromString(resultSet.getString("id"));
                }
            }
        } catch (SQLException throwables) {
            SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to get players team data.", throwables);
        }
        return null;
    }

    public PlayersTeam getPlayersTeam(@NotNull UUID id) {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT *, BIN_TO_UUID(owner) FROM " + getTablename() + " WHERE id = UUID_TO_BIN(?)"
             )) {
            ps.setString(1, id.toString());
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    UUID owner = UUID.fromString(resultSet.getString("owner"));
                    String members = resultSet.getString("members");
                    PlayersTeam team = new PlayersTeam(id, owner);
                    if (!StringUtils.isEmpty(members))
                        team.setMembers(gson.fromJson(members, new TypeToken<List<UUID>>() {
                        }.getType()));
                    return team;
                }
            }
        } catch (SQLException throwables) {
            SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to get players team data.", throwables);
        }
        return null;
    }

    public void registerTeam(@NotNull final UUID id, @NotNull final UUID owner, @Nullable List<UUID> members) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT INTO " + getTablename() + " id, owner, members VALUES (UUID_TO_BIN(?), UUID_TO_BIN(?), ?)"
             )) {
            ps.setString(1, id.toString());
            ps.setString(2, owner.toString());
            if (members == null)
                members = new ArrayList<>();
            String membersJson = gson.toJson(members);
            ps.setString(3, membersJson);
            ps.execute();
        }
    }

    public void updateMembers(@NotNull UUID id, @Nullable List<UUID> members) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "UPDATE " + getTablename() + " SET members = ? WHERE id = UUID_TO_BIN(?)"
             )) {
            ps.setString(1, gson.toJson(members));
            ps.setString(2, id.toString());
            ps.execute();
        }
    }

    public void deleteTeam(@NotNull UUID id) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "DELETE FROM " + getTablename() + " WHERE id = UUID_TO_BIN(?)"
             )) {
            ps.setString(1, id.toString());
            ps.execute();
        }
    }
}
