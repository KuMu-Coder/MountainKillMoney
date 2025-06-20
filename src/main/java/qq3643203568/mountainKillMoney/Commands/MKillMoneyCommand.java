package qq3643203568.mountainKillMoney.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import qq3643203568.mountainKillMoney.Utils.ConfigManager;

import java.util.List;

public class MKillMoneyCommand implements CommandExecutor {
    private final ConfigManager config;
    public MKillMoneyCommand(ConfigManager config) {
        this.config = config;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String str, String[] args) {
        if (cmd.getName().equalsIgnoreCase("mkmoney")){
            if (args.length == 0){
                sendTip(sender);
                return true;
            }
                switch (args[0]){
                    case "reload":
                        if (!sender.isOp()){
                            sender.sendMessage(config.getTip("noPermission"));
                            return true;
                        }
                        config.reloadConfig();
                        sender.sendMessage(config.getTip("reload"));
                        return true;
                    case "tip":
                        if (sender instanceof Player){
                            Player player = (Player) sender;
                            if (config.getTipState(player.getUniqueId())){
                                config.setTipState(player,false);
                            }else {
                                config.setTipState(player,true);
                            }
                        }else {
                            sender.sendMessage(config.getTip("noConsole"));
                        }
                }
        }
        return false;
    }

    private void sendTip(CommandSender sender){
        if (sender.isOp()){
            List<String> opHelp = config.getListHelp("opHelp");
            for (String str : opHelp) sender.sendMessage(str);
        }else {
            List<String> help = config.getListHelp("help");
            for (String str : help) sender.sendMessage(str);
        }
    }
}
