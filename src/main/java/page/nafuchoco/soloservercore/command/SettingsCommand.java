package page.nafuchoco.soloservercore.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import page.nafuchoco.soloservercore.SoloServerCore;
import page.nafuchoco.soloservercore.database.PluginSettingsManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class SettingsCommand implements CommandExecutor, TabCompleter {
    private final PluginSettingsManager settingsManager;

    public SettingsCommand(PluginSettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            if (args.length <= 1) {
                sender.sendMessage(ChatColor.AQUA + "======== SoloServerCore Settings ========");
                sender.sendMessage("checkBlock: " + settingsManager.isCheckBlock() + "\n" +
                        "protectionPeriod: " + settingsManager.getProtectionPeriod() + "\n" +
                        "teamSpawnCollect: " + settingsManager.isTeamSpawnCollect() + "\n" +
                        "stockSpawnPoint: " + settingsManager.getStockSpawnPoint());
            } else switch (args[0]) {
                case "checkBlock":
                    settingsManager.setCheckBlock(Boolean.parseBoolean(args[1]));
                    sender.sendMessage(ChatColor.GREEN + "[SSC] Option updated.");
                    break;

                case "protectionPeriod":
                    settingsManager.setProtectionPeriod(Integer.parseInt(args[1]));
                    sender.sendMessage(ChatColor.GREEN + "[SSC] Option updated.");
                    break;

                case "teamSpawnCollect":
                    settingsManager.setTeamSpawnCollect(Boolean.parseBoolean(args[1]));
                    sender.sendMessage(ChatColor.GREEN + "[SSC] Option updated.");
                    break;

                case "stockSpawnPoint":
                    settingsManager.setStockSpawnPoint(Integer.parseInt(args[1]));
                    sender.sendMessage(ChatColor.GREEN + "[SSC] Option updated.");
                    break;

                default:
                    sender.sendMessage(ChatColor.RED + "[SSC] Unknown option.");
                    break;
            }
        } catch (SQLException throwables) {
            sender.sendMessage(ChatColor.RED + "[SSC] An error occurred while updating the plugin settings.");
            SoloServerCore.getInstance().getLogger().log(Level.WARNING, "An error occurred while updating the plugin settings.", throwables);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1)
            return Arrays.asList(new String[]{"checkBlock", "protectionPeriod", "teamSpawnCollect", "stockSpawnPoint"});
        return null;
    }
}
