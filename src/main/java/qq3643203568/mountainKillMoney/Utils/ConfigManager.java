package qq3643203568.mountainKillMoney.Utils;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import qq3643203568.mountainKillMoney.Model.KillMoneyManager;
import qq3643203568.mountainKillMoney.MountainKillMoney;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigManager {

    private final JavaPlugin plugin;
    private final File playerData;
    private final KillMoneyManager killMoney;
    private final File config;
    private final File lang;
    public Map<String,String> langMap = new HashMap<>();
    private final Map<UUID,Boolean> tipMap = new HashMap<>();
    private final Map<String,Boolean> tipModeMap = new HashMap<>();
    private final Map<String,Integer> titleMap = new HashMap<>();
    public Integer dateTimer;

    public ConfigManager(MountainKillMoney plugin,KillMoneyManager killMoney){
        this.plugin = plugin;
        this.playerData = new File(plugin.getDataFolder(), "playerData.yml");
        this.config = new File(plugin.getDataFolder(), "config.yml");
        this.lang = new File(plugin.getDataFolder(), "lang.yml");
        this.killMoney = killMoney;
    }

    //加载配置文件
    public void loadConfig(){
        //检查配置文件夹
        if (!plugin.getDataFolder().exists()){
            plugin.getDataFolder().mkdirs();
        }

        //检查默认配置文件
        if (!config.exists()){
            plugin.saveResource("config.yml",false);
            plugin.getLogger().info("已生成默认配置文件");
        }

        //检查默认语言文件
        if (!lang.exists()){
            plugin.saveResource("lang.yml",false);
            plugin.getLogger().info("已生成默认语言文件");
        }

        //处理PlayerData文件
        if (!playerData.exists()){
            try {
                //创建文件
                playerData.createNewFile();
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(playerData);

                //设置日期
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                String date = LocalDateTime.now().format(formatter);
                yaml.set("Time",date);
                yaml.save(playerData);
                plugin.getLogger().info("已创建PlayerData文件");
            }catch (Exception e){
                plugin.getLogger().severe("创建PlayerData文件失败");
                e.printStackTrace();
            }
        }

        plugin.getLogger().info("配置文件加载完成");
    }
    //初始化配置文件
    public void initConfig(){
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(playerData);
        YamlConfiguration yaml1 = YamlConfiguration.loadConfiguration(config);
        YamlConfiguration tip = YamlConfiguration.loadConfiguration(lang);

        langMap.clear();

        //存储语音文件到map中
        for (String key : tip.getKeys(false)){
            langMap.put(key,tip.getString(key));
        }
        plugin.getLogger().info("语言文件加载完成");
        plugin.getLogger().info("共计加载 "+ langMap.size() + " 条提示");

        //加载权限组配置
        if (!yaml1.contains("permLimit")) {
            plugin.getLogger().severe("配置文件中未找到 permLimit 节点！");
            return;
        }

        //存储提示模式开关
        tipModeMap.clear();
        ConfigurationSection tipModeSection = yaml1.getConfigurationSection("MessageTip");
        if (tipModeSection == null) {
            plugin.getLogger().severe("配置文件中未找到 MessageTip 节点！");
            return;
        }
        for (String key : tipModeSection.getKeys(false)){
            tipModeMap.put(key,tipModeSection.getBoolean(key));
        }

        //将玩家数据存储到Map中
        ConfigurationSection playerDataSection = yaml.getConfigurationSection("PlayerData");
        KillMoneyManager.PlayerKillMoneyMap.clear();
        if (playerDataSection != null){
            for (String key : playerDataSection.getKeys(false)){
                if (key == null) continue;
                Map<UUID, Integer> playerDataMap = new ConcurrentHashMap<>();
                playerDataMap.put(UUID.fromString(key),playerDataSection.getInt(key));
                KillMoneyManager.PlayerKillMoneyMap= playerDataMap;
            }
        }
        //读取定时器所需参数
        dateTimer = yaml1.getInt("timeCheck",20*20);
        //存储日期到文件中
        String date = yaml.getString("Time");
        if (date != null){
            KillMoneyManager.date = date;
            plugin.getLogger().info("日期已加载");
        }
        String KillTip = yaml1.getString("KillTip","%prefix%&a你击杀怪物%mob%,获得了&e%money%&a杀币").replace("&","§").replaceAll("%prefix%",getPrefix());
        String LimitTip = yaml1.getString("LimitTip","%prefix%&c你已达到今日击杀上限").replaceAll("&","§").replaceAll("%prefix%",getPrefix());

        langMap.put("KillTip",KillTip);
        langMap.put("LimitTip",LimitTip);
        //存储标题配置
        titleMap.clear();
        ConfigurationSection titleSection = yaml1.getConfigurationSection("TitleConfig");
        if (titleSection == null) {
            plugin.getLogger().severe("配置文件中未找到 TitleConfig 节点！");
            return;
        }
        for (String key : titleSection.getKeys(false)){
            titleMap.put(key,titleSection.getInt(key));
        }
        //判断config是否包含permLimit节点
        if (!yaml1.contains("permLimit")) {
            plugin.getLogger().severe("配置文件中未找到 permLimit 节点！");
            return;
        }

        //清空权限配置文件
        KillMoneyManager.PermLimit.clear();

        ConfigurationSection permLimitSection = yaml1.getConfigurationSection("permLimit");

        if (permLimitSection.contains("mountainkill")){
            ConfigurationSection mountainkillSection = permLimitSection.getConfigurationSection("mountainkill");
            for (String key : mountainkillSection.getKeys(false)) {
                ConfigurationSection permSection = mountainkillSection.getConfigurationSection(key);

                if (permSection == null) {
                    plugin.getLogger().warning("权限配置 " + key + " 不存在或格式错误，跳过");
                    continue;
                }

                Map<String,Object> permLimit = new HashMap<>();
                //加载击杀上限
                permLimit.put("kill",permSection.getInt("kill",0));
                //加载优先级
                permLimit.put("priority",permSection.getInt("priority",0));

                Map<String,Map<String,Double>> moneyMap = new HashMap<>();
                if (permSection.contains("money")){
                    ConfigurationSection moneySection = permSection.getConfigurationSection("money");
                    if (moneySection == null) {
                        plugin.getLogger().warning("权限配置 " + key + " 中的 money 节点不存在或格式错误，跳过");
                        continue;
                    }
                    for (String mobType: moneySection.getKeys(false)){
                        Map<String,Double> range = new HashMap<>();

                        if (moneySection.isList(mobType)){
                            List<Double> values = moneySection.getDoubleList(mobType);
                            if (values.size() >= 2){
                                range.put("min",values.get(0));
                                range.put("max",values.get(1));
                            }
                        }else {
                            range.put("min",moneySection.getDouble(mobType+".min",0));
                            range.put("max",moneySection.getDouble(mobType+".max",0));
                        }
                        moneyMap.put(mobType,range);
                    }
                }
                permLimit.put("money",moneyMap);
                String permName = "mountainkill."+key.toLowerCase();
                killMoney.PermLimit.put(permName,permLimit);
            }
            if (!killMoney.PermLimit.containsKey("mountainkill.default")){
                plugin.getLogger().severe("配置文件中缺少 mountainkill.default 默认配置");
                plugin.getLogger().severe("请检查配置文件或重新生成默认配置");
            }
            plugin.getLogger().info("成功加载 "+ killMoney.PermLimit.size() + " 个权限配置");
        }
    }
    //重载配置文件
    public void reloadConfig(){
        plugin.reloadConfig();
        loadConfig();
        initConfig();
        plugin.getLogger().info("配置文件已重载");
    }
    //获取玩家权限组
    public Map<String,Object> getPlayerPerm(String perm){
        return killMoney.PermLimit.getOrDefault(perm.toLowerCase(),null);
    }
    //给予玩家金币
    public boolean giveMoney(Player player, Double money){
        Economy econ = MountainKillMoney.getEconomy();
        if (econ == null) {
            plugin.getLogger().severe("经济系统未初始化，无法发放奖励");
            return false;
        }
        EconomyResponse r = econ.depositPlayer(player,money);
        return r.transactionSuccess();
    }
    //发送提示
    public void sendTip(Player player, Entity entity,double money,Map<String,Object> cfg){
        String mobName = entity.getName();
        String killAmount = String.valueOf(killMoney.getKillAmount(player.getUniqueId())+1);
        String maxAmount = String.valueOf((int) cfg.get("kill"));
        String msg = langMap.get("KillTip").replace("%mob%",mobName).replace("%money%",String.valueOf(money)).replace("%nowkill%",killAmount).replace("%maxkill%",maxAmount).replaceAll("%prefix%",getPrefix());
        if (tipModeMap.get("Chat")){
            player.sendMessage(msg);
        }
        if (tipModeMap.get("Title")){
            int fadeIn = titleMap.get("fadeIn");
            int stay = titleMap.get("stay");
            int fadeOut = titleMap.get("fadeOut");
            player.sendTitle(msg,"",fadeIn,stay,fadeOut);
        }
        if (tipModeMap.get("ActionBar")){
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
        }
    }
    //获取前缀
    public String getPrefix(){
        return langMap.getOrDefault("prefix","&c未找到前缀").replaceAll("&","§");
    }

    //获取提示
    public String getTip(String key){
        return langMap.getOrDefault(key,"&c未找到提示").replaceAll("&","§").replace("%prefix%",getPrefix());
    }
    //获取帮助提示
    public List<String> getListHelp(String key){
        List<String> list = new ArrayList<>();
        String help = langMap.getOrDefault(key,"&c未找到提示").replaceAll("&","§").replace("%prefix%",getPrefix());
        if (help.startsWith("[") && help.endsWith("]")){
            help = help.substring(1,help.length() - 1);
        }
        for (String s : help.split(",")){
            s = s.trim();
            list.add(s);
        }
        return list;
    }
    //获取开关状态
    public boolean getTipState(UUID uuid){
        if (!tipMap.containsKey(uuid)){
            tipMap.put(uuid,true);
            return true;
        }
        return tipMap.get(uuid);
    }
    //设置开关状态
    public void setTipState(Player player,boolean state){
        tipMap.put(player.getUniqueId(),state);
        if (state){
            player.sendMessage(getTip("open"));
        }else {
            player.sendMessage(getTip("false"));
        }
    }
    //存储玩家数据
    public void savePlayerData(UUID uuid ,int amount){
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(playerData);
        yaml.set("PlayerData."+uuid.toString(),amount);
        try {
            yaml.save(playerData);
        }catch (Exception e){
            plugin.getLogger().severe("保存PlayerData文件失败");
        }
    }
    //设置日期
    public void saveDate(String date){
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(playerData);
        yaml.set("Time",date);
        try {
            yaml.save(playerData);
        }catch (Exception e){
            plugin.getLogger().severe("存储日期失败");
        }
    }
    //读取debug模式
    public boolean getDebug(){
        return plugin.getConfig().getBoolean("debug",false);
    }
    //清空玩家数据
    public void clearPlayerData(){
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(playerData);
        ConfigurationSection playerDataSection = yaml.getConfigurationSection("PlayerData");
        if (playerDataSection!= null){
            for (String key : playerDataSection.getKeys(false)){
                if (key == null) continue;
                Map<UUID, Integer> playerDataMap = new ConcurrentHashMap<>();
                yaml.set("PlayerData."+key,0);
                playerDataMap.put(UUID.fromString(key),0);
                KillMoneyManager.PlayerKillMoneyMap= playerDataMap;
            }
            try {
                yaml.save(playerData);
            }catch (Exception e){
                plugin.getLogger().info("清空玩家数据失败");
            }
        }
    }
}