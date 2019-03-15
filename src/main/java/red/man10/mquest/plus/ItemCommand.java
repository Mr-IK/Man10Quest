package red.man10.mquest.plus;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import red.man10.mquest.MQuest;

public class ItemCommand implements CommandExecutor {

    MQuest quest;

    public ItemCommand(MQuest quest){
        this.quest = quest;
        quest.getLogger().info("MQuest ItemCommand Enabled");
        quest.getCommand("mquestitem").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player p = (Player) sender;
        if(!p.hasPermission("mquest.admin")){
            p.sendMessage(quest.prefix + "§cあなたには権限がありません！");
            return true;
        }
        if (args.length == 0) {
            p.sendMessage(quest.prefix + "§e/mquestitem additem [name] : アイテムをDBに登録");
            p.sendMessage(quest.prefix + "§e/mquestitem removeitem [name] : アイテムをDBから削除");
            return true;
        }else if (args.length == 2) {
            if(args[0].equalsIgnoreCase("additem")){
                String name = args[1];
                if(p.getInventory().getItemInMainHand()==null){
                    p.sendMessage(quest.prefix + "§c登録したいアイテムを持ってください！");
                    return true;
                }
                if(quest.im.addItem(name,p.getInventory().getItemInMainHand())){
                    p.sendMessage(quest.prefix + "§a登録しました！");
                }else{
                    p.sendMessage(quest.prefix + "§c登録に失敗しました");
                }
            }else if(args[0].equalsIgnoreCase("removeitem")){
                String name = args[1];
                if(quest.im.removeItem(name)){
                    p.sendMessage(quest.prefix + "§a削除しました！");
                }else{
                    p.sendMessage(quest.prefix + "§c削除に失敗しました");
                }
            }
            return true;
        }
        p.sendMessage(quest.prefix + "§e/mquestitem additem [name] : アイテムをDBに登録");
        p.sendMessage(quest.prefix + "§e/mquestitem removeitem [name] : アイテムをDBから削除");
        return true;
    }
}
