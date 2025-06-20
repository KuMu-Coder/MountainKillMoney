package qq3643203568.mountainKillMoney;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import qq3643203568.mountainKillMoney.Commands.MKillMoneyCommand;
import qq3643203568.mountainKillMoney.Event.PlayerKillMobsListener;
import qq3643203568.mountainKillMoney.Model.CheckTimerTask;
import qq3643203568.mountainKillMoney.Model.KillMoneyManager;
import qq3643203568.mountainKillMoney.Utils.ConfigManager;

public final class MountainKillMoney extends JavaPlugin {
    private ConfigManager config;
    private KillMoneyManager killMoneyManager;
    private static Economy economy;
    private CheckTimerTask checkTimerTask;

    @Override
    public void onEnable() {
        //初始化经济系统
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!setupEconomy()) {
                    getLogger().severe("未找到Vault或Vault未启用");
                    getServer().getPluginManager().disablePlugin(MountainKillMoney.this);
                    return;
                }

                killMoneyManager = new KillMoneyManager("");
                config = new ConfigManager(MountainKillMoney.this, killMoneyManager);
                //加载配置文件
                config.loadConfig();
                //初始化配置文件
                config.initConfig();
                //注册事件
                getServer().getPluginManager().registerEvents(new PlayerKillMobsListener(config, killMoneyManager, MountainKillMoney.this), MountainKillMoney.this);
                //注册命令
                getCommand("mkmoney").setExecutor(new MKillMoneyCommand(config));
                //加载定时器
                checkTimerTask = new CheckTimerTask(MountainKillMoney.this,config);
                //加载提示
                getLogger().info("山之击杀已加载");
                getLogger().info("作者QQ：3643203568");
            }
        }.runTask(this);
    }

    @Override
    public void onDisable() {
        getLogger().info("感谢您使用山之系列插件");
        getLogger().info("作者QQ：3643203568");
    }

    //初始化经济API
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().severe("未找到支持vault的经济插件,请安装essx等支持vault的经济插件");
            return false;
        }
        economy = rsp.getProvider();
        if (economy == null) {
            getLogger().severe("无法获取经济服务，请检查经济插件是否正常工作");
            return false;
        }
        getLogger().info("已成功连接到 "+ economy.getName() + " 经济系统");
        return true;
    }

    //获取经济API实例
    public static Economy getEconomy() {
        return economy;
    }
}
