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

package dev.nafusoft.soloservercore;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import dev.nafusoft.soloservercore.command.*;
import dev.nafusoft.soloservercore.data.InGameSSCPlayer;
import dev.nafusoft.soloservercore.data.MoveTimeUpdater;
import dev.nafusoft.soloservercore.data.TempSSCPlayer;
import dev.nafusoft.soloservercore.database.*;
import dev.nafusoft.soloservercore.event.player.PlayerPeacefulModeChangeEvent;
import dev.nafusoft.soloservercore.listener.*;
import dev.nafusoft.soloservercore.listener.internal.PlayersTeamEventListener;
import dev.nafusoft.soloservercore.packet.ServerInfoPacketEventListener;
import dev.nafusoft.soloservercore.team.chest.TeamChestManager;
import io.papermc.lib.PaperLib;
import lombok.val;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.regex.Pattern;

public final class SoloServerCore extends JavaPlugin implements Listener {
    private static SoloServerCore instance;
    private SoloServerCoreConfig config;
    private DatabaseConnector connector;
    private PluginSettingsTable pluginSettingsTable;
    private PlayersTable playersTable;
    private PlayersTeamsTable playersTeamsTable;
    private MessagesTable messagesTable;
    private ChestsTable chestsTable;
    private PluginSettingsManager pluginSettingsManager;
    private TeamChestManager teamChestManager;
    private ProtocolManager protocolManager;
    private CoreProtectAPI coreProtectAPI;

    public static SoloServerCore getInstance() {
        if (instance == null)
            instance = (SoloServerCore) Bukkit.getServer().getPluginManager().getPlugin("SoloServerCore");
        return instance;
    }

    public static String getMessage(Player player, String index) {
        if (player.hasPermission("mofucraft.english"))
            return MessageManager.getMessage("en_US", index);
        return MessageManager.getMessage(index);
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        config = new SoloServerCoreConfig();
        getCoreConfig().reloadConfig();

        // Check PaperMC.
        PaperLib.suggestPaper(this);

        getLogger().info("Start the initialization process. This may take some time.");

        // Database Init
        connector = new DatabaseConnector(getCoreConfig().getInitConfig().getDatabaseType(),
                getCoreConfig().getInitConfig().getAddress() + ":" + getCoreConfig().getInitConfig().getPort(),
                getCoreConfig().getInitConfig().getDatabase(),
                getCoreConfig().getInitConfig().getUsername(),
                getCoreConfig().getInitConfig().getPassword(),
                getCoreConfig().getInitConfig().getTablePrefix());
        pluginSettingsTable = new PluginSettingsTable("settings", connector);
        playersTable = new PlayersTable("players", connector);
        playersTeamsTable = new PlayersTeamsTable("teams", connector);
        messagesTable = new MessagesTable("messages", connector);
        chestsTable = new ChestsTable("chests", connector);
        try {
            pluginSettingsTable.createTable();
            playersTable.createTable();
            playersTeamsTable.createTable();
            messagesTable.createTable();
            chestsTable.createTable();
        } catch (SQLException e) {
            getInstance().getLogger().log(Level.WARNING, "An error occurred while initializing the database table.", e);
        }

        pluginSettingsManager = new PluginSettingsManager(pluginSettingsTable);
        teamChestManager = new TeamChestManager(chestsTable);
        migrateDatabase();

        // ProtocolLib Init
        try {
            protocolManager = ProtocolLibrary.getProtocolManager();
            protocolManager.addPacketListener(new ServerInfoPacketEventListener());
        } catch (NoClassDefFoundError e) {
            getLogger().info("Relevant functions have been disabled because ProtocolLib has not been installed.");
        }

        // CoreProtect Init
        val coreProtect = (CoreProtect) getServer().getPluginManager().getPlugin("CoreProtect");
        coreProtectAPI = coreProtect.getAPI();
        if (pluginSettingsManager.isCheckBlock())
            getServer().getPluginManager().registerEvents(new BlockEventListener(new CoreProtectClient(coreProtectAPI), pluginSettingsManager), this);

        getServer().getPluginManager().registerEvents(new PlayersTeamEventListener(
                        playersTable,
                        playersTeamsTable,
                        pluginSettingsManager,
                        messagesTable),
                this);
        getServer().getPluginManager().registerEvents(new PlayerBedEventListener(pluginSettingsManager), this);
        getServer().getPluginManager().registerEvents(new PlayerRespawnEventListener(), this);
        getServer().getPluginManager().registerEvents(new AsyncPlayerChatEventListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathEventListener(), this);
        getServer().getPluginManager().registerEvents(new MoveTimeUpdater(), this);
        getServer().getPluginManager().registerEvents(new PeacefulModeEventListener(), this);
        getServer().getPluginManager().registerEvents(teamChestManager, this);
        getServer().getPluginManager().registerEvents(this, this);

        // Command Register
        val settingsCommand = new SettingsCommand(pluginSettingsManager);
        val teamCommand = new TeamCommand(pluginSettingsManager);
        val maintenanceCommand = new MaintenanceCommand(playersTable, playersTeamsTable, pluginSettingsManager);
        val messageCommand = new MessageCommand();
        getCommand("settings").setExecutor(settingsCommand);
        getCommand("settings").setTabCompleter(settingsCommand);
        getCommand("team").setExecutor(teamCommand);
        getCommand("team").setTabCompleter(teamCommand);
        getCommand("maintenance").setExecutor(maintenanceCommand);
        getCommand("messageboard").setExecutor(messageCommand);
        getCommand("messageboard").setTabCompleter(messageCommand);
        getCommand("teamchest").setExecutor(new ChestCommand(teamChestManager));
    }

