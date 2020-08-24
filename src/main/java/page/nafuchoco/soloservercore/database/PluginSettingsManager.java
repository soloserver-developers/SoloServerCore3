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

public class PluginSettingsManager {
    private final PluginSettingsTable settingsTable;

    // Default Value
    private final boolean checkBlock = true;
    private final int protectionPeriod = 259200;
    private final boolean teamSpawnCollect = true;

    public PluginSettingsManager(PluginSettingsTable settingsTable) {
        this.settingsTable = settingsTable;
    }

    public boolean isCheckBlock() {
        String value = settingsTable.getPluginSetting("checkBlock");
        if (value == null)
            return checkBlock;
        else
            return Boolean.parseBoolean(value);
    }

    public int getProtectionPeriod() {
        String value = settingsTable.getPluginSetting("protectionPeriod");
        if (value == null)
            return protectionPeriod;
        else
            return Integer.parseInt(value);
    }

    public boolean isTeamSpawnCollect() {
        String value = settingsTable.getPluginSetting("teamSpawnCollect");
        if (value == null)
            return teamSpawnCollect;
        else
            return Boolean.parseBoolean(value);
    }
}
