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

import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class PluginSettingsManager {
    private static final String[] settingsKeys =
            new String[]{"checkBlock", "protectionPeriod", "teamSpawnCollect", "stockSpawnPoint", "broadcastBedCount", "useAfkCount", "afkTimeThreshold", "reteleportResetAll", "lastMigratedVersion"};
    // Default Value
    private static final boolean CHECK_BLOCK = true;
    private static final int PROTECTION_PERIOD = 259200;
    private static final boolean TEAM_SPAWN_COLLECT = true;
    private static final boolean BROADCAST_BED_COUNT = true;
    private static final boolean USE_AFK_COUNT = false;
    private static final int AFK_TIME_THRESHOLD = 30;
    private static final boolean RETELEPORT_RESET_ALL = false;
    private static final int LAST_MIGRATED_VERSION = 350;
    private final PluginSettingsTable settingsTable;

    public PluginSettingsManager(PluginSettingsTable settingsTable) {
        this.settingsTable = settingsTable;
    }

    public static String[] getSettingsKeys() {
        return settingsKeys;
    }

    public boolean isCheckBlock() {
        val value = settingsTable.getPluginSetting("checkBlock");
        if (value == null)
            return CHECK_BLOCK;
        else
            return Boolean.parseBoolean(value);
    }

    public void setCheckBlock(@NotNull boolean checkBlock) throws SQLException {
        settingsTable.setPluginSetting("checkBlock", String.valueOf(checkBlock));
    }

    public int getProtectionPeriod() {
        val value = settingsTable.getPluginSetting("protectionPeriod");
        if (value == null)
            return PROTECTION_PERIOD;
        else
            return Integer.parseInt(value);
    }

    public void setProtectionPeriod(@NotNull int protectionPeriod) throws SQLException {
        settingsTable.setPluginSetting("protectionPeriod", String.valueOf(protectionPeriod));
    }

    public boolean isTeamSpawnCollect() {
        val value = settingsTable.getPluginSetting("teamSpawnCollect");
        if (value == null)
            return TEAM_SPAWN_COLLECT;
        else
            return Boolean.parseBoolean(value);
    }

    public void setTeamSpawnCollect(@NotNull boolean teamSpawnCollect) throws SQLException {
        settingsTable.setPluginSetting("teamSpawnCollect", String.valueOf(teamSpawnCollect));
    }

    public boolean isBroadcastBedCount() {
        val value = settingsTable.getPluginSetting("broadcastBedCount");
        if (value == null)
            return BROADCAST_BED_COUNT;
        else
            return Boolean.parseBoolean(value);
    }

    public void setBroadcastBedCount(@NotNull boolean broadcastBedCount) throws SQLException {
        settingsTable.setPluginSetting("broadcastBedCount", String.valueOf(broadcastBedCount));
    }

    public boolean isUseAfkCount() {
        val value = settingsTable.getPluginSetting("useAfkCount");
        if (value == null)
            return USE_AFK_COUNT;
        else
            return Boolean.parseBoolean(value);
    }

    public void setUseAfkCount(@NotNull boolean useAfkCount) throws SQLException {
        settingsTable.setPluginSetting("useAfkCount", String.valueOf(useAfkCount));
    }

    public int getAfkTimeThreshold() {
        val value = settingsTable.getPluginSetting("afkTimeThreshold");
        if (value == null)
            return AFK_TIME_THRESHOLD;
        else
            return Integer.parseInt(value);
    }

    public void setAfkTimeThreshold(@NotNull int afkTimeThreshold) throws SQLException {
        settingsTable.setPluginSetting("afkTimeThreshold", String.valueOf(afkTimeThreshold));
    }

    public boolean isReteleportResetAll() {
        val value = settingsTable.getPluginSetting("reteleportResetAll");
        if (value == null)
            return RETELEPORT_RESET_ALL;
        else
            return Boolean.parseBoolean(value);
    }

    public void setReteleportResetAll(@NotNull boolean reteleportResetAll) throws SQLException {
        settingsTable.setPluginSetting("reteleportResetAll", String.valueOf(reteleportResetAll));
    }

    public int getLastMigratedVersion() {
        val value = settingsTable.getPluginSetting("lastMigratedVersion");
        var result = LAST_MIGRATED_VERSION;
        try {
            if (value != null)
                result = Integer.parseInt(value.replaceAll("\\.", ""));
        } catch (NumberFormatException e) {
            // nothing
        }
        return result;
    }

    public void setLastMigratedVersion(@NotNull String lastMigratedVersion) throws SQLException {
        settingsTable.setPluginSetting("lastMigratedVersion", lastMigratedVersion);
    }
}
