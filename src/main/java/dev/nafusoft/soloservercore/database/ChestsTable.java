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

package dev.nafusoft.soloservercore.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nafusoft.soloservercore.data.PlayersTeam;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChestsTable extends DatabaseTable {
    private final ObjectMapper mapper = new ObjectMapper();

    public ChestsTable(String tablename, DatabaseConnector connector) {
        super(tablename, connector);
    }

    public void createTable() throws SQLException {
        super.createTable("id VARCHAR(36) PRIMARY KEY, items VARBINARY(1024) NOT NULL");
    }

    public byte[] getTeamChestInventory(PlayersTeam team) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT * FROM " + getTablename() + " WHERE id = ?"
             )) {
            ps.setString(1, team.getId().toString());
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    return resultSet.getBytes("items");
                }
            }
            return new byte[0];
        }
    }

    public void saveTeamChestInventory(PlayersTeam team, byte[] items) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT INTO " + getTablename() + " (id, items) VALUES (?, ?) ON DUPLICATE KEY UPDATE items = VALUES (items)"
             )) {
            ps.setString(1, team.getId().toString());
            ps.setBytes(2, items);
            ps.executeUpdate();
        }
    }


    public void deleteTeamChest(PlayersTeam team) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "DELETE FROM " + getTablename() + " WHERE id = ?"
             )) {
            ps.setString(1, team.getId().toString());
            ps.execute();
        }
    }
}
