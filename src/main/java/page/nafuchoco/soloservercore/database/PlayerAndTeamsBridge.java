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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import page.nafuchoco.soloservercore.SoloServerCore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class PlayerAndTeamsBridge {
    private static final Gson gson = new Gson();

    private final DatabaseConnector connector;
    private final PlayersTable playersTable;
    private final PlayersTeamsTable teamsTable;

    public PlayerAndTeamsBridge(DatabaseConnector connector, PlayersTable playersTable, PlayersTeamsTable teamsTable) {
        this.connector = connector;
        this.playersTable = playersTable;
        this.teamsTable = teamsTable;
    }

    public TeamsPlayerData getPlayerData(@NotNull Player player) {
        try (Connection connection = connector.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT * FROM " + playersTable.getTablename() + " INNER JOIN " + teamsTable.getTablename() +
                             " ON " + playersTable.getTablename() + ".joined_team = " + teamsTable.getTablename() + ".id" +
                             " WHERE " + playersTable.getTablename() + ".id = UUID_TO_BIN(?)"
             )) {
            ps.setString(1, player.getUniqueId().toString());
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    String playerName = resultSet.getString("player_name");
                    String spawnLocation = resultSet.getString("spawn_location");
                    Date lastJoined = new Date(resultSet.getTimestamp("last_joined").getTime());
                    UUID joinedTeam = UUID.fromString(resultSet.getString("joined_team"));
                    UUID owner = UUID.fromString(resultSet.getString("owner"));
                    String members = resultSet.getString("members");
                    List<UUID> membersList = null;
                    if (!StringUtils.isEmpty(members))
                        membersList = gson.fromJson(members, new TypeToken<List<UUID>>() {
                        }.getType());
                    return new TeamsPlayerData(
                            player.getUniqueId(),
                            playerName,
                            spawnLocation,
                            lastJoined,
                            joinedTeam,
                            owner,
                            membersList);
                }
            }
        } catch (SQLException throwables) {
            SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to get player data.", throwables);
        }
        return null;
    }
}
