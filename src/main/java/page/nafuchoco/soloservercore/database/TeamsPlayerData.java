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

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class TeamsPlayerData extends PlayerData {
    private UUID teamOwner;
    private List<UUID> members;

    public TeamsPlayerData(UUID id, String playerName, String spawnLocation, Date lastJoined, UUID joinedTeam, UUID teamOwner, List<UUID> members) {
        super(id, playerName, spawnLocation, lastJoined, joinedTeam);
        this.teamOwner = teamOwner;
        this.members = members;
    }

    public UUID getTeamOwner() {
        return teamOwner;
    }

    public List<UUID> getMembers() {
        return members;
    }
}
