package qq3643203568.mountainKillMoney.Model;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class KillMoneyManager {
    public static Map<UUID,Integer> PlayerKillMoneyMap = new ConcurrentHashMap<>();
    public static Map<String,Map<String,Object>> PermLimit = new HashMap<>();
    public static String date = "";

    public KillMoneyManager(String date){
        this.date = date;
    }

    //获取击杀数量
    public int getKillAmount(UUID uuid){
        if (!PlayerKillMoneyMap.containsKey(uuid)){
            PlayerKillMoneyMap.put(uuid,1);
            return 1;
        }
        return PlayerKillMoneyMap.get(uuid);
    }

    //设置击杀数量
    public void setKillAmount(UUID uuid){
        int amount = getKillAmount(uuid);
        amount = amount + 1;
        PlayerKillMoneyMap.put(uuid,amount);
    }
}
