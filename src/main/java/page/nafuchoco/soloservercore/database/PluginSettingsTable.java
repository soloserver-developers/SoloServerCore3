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

import page.nafuchoco.soloservercore.SoloServerCore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public class PluginSettingsTable extends DatabaseTable {

    public PluginSettingsTable(String tablename, DatabaseConnector connector) {
        super(tablename, connector);
    }

    public void createTable() throws SQLException {
        super.createTable("settings_name VARCHAR(32) NOT NULL, settings_value LONGTEXT NOT NULL");
    }

    public String getPluginSetting(String name) {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT settings_value FROM " + getTablename() + " WHERE settings_name = ?")) {
            ps.setString(1, name);
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next())
                    return resultSet.getString("settings_value");
            }
        } catch (SQLException throwables) {
            SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to get the plugin settings.", throwables);
        }
        return null;
    }

    public void setPluginSetting(String name, String value) throws SQLException {
        try (Connection connection = getConnector().getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT INTO " + getTablename() + " (settings_name, settings_value) VALUES (?, ?) " +
                             "ON DUPLICATE KEY UPDATE settings_value = VALUES (settings_value)")) {
            ps.setString(1, name);
            ps.setString(2, value);
            ps.execute();
        }
    }
}