    private void migrateDatabase() {
        val versionPattern =
                Pattern.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)" +
                        "(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)" +
                        "(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))" +
                        "?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");

        // 初起動は除外する
        var doMigrate = true;
        val lastMigratedVersion = pluginSettingsManager.getLastMigratedVersion();
        getLogger().log(Level.INFO, "Starting database migrate check... Now database version: {0}", lastMigratedVersion);
        if (lastMigratedVersion.equals("0.0.0")) {
            try {
                if (playersTable.getPlayers().isEmpty()) {
                    doMigrate = false;
                    pluginSettingsManager.setLastMigratedVersion(getDescription().getVersion());
                }
            } catch (SQLException e) {
                // nothing...
            }
        }

        if (doMigrate) {
            // Migrationコンフィグの読み込み
            val migrateConfig =
                    YamlConfiguration.loadConfiguration(new InputStreamReader(getResource("migrate.yml")));
            int latest;
            val matcher = versionPattern.matcher(lastMigratedVersion);
            if (matcher.matches()) {
                val major = Integer.parseInt(matcher.group(1));
                val minor = Integer.parseInt(matcher.group(2));
                val patch = Integer.parseInt(matcher.group(3));
                latest = major * 10000 + minor * 100 + patch;
            } else {
                latest = 0;
            }

            val migration = migrateConfig.getMapList("migrate").stream()
                    .filter(map -> {
                        val version = (String) map.get("version");
                        val matcher1 = versionPattern.matcher(version);
                        if (matcher1.matches()) {
                            val major = Integer.parseInt(matcher1.group(1));
                            val minor = Integer.parseInt(matcher1.group(2));
                            val patch = Integer.parseInt(matcher1.group(3));
                            return latest < major * 10000 + minor * 100 + patch;
                        } else {
                            return false;
                        }
                    })
                    .sorted(Comparator.comparingInt(m -> {
                        val matcher2 = versionPattern.matcher((String) m.get("version"));
                        if (matcher2.matches()) {
                            val major = Integer.parseInt(matcher2.group(1));
                            val minor = Integer.parseInt(matcher2.group(2));
                            val patch = Integer.parseInt(matcher2.group(3));
                            return major * 10000 + minor * 100 + patch;
                        }
                        return 0;
                    }))
                    .distinct()
                    .toList();

            doMigrate = migration.size() > 0;

            if (doMigrate)
                getLogger().info("""
                        Find migration script, Start migration.
                        The database structure has been updated. Start the migration process.
                        """);
            migration.stream().map(m -> (List<String>) m.get("scripts")).forEach(process -> {
                process.forEach(script -> {
                    try (Connection connection = connector.getConnection();
                         PreparedStatement ps = connection.prepareStatement(
                                 script.replace("%PREFIX%", connector.getPrefix())
                         )) {
                        ps.execute();
                    } catch (SQLException e) {
                        getLogger().log(Level.WARNING, "An error has occurred during the migration process.", e);
                    }
                });
            });

            try {
                pluginSettingsManager.setLastMigratedVersion(getDescription().getVersion());
                getLogger().info("Migration process is completed.");
            } catch (SQLException e) {
                getLogger().log(Level.WARNING,
                        """
                                The migration process was completed successfully, 
                                but the results could not be saved. 
                                An error may be displayed the next time you start the program.
                                """,
                        e);
            }
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        teamChestManager.saveAllOpenedChest();

        if (connector != null)
            connector.close();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("soloservercore." + command.getName())) {
            sender.sendMessage(ChatColor.RED + "You can't run this command because you don't have permission.");
        } else if (sender instanceof Player player
                && SoloServerApi.getInstance().getSSCPlayer(player) instanceof TempSSCPlayer) {
            return true;
        } else switch (command.getName()) {
            case "spawn" -> {
                if (sender instanceof Player player) {
                    player.teleport(SoloServerApi.getInstance().getPlayerSpawn(player));
                } else {
                    sender.sendMessage("This command must be executed in-game.");
                }
            }

            case "home" -> {
                if (sender instanceof Player player) {
                    if (args.length >= 1) {
                        if (args[0].equals("fixed")) {
                            var location = player.getBedSpawnLocation();
                            if (location != null) {
                                SoloServerApi.getInstance().getSSCPlayer(player).setFixedHomeLocation(location);
                                try {
                                    getPlayersTable().updateFixedHome(player.getUniqueId(),
                                            SoloServerApi.getInstance().getSSCPlayer(player).getFixedHomeLocation());
                                } catch (SQLException e) {
                                    getLogger().log(Level.WARNING, "Failed to update the player data.", e);
                                }
                                player.sendMessage(getMessage(player, "command.home.fixed.set"));
                            } else {
                                player.sendMessage(getMessage(player, "command.home.fixed.set.warn"));
                            }
                        } else if (args[0].equals("reset")) {
                            SoloServerApi.getInstance().getSSCPlayer(player).setFixedHomeLocation(player.getBedSpawnLocation());
                            try {
                                getPlayersTable().updateFixedHome(player.getUniqueId(),
                                        SoloServerApi.getInstance().getSSCPlayer(player).getFixedHomeLocation());
                            } catch (SQLException e) {
                                getLogger().log(Level.WARNING, "Failed to update the player data.", e);
                            }
                            player.sendMessage(getMessage(player, "command.home.fixed.reset"));
                        }
                    } else {
                        var location = SoloServerApi.getInstance().getSSCPlayer(player).getFixedHomeLocationObject();
                        if (location == null) // 固定Home設定がない場合
                            location = player.getBedSpawnLocation();
                        if (location == null) // Bedスポーンがない場合
                            location = SoloServerApi.getInstance().getPlayerSpawn(player);
                        player.teleport(location);
                    }
                } else {
                    sender.sendMessage("This command must be executed in-game.");
                }
            }

            case "peaceful" -> {
                if (sender instanceof Player player) {
                    val sscPlayer = SoloServerApi.getInstance().getSSCPlayer(player);
                    sscPlayer.setPeacefulMode(!sscPlayer.isPeacefulMode());
                    player.sendMessage(MessageManager.format(getMessage(player, "command.peaceful.change"), sscPlayer.isPeacefulMode()));
                }
            }

            case "reteleport" -> {
                if (sender instanceof Player player) {
                    if (!sender.hasPermission("soloservercore.reteleport")) {
                        sender.sendMessage(ChatColor.RED + "You can't run this command because you don't have permission.");
                    } else if (args.length == 0) {
                        val sscPlayer = SoloServerApi.getInstance().getSSCPlayer(player);
                        if (!(sscPlayer instanceof TempSSCPlayer)) {
                            if (!sscPlayer.getSpawnLocationObject().getWorld().equals(Bukkit.getWorld(config.getInitConfig().getSpawnWorld()))) {
                                sender.sendMessage(SoloServerCore.getMessage(player, "command.teleport.new-world.confirm"));
                            } else {
                                sender.sendMessage(SoloServerCore.getMessage(player, "command.teleport.new-world.warn.notfound"));
                            }
                        }
                    } else if ("confirm".equalsIgnoreCase(args[0])) {
                        val sscPlayer = SoloServerApi.getInstance().getOfflineSSCPlayer(player.getUniqueId());

                        // チーム情報を確認し所属している場合は脱退
                        if (sscPlayer.getJoinedTeam() != null) {
                            sscPlayer.getJoinedTeam().leaveTeam(player);
                            player.sendMessage(SoloServerCore.getMessage(player, "teams.leave"));
                        }

                        // ベッドスポーンの上書き
                        player.setBedSpawnLocation(null);

                        // プレイヤーの初期化
                        if (getPluginSettingsManager().isReteleportResetAll()) {
                            player.getInventory().clear();
                            player.getEnderChest().clear();
                            player.setLevel(0);
                            player.setExp(0F);
                            player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType()));
                            player.setFireTicks(0);
                            player.setHealth(20D);
                            player.setFoodLevel(20);
                            player.setSaturation(20F);
                        }

                        // プレイヤーを切断
                        player.kickPlayer(SoloServerCore.getMessage(player, "command.teleport.new-world.in-process"));

                        // 旧プレイヤーデータの削除
                        try {
                            playersTable.deletePlayer(sscPlayer);
                        } catch (SQLException e) {
                            SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to delete the player data.", e);
                        }

                        SoloServerApi.getInstance().registerTempPlayer(new TempSSCPlayer(player));
                        AsyncLoginManager.login(player);
                    }
                } else {
                    sender.sendMessage("This command must be executed in-game.");
                }
            }

            default -> {
                return false;
            }
        }
        return true;
    }

    @EventHandler
    public void onPlayerLoginEvent(PlayerLoginEvent event) {
        SoloServerApi.getInstance().registerTempPlayer(new TempSSCPlayer(event.getPlayer()));
        AsyncLoginManager.login(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        event.setJoinMessage("");

        CompletableFuture<AsyncLoginManager.LoginResult> future = AsyncLoginManager.getLoginResult(event.getPlayer());
        future.thenAccept(result -> Bukkit.getScheduler().callSyncMethod(this, () -> {
            getLogger().info("Login result: " + result);
            if (result.status() == AsyncLoginManager.ResultStatus.FAILED) {
                event.getPlayer().kickPlayer(result.message());
                return result;
            }

            if (result.status() != AsyncLoginManager.ResultStatus.FIRST_JOINED)
                SoloServerApi.getInstance().dropStoreData(event.getPlayer());

            var sscPlayer = SoloServerApi.getInstance().getSSCPlayer(event.getPlayer());
            if (result.status() == AsyncLoginManager.ResultStatus.FIRST_JOINED) {
                SoloServerApi.getInstance().dropStoreData(event.getPlayer());
                sscPlayer = new InGameSSCPlayer(event.getPlayer().getUniqueId(),
                        sscPlayer.getSpawnLocationObject(),
                        null,
                        event.getPlayer(),
                        true,
                        null,
                        false);

                try {
                    SoloServerApi.getInstance().registerSSCPlayer(sscPlayer);
                } catch (SQLException | NullPointerException exception) {
                    SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to save the player data.\n" +
                            "New data will be regenerated next time.", exception);
                }

                // MVとの競合に対する対策
                InGameSSCPlayer finalSscPlayer = sscPlayer;
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SoloServerCore.getInstance(),
                        () -> {
                            val location = finalSscPlayer.getSpawnLocationObject();
                            event.getPlayer().teleport(location);
                            Object[] perms = {event.getPlayer().getName(),
                                    location.getBlockX(),
                                    location.getBlockY(),
                                    location.getBlockZ()};
                            SoloServerCore.getInstance().getLogger().log(Level.INFO,
                                    "{0} has been successfully teleported to {1}, {2}, {3}", perms);
                            event.getPlayer().sendMessage(SoloServerCore.getMessage(event.getPlayer(), "system.world.moved"));
                        }, 10L);
                event.getPlayer().setCompassTarget(sscPlayer.getSpawnLocationObject());
            }

            val joinedTeam = SoloServerApi.getInstance().getPlayersTeam(event.getPlayer());
            List<UUID> member;
            if (joinedTeam != null) {
                member = new ArrayList<>(joinedTeam.getMembers());
                member.add(joinedTeam.getOwner());
            } else {
                member = new ArrayList<>();
            }

            List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
            players.forEach(player -> {
                if (!player.equals(event.getPlayer()) && !member.contains(player.getUniqueId())) {
                    if (!event.getPlayer().hasPermission("soloservercore.invisible.bypass"))
                        event.getPlayer().hidePlayer(SoloServerCore.getInstance(), player);
                    if (!player.hasPermission("soloservercore.invisible.bypass"))
                        player.hidePlayer(SoloServerCore.getInstance(), event.getPlayer());
                }
            });

            if (member.contains(event.getPlayer().getUniqueId()))
                member.forEach(m -> {
                    var player = Bukkit.getPlayer(m);
                    if (player != null && !player.equals(event.getPlayer()))
                        player.sendMessage(MessageManager.format(getMessage(player, "teams.login"), player.getDisplayName()));
                });

            if (joinedTeam != null) {
                val newMessages = joinedTeam.getMessages().stream()
                        .filter(message -> message.getSentDate().getTime() > event.getPlayer().getLastPlayed()).toList();
                if (!newMessages.isEmpty()) {
                    event.getPlayer().sendMessage(ChatColor.AQUA + "====== New Team Message! ======");
                    newMessages.forEach(message -> {
                        TextComponent component = new TextComponent();
                        component.setText("[" + message.getId().toString().split("-")[0] + "] ");
                        component.setBold(true);
                        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/messageboard read " + message.getId()));
                        component.addExtra(message.getSubject());
                        event.getPlayer().spigot().sendMessage(component);
                    });
                }
            }

            if (!sscPlayer.getSpawnLocationObject().getWorld().getName().equals(SoloServerApi.getInstance().getSpawnWorld()))
                event.getPlayer().sendMessage(getMessage(event.getPlayer(), "system.world.new"));

            return result;
        }));
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        event.setQuitMessage("");

        SoloServerApi.getInstance().dropStoreData(event.getPlayer());
        if (!Bukkit.getOnlinePlayers().isEmpty())
            Bukkit.getOnlinePlayers().forEach(player -> player.showPlayer(this, event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPeacefulModeChangeEvent(PlayerPeacefulModeChangeEvent event) {
        try {
            playersTable.updatePeacefulMode(event.getPlayer().getId(), event.getPlayer().isPeacefulMode());
            if (event.getPlayer().isPeacefulMode()) {
                event.getBukkitPlayer().getNearbyEntities(40, 40, 40).forEach(
                        entity -> {
                            // 既にターゲット中のMobのターゲットを解除
                            if (entity instanceof Monster monster
                                    && monster.getTarget() instanceof Player target
                                    && target.equals(event.getBukkitPlayer()))
                                monster.setTarget(null);
                        }
                );
            }
        } catch (SQLException e) {
            SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to update the player data.", e);
        }
    }


    public SoloServerCoreConfig getCoreConfig() {
        return config;
    }

    PluginSettingsManager getPluginSettingsManager() {
        return pluginSettingsManager;
    }

    DatabaseConnector getConnector() {
        return connector;
    }

    PlayersTable getPlayersTable() {
        return playersTable;
    }

    PlayersTeamsTable getPlayersTeamsTable() {
        return playersTeamsTable;
    }

    PluginSettingsTable getPluginSettingsTable() {
        return pluginSettingsTable;
    }

    MessagesTable getMessagesTable() {
        return messagesTable;
    }
}
