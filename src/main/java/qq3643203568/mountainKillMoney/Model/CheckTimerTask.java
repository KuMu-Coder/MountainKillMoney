package qq3643203568.mountainKillMoney.Model;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import qq3643203568.mountainKillMoney.Utils.ConfigManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

public class CheckTimerTask {
    private final JavaPlugin plugin;
    private final ConfigManager config;


    public CheckTimerTask(JavaPlugin plugin,ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
        SavePlayerDataTask();
        CheckDateTask();
    }

    private void SavePlayerDataTask(){
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<UUID,Integer> entry : KillMoneyManager.PlayerKillMoneyMap.entrySet()){
                    UUID uuid = entry.getKey();
                    int amount = entry.getValue();
                    config.savePlayerData(uuid,amount);
                }
                plugin.getLogger().info("已保存玩家数据");
            }
        }.runTaskTimerAsynchronously(plugin,0,config.playerDataTimer*20);
    }

    private void CheckDateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                if (!date.equals(KillMoneyManager.date)) {
                    KillMoneyManager.date = date;
                    KillMoneyManager.PlayerKillMoneyMap.clear();
                    config.saveDate(date);
                    plugin.getLogger().info("每日击杀上限已刷新");
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0, config.dateTimer * 20);
    }
}
