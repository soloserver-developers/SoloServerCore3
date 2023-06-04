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

package dev.nafusoft.soloservercore.data;

import dev.nafusoft.soloservercore.event.team.*;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayersTeam {
    private final UUID id;
    private final UUID owner;

    private String teamName;
    private List<UUID> members = new ArrayList<>();

    private List<TeamMessage> messages = new ArrayList<>();

    public PlayersTeam(UUID id, UUID owner) {
        this.id = id;
        this.owner = owner;
    }

    /**
     * チームのIDを返します。
     *
     * @return チームID
     */
    @NotNull
    public UUID getId() {
        return id;
    }

    /**
     * チームオーナーのUUIDを返します。
     *
     * @return チームオーナーのプレイヤーUUID
     */
    @NotNull
    public UUID getOwner() {
        return owner;
    }

    /**
     * チーム名を返します。
     *
     * @return チーム名 もしくは Null
     */
    @Nullable
    public String getTeamName() {
        return teamName;
    }

    /**
     * チーム名を設定します。
     *
     * @param teamName チーム名
     * @deprecated このメソッドはデータベースとの同期を行わず、データの不整合が発生する可能性があります。
     * 通常は{@link #setTeamName(String)}を使用してください。
     */
    @Deprecated
    public void setTeamName(@Nullable String teamName) {
        this.teamName = teamName;
    }

    /**
     * チームの表示名を返します。
     *
     * @return チームの表示名
     * @since v6.0
     */
    @NotNull
    public String getTeamDisplayName() {
        return teamName == null ? getId().toString().split("-")[0] : teamName;
    }

    /**
     * チームのメンバー一覧を返します。
     *
     * @return チームのメンバー一覧
     */
    @NotNull
    public List<UUID> getMembers() {
        return members;
    }

    /**
     * チームメンバーの一覧を設定します。
     *
     * @param members メンバー一覧のList
     * @deprecated このメソッドはデータベースとの同期を行わず、データの不整合が発生する可能性があります。
     * 通常は{@link #joinTeam(Player)}, {@link #leaveTeam(Player)}を使用してください。
     */
    @Deprecated
    public void setMembers(@NotNull List<UUID> members) {
        this.members = members;
    }

    /**
     * 登録されているチームメッセージ一覧を返します。
     *
     * @return 登録されているチームメッセージ一覧
     * @since v4.5
     */
    @NotNull
    public List<TeamMessage> getMessages() {
        return messages;
    }

    /**
     * チームメッセージを登録します。
     *
     * @param messages チームメッセージ一覧のList
     * @since v4.5
     * @deprecated このメソッドはデータベースとの同期を行わず、データの不整合が発生する可能性があります。
     * 通常は{@link #addTeamMessage(TeamMessage)}, {@link #deleteTeamMessage(TeamMessage)}を使用してください。
     */
    @Deprecated
    public void setTeamMessages(@NotNull List<TeamMessage> messages) {
        this.messages = messages;
    }

    /**
     * チーム名を変更します。
     *
     * @param player   変更を実行したプレイヤー
     * @param teamName 変更するチーム名
     */
    public void updateTeamName(Player player, String teamName) {
        val before = getTeamName();
        setTeamName(teamName);
        val statusUpdateEvent = new PlayersTeamStatusUpdateEvent(this, player, PlayersTeamStatusUpdateEvent.UpdatedState.NAME, before, teamName);
        Bukkit.getServer().getPluginManager().callEvent(statusUpdateEvent);
    }

    /**
     * チームにメンバーを参加させます。
     *
     * @param player 参加させるプレイヤー
     */
    public void joinTeam(Player player) {
        members.add(player.getUniqueId());
        val joinEvent = new PlayersTeamJoinEvent(this, player);
        Bukkit.getServer().getPluginManager().callEvent(joinEvent);
        if (joinEvent.isCancelled())
            members.remove(player.getUniqueId());
    }

    /**
     * チームからメンバーを脱退させます。
     *
     * @param player 脱退させるプレイヤー
     */
    public void leaveTeam(Player player) {
        if (player.getUniqueId().equals(owner)) {
            val disappearanceEvent = new PlayersTeamDisappearanceEvent(this, player);
            Bukkit.getServer().getPluginManager().callEvent(disappearanceEvent);
        } else {
            members.remove(player.getUniqueId());
            val leaveEvent = new PlayersTeamLeaveEvent(this, player);
            Bukkit.getServer().getPluginManager().callEvent(leaveEvent);
            if (leaveEvent.isCancelled())
                members.add(player.getUniqueId());
        }
    }

    /**
     * チームメッセージを登録します。
     *
     * @param message 登録するチームメッセージ
     * @since v4.5
     */
    public void addTeamMessage(TeamMessage message) {
        messages.add(message);
        val messageCreateEvent =
                new PlayersTeamMessageCreateEvent(this, Bukkit.getPlayer(message.getSenderPlayer()), message);
        Bukkit.getServer().getPluginManager().callEvent(messageCreateEvent);
    }

    /**
     * チームメッセージを削除します。
     *
     * @param message 削除するチームメッセージ
     * @since v4.5
     */
    public void deleteTeamMessage(TeamMessage message) {
        messages.remove(message);
        val messageDeleteEvent =
                new PlayersTeamMessageDeleteEvent(this, Bukkit.getPlayer(message.getSenderPlayer()), message);
        Bukkit.getServer().getPluginManager().callEvent(messageDeleteEvent);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof PlayersTeam team) {
            return id.equals(team.id);
        } else {
            return false;
        }
    }
}
