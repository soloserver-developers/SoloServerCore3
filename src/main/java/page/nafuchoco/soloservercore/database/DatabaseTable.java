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
import java.sql.SQLException;

public abstract class DatabaseTable {
    private static final String PREFIX = SoloServerCore.getCoreConfig().getInitConfig().getTablePrefix();
    private final String tablename;
    private final DatabaseConnector connector;

    public DatabaseTable(String tablename, DatabaseConnector connector) {
        this.tablename = PREFIX + tablename;
        this.connector = connector;
    }

    protected String getTablename() {
        return tablename;
    }

    protected DatabaseConnector getConnector() {
        return connector;
    }

    /**
     * Creates a table with the specified structure.
     * If a table with the same name already exists, it exits without executing the process.
     *
     * @param construction Structure of the table to be created
     * @throws SQLException Thrown when creating a table fails.
     */
    public void createTable(String construction) throws SQLException {
        try (Connection connection = connector.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS " + tablename + " (" + construction + ")")) {
                ps.execute();
            }
        }
    }
}
