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

package page.nafuchoco.soloservercore;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import lombok.val;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import page.nafuchoco.soloservercore.command.*;
import page.nafuchoco.soloservercore.data.InGameSSCPlayer;
import page.nafuchoco.soloservercore.data.MoveTimeUpdater;
import page.nafuchoco.soloservercore.database.*;
import page.nafuchoco.soloservercore.event.player.PlayerMoveToNewWorldEvent;
import page.nafuchoco.soloservercore.listener.*;
import page.nafuchoco.soloservercore.listener.internal.PlayersTeamEventListener;
import page.nafuchoco.soloservercore.packet.ServerInfoPacketEventListener;

import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class SoloServerCore extends JavaPlugin implements Listener {
    private static SoloServerCore instance;

    private SoloServerCoreConfig config;

    private PluginSettingsTable pluginSettingsTable;
    private PlayersTable playersTable;
    private PlayersTeamsTable playersTeamsTable;
    private MessagesTable messagesTable;

    private PluginSettingsManager pluginSettingsManager;
    private SpawnPointLoader spawnPointLoader;
    private ProtocolManager protocolManager;
    private DatabaseConnector connector;
    private CoreProtectAPI coreProtectAPI;

    public static SoloServerCore getInstance() {
        if (instance == null)
            instance = (SoloServerCore) Bukkit.getServer().getPluginManager().getPlugin("SoloServerCore");
        return instance;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        config = new SoloServerCoreConfig();
        getCoreConfig().reloadConfig();

        getLogger().info("Start the initialization process. This may take some time.");

        // Database Init
        connector = new DatabaseConnector(getCoreConfig().getInitConfig().getDatabaseType(),
                getCoreConfig().getInitConfig().getAddress() + ":" + getCoreConfig().getInitConfig().getPort(),
                getCoreConfig().getInitConfig().getDatabase(),
                getCoreConfig().getInitConfig().getUsername(),
                getCoreConfig().getInitConfig().getPassword());
        pluginSettingsTable = new PluginSettingsTable("settings", connector);
        playersTable = new PlayersTable("players", connector);
        playersTeamsTable = new PlayersTeamsTable("teams", connector);
        messagesTable = new MessagesTable("messages", connector);
        try {
            pluginSettingsTable.createTable();
            playersTable.createTable();
            playersTeamsTable.createTable();
            messagesTable.createTable();
        } catch (SQLException e) {
            getInstance().getLogger().log(Level.WARNING, "An error occurred while initializing the database table.", e);
        }

        pluginSettingsManager = new PluginSettingsManager(pluginSettingsTable);

        migrateDatabase();

        // Generate random world spawn point
        getLogger().info("Pre-generate spawn points. This often seems to freeze the system, but for the most part it is normal.");
        val world = Bukkit.getWorld(config.getInitConfig().getSpawnWorld());
        spawnPointLoader = new SpawnPointLoader(
                pluginSettingsManager,
                new SpawnPointGenerator(world, config.getInitConfig().getGenerateLocationRange()));
        spawnPointLoader.initPoint(true);
    }

    protected void init() {
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
                        messagesTable,
                        spawnPointLoader),
                this);
        getServer().getPluginManager().registerEvents(new PlayerBedEventListener(pluginSettingsManager), this);
        getServer().getPluginManager().registerEvents(new PlayerRespawnEventListener(spawnPointLoader), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinEventListener(), this);
        getServer().getPluginManager().registerEvents(new AsyncPlayerChatEventListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathEventListener(), this);
        getServer().getPluginManager().registerEvents(new MoveTimeUpdater(), this);
        getServer().getPluginManager().registerEvents(this, this);

        // Command Register
        val settingsCommand = new SettingsCommand(pluginSettingsManager);
        val teamCommand = new TeamCommand(pluginSettingsManager);
        val reTeleportCommand = new ReTeleportCommand(
                pluginSettingsManager,
                playersTable,
                spawnPointLoader,
                Bukkit.getWorld(config.getInitConfig().getSpawnWorld()));
        val maintenanceCommand = new MaintenanceCommand(playersTable, playersTeamsTable);
        val messageCommand = new MessageCommand();
        getCommand("settings").setExecutor(settingsCommand);
        getCommand("settings").setTabCompleter(settingsCommand);
        getCommand("team").setExecutor(teamCommand);
        getCommand("team").setTabCompleter(teamCommand);
        getCommand("reteleport").setExecutor(reTeleportCommand);
        getCommand("reteleport").setTabCompleter(reTeleportCommand);
        getCommand("maintenance").setExecutor(maintenanceCommand);
        getCommand("messageboard").setExecutor(messageCommand);
        getCommand("messageboard").setTabCompleter(messageCommand);
    }

    private void migrateDatabase() {
        // 初起動は除外する
        var doMigrate = true;
        val lastMigratedVersion = pluginSettingsManager.getLastMigratedVersion();
        getLogger().log(Level.INFO, "Starting database migrate check... Now database version: {0}", lastMigratedVersion);
        if (lastMigratedVersion == 350) {
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
            val versions = migrateConfig.getMapList("migrate").stream()
                    .map(map -> (String) map.get("version"))
                    .map(v -> Integer.parseInt(v.replace(".", "")))
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            // 更新がない場合実行しない
            val s = getDescription().getVersion().split("-");
            if (versions.stream().max(Comparator.naturalOrder()).get() <= lastMigratedVersion)
                doMigrate = false;

            if (doMigrate)
                getLogger().info("Find migration script, Start migration.");

            // processリストの生成
            int index = 0;
            while (versions.size() > index) {
                if (versions.get(index) > lastMigratedVersion)
                    break;
                index++;
            }
            val processList = index == 0 ? versions : versions.subList(index, versions.size());
            doMigrate = doMigrate && !processList.isEmpty();

            if (doMigrate) {
                getLogger().info("The database structure has been updated. Start the migration process.");
                processList.forEach(process -> {
                    List<Map<?, ?>> versionProcessMap = migrateConfig.getMapList("migrate").stream()
                            .filter(map -> Integer.parseInt(((String) map.get("version")).replace(".", "")) == process)
                            .collect(Collectors.toList());
                    versionProcessMap.forEach(processMap -> {
                        String database = (String) processMap.get("database");
                        List<String> scripts = (List<String>) processMap.get("scripts");
                        DatabaseTable databaseTable;
                        switch (database) {
                            case "teams":
                                databaseTable = playersTeamsTable;
                                break;

                            case "players":
                                databaseTable = playersTable;
                                break;

                            case "settings":
                                databaseTable = pluginSettingsTable;
                                break;

                            case "messages":
                                databaseTable = messagesTable;
                                break;

                            default:
                                databaseTable = null;
                                break;
                        }

                        scripts.forEach(script -> {
                            if (SoloServerApi.getInstance().isDebug())
                                getLogger().info("Migration...: " + script);
                            try (Connection connection = connector.getConnection();
                                 PreparedStatement ps = connection.prepareStatement(
                                         script.replace("%TABLENAME%", databaseTable.getTablename())
                                 )) {
                                ps.execute();
                            } catch (SQLException e) {
                                getLogger().log(Level.WARNING, "An error has occurred during the migration process.", e);
                            }
                        });
                    });
                });

                try {
                    pluginSettingsManager.setLastMigratedVersion(s[0]);
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
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (connector != null)
            connector.close();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("soloservercore." + command.getName())) {
            sender.sendMessage(ChatColor.RED + "You can't run this command because you don't have permission.");
        } else switch (command.getName()) {
            case "status":
                sender.sendMessage(ChatColor.AQUA + "======== SoloServerCore System Information ========");
                sender.sendMessage("Plugin Version: " + getDescription().getVersion());
                sender.sendMessage("Stocked spawn point: " + spawnPointLoader.getPointRemaining());
                sender.sendMessage("");
                sender.sendMessage("CHECK_BLOCK: " + pluginSettingsManager.isCheckBlock());
                sender.sendMessage("PROTECTION_PERIOD: " + pluginSettingsManager.getProtectionPeriod());
                sender.sendMessage("TEAM_SPAWN_COLLECT: " + pluginSettingsManager.isTeamSpawnCollect());
                sender.sendMessage("STOCK_SPAWN_POINT: " + pluginSettingsManager.getStockSpawnPoint());
                sender.sendMessage("BROADCAST_BED_COUNT: " + pluginSettingsManager.isBroadcastBedCount());
                sender.sendMessage("USE_AFK_COUNT: " + pluginSettingsManager.isUseAfkCount());
                sender.sendMessage("AFK_TIME_THRESHOLD: " + pluginSettingsManager.getAfkTimeThreshold());
                sender.sendMessage("RETELEPORT_RESET_ALL: " + pluginSettingsManager.isReteleportResetAll());
                sender.sendMessage("LAST_MIGRATED_VERSION: " + pluginSettingsManager.getLastMigratedVersion());
                break;

            case "charge":
                Bukkit.broadcastMessage("[SSC] Start generate spawn points.\n" +
                        "The server may be stopped while it finishes.");
                spawnPointLoader.initPoint(false);
                Bukkit.broadcastMessage("[SSC] The generate spawn point is now complete.");
                break;

            case "spawn":
                if (sender instanceof Player player) {
                    player.teleport(spawnPointLoader.getSpawn(player));
                } else {
                    Bukkit.getLogger().info("This command must be executed in-game.");
                }
                break;

            case "home":
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
                                player.sendMessage(ChatColor.GREEN + "[SSC] 固定ホーム地点を設定しました。");
                            } else {
                                player.sendMessage(ChatColor.RED + "[SSC] 固定ホーム地点を設定するにはベッドスポーンの事前設定が必要です。");
                            }
                        } else if (args[0].equals("reset")) {
                            SoloServerApi.getInstance().getSSCPlayer(player).setFixedHomeLocation(player.getBedSpawnLocation());
                            try {
                                getPlayersTable().updateFixedHome(player.getUniqueId(),
                                        SoloServerApi.getInstance().getSSCPlayer(player).getFixedHomeLocation());
                            } catch (SQLException e) {
                                getLogger().log(Level.WARNING, "Failed to update the player data.", e);
                            }
                            player.sendMessage(ChatColor.GREEN + "[SSC] 固定ホーム地点を解除しました。");
                        }
                    } else {
                        var location = SoloServerApi.getInstance().getSSCPlayer(player).getFixedHomeLocationObject();
                        if (location == null) // 固定Home設定がない場合
                            location = player.getBedSpawnLocation();
                        if (location == null) // Bedスポーンがない場合
                            location = spawnPointLoader.getSpawn(player);
                        player.teleport(location);
                    }
                } else {
                    Bukkit.getLogger().info("This command must be executed in-game.");
                }
                break;

            default:
                return false;
        }
        return true;
    }

    @EventHandler
    public void onPlayerLoginEvent(PlayerLoginEvent event) {
        if (!spawnPointLoader.isDone()) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "System is in preparation.");
            return;
        }

        if (playersTable.getPlayerData(event.getPlayer().getUniqueId()) == null) {
            val location = spawnPointLoader.getNewLocation();
            if (location != null) {
                val sscPlayer = new InGameSSCPlayer(event.getPlayer().getUniqueId(),
                        location,
                        null,
                        event.getPlayer(),
                        true,
                        null);
                try {
                    SoloServerApi.getInstance().registerSSCPlayer(sscPlayer);
                } catch (SQLException | NullPointerException exception) {
                    SoloServerCore.getInstance().getLogger().log(Level.WARNING, "Failed to save the player data.\n" +
                            "New data will be regenerated next time.", exception);
                    event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "The login process was interrupted due to a system problem.");
                }
            } else {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "System is in preparation.");
                getLogger().warning("There is no stock of teleport coordinates. Please execute regeneration.");
            }
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        event.setQuitMessage("");

        SoloServerApi.getInstance().dropStoreData(event.getPlayer());
        if (Bukkit.getOnlinePlayers().isEmpty())
            spawnPointLoader.initPoint(false);
        else
            Bukkit.getOnlinePlayers().forEach(player -> player.showPlayer(this, event.getPlayer()));
    }

    public SoloServerCoreConfig getCoreConfig() {
        return config;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMoveToNewWorldEvent(PlayerMoveToNewWorldEvent event) {
        SoloServerApi.getInstance().dropStoreData(event.getBukkitPlayer());
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
