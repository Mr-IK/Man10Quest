package red.man10.mquest.plus.quests;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import red.man10.mquest.MQuest;
import red.man10.mquest.Util;

public class WikiView implements CommandExecutor {
    MQuest quest;
    int id;

    public WikiView(MQuest quest){
        this.quest = quest;
        quest.getLogger().info("WikiView Quest Enabled");
        Bukkit.getScheduler().runTaskAsynchronously(quest, () -> {
            quest.im.addItem("Diamond",new ItemStack(Material.DIAMOND));
            int i = quest.qm.createQuest("wikiを見る","Wikiを見てルールを確認しよう！","","FirstView_wiki",
                    "Diamond","mquest say §e§l<player_name>さんが§a§lWikiを見るを完了しました！;;mquest msg <player_name> §e報酬として10万とダイヤモンドを取得しました！",100000);
            if(i == -1){
                quest.getLogger().info("WikiView Quest already Created ID: "+quest.qm.getQuestID("wikiを見る"));
                id = quest.qm.getQuestID("初めてのおはまん");
            }else{
                quest.getLogger().info("WikiView Quest Created! ID: "+i);
                id = i;
            }
        });
        quest.getCommand("wikimitayo").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player p = (Player) sender;
        if(!p.hasPermission("mquest.use")){
            p.sendMessage(quest.prefix + "§cあなたには権限がありません！");
            return true;
        }
        if (args.length == 1) {
            if(!args[0].equalsIgnoreCase("まんじゅうへようこそ")){
                p.sendMessage(quest.prefix+"§cパスワードが違います！");
                return true;
            }
            Bukkit.getScheduler().runTaskAsynchronously(quest,()->{
                if(!quest.fm.checkFlag("FirstView_wiki",p.getUniqueId())){
                    quest.fm.upFlag("FirstView_wiki",p);
                    Util.sendHoverText(p,quest.prefix+"§e§l§nWikiを見る達成！ クリックで報酬をゲットしよう！",
                            "§eクリックで報酬ゲット！","/mquest check "+id);
                }else{
                    p.sendMessage(quest.prefix+"§cクリアできるのは一回のみです！");
                }
            });
            return true;
        }
        return true;
    }
}
