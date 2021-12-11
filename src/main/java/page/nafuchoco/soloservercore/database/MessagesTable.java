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
import org.jetbrains.annotations.NotNull;
import page.nafuchoco.soloservercore.SoloServerCore;
import page.nafuchoco.soloservercore.data.PlayersTeam;
import page.nafuchoco.soloservercore.data.TeamMessage;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class MessagesTable extends DatabaseTable {
    private static final Gson gson = new Gson();

    public MessagesTable(String tablename, DatabaseConnector connector) {
        super(tablename, connector);
    }

    public void createTable() throws SQLException {
        super.createTable("id VARCHAR(36) PRIMARY KEY, sender_id VARCHAR(36) NOT NULL, " +
                "target_team VARCHAR(36) NOT NULL, sent_date TIMESTAMP, subject TINYTEXT NOT NULL, message LONGTEXT NOT NULL");
    }

    public List<TeamMessage> getAllMessage(PlayersTeam team) {
        List<TeamMessage> messages = new ArrayList<>();
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT * FROM " + getTablename() + " WHERE target_team = ?"
             )) {
            ps.setString(1, team.getId().toString());
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    UUID id = UUID.fromString(resultSet.getString("id"));
                    UUID senderId = UUID.fromString(resultSet.getString("sender_id"));
                    Date sentDate = new Date(resultSet.getTimestamp("sent_date").getTime());
                    String subject = resultSet.getString("subject");
                    List<String> message = gson.fromJson(resultSet.getString("message"), new TypeToken<List<String>>() {
                    }.getType());
                    TeamMessage teamMessage = new TeamMessage(id, senderId, team.getId(), sentDate, subject, message);
                    messages.add(teamMessage);
                }
            }
        } catch (SQLException e) {
            SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to get players team data.", e);
        }
        return messages;
    }

    public List<TeamMessage> getNewMessage(PlayersTeam team, Date from) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT * FROM " + getTablename() + " WHERE target_team = ? AND sent_date > ?"
             )) {
            ps.setString(1, team.getId().toString());
            ps.setTimestamp(2, new Timestamp(from.getTime()));
            try (ResultSet resultSet = ps.executeQuery()) {
                List<TeamMessage> messages = new ArrayList<>();
                while (resultSet.next()) {
                    UUID id = UUID.fromString(resultSet.getString("id"));
                    UUID senderId = UUID.fromString(resultSet.getString("sender_id"));
                    Date sentDate = new Date(resultSet.getTimestamp("sent_date").getTime());
                    String subject = resultSet.getString("subject");
                    List<String> message = gson.fromJson(resultSet.getString("message"), new TypeToken<List<String>>() {
                    }.getType());
                    TeamMessage teamMessage = new TeamMessage(id, senderId, team.getId(), sentDate, subject, message);
                    messages.add(teamMessage);
                }
                return messages;
            }
        }
    }

    public TeamMessage getMessage(UUID messageId) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT * FROM " + getTablename() + " WHERE id = ?"
             )) {
            ps.setString(1, messageId.toString());
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    UUID id = UUID.fromString(resultSet.getString("id"));
                    UUID senderId = UUID.fromString(resultSet.getString("sender_id"));
                    UUID targetTeam = UUID.fromString(resultSet.getString("target_team"));
                    Date sentDate = new Date(resultSet.getTimestamp("sent_date").getTime());
                    String subject = resultSet.getString("subject");
                    List<String> message = gson.fromJson(resultSet.getString("message"), new TypeToken<List<String>>() {
                    }.getType());
                    return new TeamMessage(id, senderId, targetTeam, sentDate, subject, message);
                }
            }
            return null;
        }
    }

    public void registerMessage(@NotNull TeamMessage message) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT INTO " + getTablename() + " (id, sender_id, target_team, sent_date, subject, message) VALUES (?, ?, ?, ?, ?, ?)"
             )) {
            ps.setString(1, message.getId().toString());
            ps.setString(2, message.getSenderPlayer().toString());
            ps.setString(3, message.getTargetTeam().toString());
            ps.setTimestamp(4, new Timestamp(message.getSentDate().getTime()));
            ps.setString(5, message.getSubject());
            ps.setString(6, gson.toJson(message.getMessage()));
            ps.execute();
        }
    }

    public void deleteMessage(@NotNull UUID messageId) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "DELETE FROM " + getTablename() + " WHERE id = ?"
             )) {
            ps.setString(1, messageId.toString());
            ps.execute();
        }
    }

    public void deleteAllMessages(@NotNull UUID teamId) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "DELETE FROM " + getTablename() + " WHERE target_team = ?"
             )) {
            ps.setString(1, teamId.toString());
            ps.execute();
        }
    }
}
