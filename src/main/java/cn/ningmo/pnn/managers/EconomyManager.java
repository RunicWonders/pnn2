package cn.ningmo.pnn.managers;

import cn.ningmo.pnn.PNN;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {
    private final PNN plugin;
    private Economy economy;
    private boolean enabled;
    private double setNicknameCost;
    private double resetNicknameCost;
    private boolean refundOnReset;
    private double refundRatio;

    public EconomyManager(PNN plugin) {
        this.plugin = plugin;
        loadConfig();
        if (enabled) {
            setupEconomy();
        }
    }

    private void loadConfig() {
        this.enabled = plugin.getConfig().getBoolean("economy.enabled", false);
        this.setNicknameCost = plugin.getConfig().getDouble("economy.set-nickname-cost", 100.0);
        this.resetNicknameCost = plugin.getConfig().getDouble("economy.reset-nickname-cost", 50.0);
        this.refundOnReset = plugin.getConfig().getBoolean("economy.refund-on-reset", true);
        this.refundRatio = plugin.getConfig().getDouble("economy.refund-ratio", 0.5);
    }

    private boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("未找到Vault插件，经济系统将被禁用！");
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("未找到经济插件，经济系统将被禁用！");
            return false;
        }

        economy = rsp.getProvider();
        return economy != null;
    }

    public boolean isEnabled() {
        return enabled && economy != null;
    }

    public boolean hasEnoughMoney(Player player, double amount) {
        if (!isEnabled()) return true;
        return economy.has(player, amount);
    }

    public boolean withdrawMoney(Player player, double amount) {
        if (!isEnabled()) return true;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public boolean depositMoney(Player player, double amount) {
        if (!isEnabled()) return true;
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    public double getSetNicknameCost() {
        return setNicknameCost;
    }

    public double getResetNicknameCost() {
        return resetNicknameCost;
    }

    public boolean isRefundOnReset() {
        return refundOnReset;
    }

    public double getRefundRatio() {
        return refundRatio;
    }

    public String formatMoney(double amount) {
        if (!isEnabled()) return String.valueOf(amount);
        return economy.format(amount);
    }

    public void reloadConfig() {
        loadConfig();
        if (enabled) {
            setupEconomy();
        }
    }
} 