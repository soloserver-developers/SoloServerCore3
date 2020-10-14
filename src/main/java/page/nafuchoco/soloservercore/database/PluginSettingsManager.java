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

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class PluginSettingsManager {
    private final PluginSettingsTable settingsTable;

    // Default Value
    private static final boolean CHECK_BLOCK = true;
    private static final int PROTECTION_PERIOD = 259200;
    private static final boolean TEAM_SPAWN_COLLECT = true;
    private static final int STOCK_SPAWN_POINT = 100;
    private static final boolean BROADCAST_BED_COUNT = true;

    public PluginSettingsManager(PluginSettingsTable settingsTable) {
        this.settingsTable = settingsTable;
    }

    public boolean isCheckBlock() {
        String value = settingsTable.getPluginSetting("checkBlock");
        if (value == null)
            return CHECK_BLOCK;
        else
            return Boolean.parseBoolean(value);
    }

    public int getProtectionPeriod() {
        String value = settingsTable.getPluginSetting("protectionPeriod");
        if (value == null)
            return PROTECTION_PERIOD;
        else
            return Integer.parseInt(value);
    }

    public boolean isTeamSpawnCollect() {
        String value = settingsTable.getPluginSetting("teamSpawnCollect");
        if (value == null)
            return TEAM_SPAWN_COLLECT;
        else
            return Boolean.parseBoolean(value);
    }

    public int getStockSpawnPoint() {
        String value = settingsTable.getPluginSetting("stockSpawnPoint");
        if (value == null)
            return STOCK_SPAWN_POINT;
        else
            return Integer.parseInt(value);
    }

    public boolean isBroadcastBedCount() {
        String value = settingsTable.getPluginSetting("broadcastBedCount");
        if (value == null)
            return BROADCAST_BED_COUNT;
        else
            return Boolean.parseBoolean(value);
    }

    public void setCheckBlock(@NotNull boolean checkBlock) throws SQLException {
        settingsTable.setPluginSetting("checkBlock", String.valueOf(checkBlock));
    }

    public void setProtectionPeriod(@NotNull int protectionPeriod) throws SQLException {
        settingsTable.setPluginSetting("protectionPeriod", String.valueOf(protectionPeriod));
    }

    public void setTeamSpawnCollect(@NotNull boolean teamSpawnCollect) throws SQLException {
        settingsTable.setPluginSetting("teamSpawnCollect", String.valueOf(teamSpawnCollect));
    }

    public void setStockSpawnPoint(@NotNull int stockSpawnPoint) throws SQLException {
        settingsTable.setPluginSetting("stockSpawnPoint", String.valueOf(stockSpawnPoint));
    }

    public void setBroadcastBedCount(@NotNull boolean broadcastBedCount) throws SQLException {
        settingsTable.setPluginSetting("broadcastBedCount", String.valueOf(broadcastBedCount));
    }

}
