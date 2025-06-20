package qq3643203568.mountainKillMoney.Model;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import qq3643203568.mountainKillMoney.Utils.ConfigManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CheckTimerTask {
    private final JavaPlugin plugin;
    private final ConfigManager config;


    public CheckTimerTask(JavaPlugin plugin,ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
        CheckDateTask();
    }

    //检查日期是否改变
    private void CheckDateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                if (!date.equals(KillMoneyManager.date)) {
                    KillMoneyManager.date = date;
                    KillMoneyManager.PlayerKillMoneyMap.clear();
                    config.saveDate(date);
                    config.clearPlayerData();
                    plugin.getLogger().info("每日击杀上限已刷新");
                }
                if (config.getDebug()){
                    plugin.getLogger().info("执行时间检测任务");
                }
            }
        }.runTaskTimer(plugin, 0, config.dateTimer * 20);
    }
}
