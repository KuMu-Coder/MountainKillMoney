package qq3643203568.mountainKillMoney.Event;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.java.JavaPlugin;
import qq3643203568.mountainKillMoney.Model.KillMoneyManager;
import qq3643203568.mountainKillMoney.MountainKillMoney;
import qq3643203568.mountainKillMoney.Utils.ConfigManager;

import java.util.*;

public class PlayerKillMobsListener implements Listener {
    private final ConfigManager config;
    private final KillMoneyManager killMoneyManager;
    private final JavaPlugin plugin;

    public PlayerKillMobsListener(ConfigManager config, KillMoneyManager killMoneyManager, MountainKillMoney plugin){
        this.config = config;
        this.killMoneyManager = killMoneyManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerKillMobs(EntityDeathEvent event) {
        //获取击杀者
        Player player = event.getEntity().getKiller();
        //判断击杀者是否为空
        if (player != null) {
            //获取怪物类型
            Entity mobType = event.getEntity();
            if (config.getDebug()){
                plugin.getLogger().info("玩家：" + player.getName() + " 击杀了：" + mobType.getType().name());
            }
            //获取玩家权限组
            List<String> permlist = getPlayerPerm(player);
            if (permlist.isEmpty()) return;
            //获取最高奖励配置
            Map<String,Object> cfg = getHighestConfig(permlist);
            if (cfg == null) return;
            if (isLimit(player,cfg)) return;
            if (config.getDebug()){
                plugin.getLogger().info("玩家：" + player.getName() + " 的最高奖励配置为：" + cfg);
            }
            //给予奖励
            giveReward(player,cfg,mobType);
            if (config.getDebug()){
                plugin.getLogger().info("玩家：" + player.getName() + " 获得了：" + cfg.get("money"));
            }
        }
    }
    //根据优先级获取最高奖励配置
    private Map<String,Object> getHighestConfig(List<String> perm){
        Map<String,Object> highestConfig = null;
        int highestPriority = -1;

        for (String per : perm){
            Map<String,Object> cfg = config.getPlayerPerm(per);
            if (cfg == null) {
                plugin.getLogger().warning("未找到权限 [" + per + "] 的配置，跳过...");
                continue;
            }

            int priority = (int) cfg.getOrDefault("priority",0);
            if (priority > highestPriority){
                highestPriority = priority;
                highestConfig = cfg;
            }
        }
        return highestConfig;
    }
    //获取玩家权限组
    private List<String> getPlayerPerm(Player player){
        List<String> permList = new ArrayList<>();
        for (PermissionAttachmentInfo perm : player.getEffectivePermissions()){
            if (perm.getValue() && perm.getPermission().toLowerCase().startsWith("mountainkill.")){
                permList.add(perm.getPermission().toLowerCase());
            }
        }
        if (config.getDebug()){
            plugin.getLogger().info("玩家：" + player.getName() + " 的权限组为：" + permList);
        }
        return permList;
    }
    //给予奖励
    private void giveReward(Player player, Map<String,Object> cfg, Entity mobtype){
        Map<String,Map<String,Double>> moneyMap = (Map<String, Map<String, Double>>) cfg.get("money");
        String mobtype1 = mobtype.getType().name();

        Map<String,Double> moneyRange = moneyMap.getOrDefault(mobtype1,moneyMap.get("ALL"));
        if (moneyRange != null){
            double min = moneyRange.get("min");
            double max = moneyRange.get("max");
            double money = new Random().nextDouble()*(max-min)+min;
            double money1 = Math.floor(money);

            config.giveMoney(player,money1);
            if (config.getTipState(player.getUniqueId())){
                config.sendTip(player,mobtype,money1,cfg);
            }
            killMoneyManager.setKillAmount(player.getUniqueId());
            config.savePlayerData(player.getUniqueId(),killMoneyManager.getKillAmount(player.getUniqueId()));
        }
    }
    //判断是否达到击杀上限
    private boolean isLimit(Player player,Map<String,Object> cfg){
        //true则为到达上限 false则为未到达上限
        int limit = killMoneyManager.getKillAmount(player.getUniqueId());
        int sl = (int) cfg.get("kill");
        if (limit >= sl){
            if (config.getTipState(player.getUniqueId())){
                player.sendMessage(config.langMap.get("LimitTip").replace("%limit%",String.valueOf(sl)));
            }
            return true;
        }
        return false;
    }
}
