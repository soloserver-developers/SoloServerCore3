/*
 * Copyright 2021 NAFU_at
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

package page.nafuchoco.soloservercore.event.team;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import page.nafuchoco.soloservercore.data.InGameSSCPlayer;
import page.nafuchoco.soloservercore.data.PlayersTeam;

public class PlayersTeamStatusUpdateEvent extends PlayersTeamEvent {
    private static final HandlerList handlers = new HandlerList();
    private final UpdatedState state;
    private final Object before;
    private final Object after;

    public PlayersTeamStatusUpdateEvent(PlayersTeam playersTeam, Player player, UpdatedState state, Object before, Object after) {
        super(playersTeam, player);
        this.state = state;
        this.before = before;
        this.after = after;
    }

    public PlayersTeamStatusUpdateEvent(PlayersTeam playersTeam, InGameSSCPlayer player, UpdatedState state, Object before, Object after) {
        super(playersTeam, player);
        this.state = state;
        this.before = before;
        this.after = after;
    }

    public UpdatedState getState() {
        return state;
    }

    public Object getBefore() {
        return before;
    }

    public Object getAfter() {
        return after;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public enum UpdatedState {
        NAME, OWNER
    }
}
