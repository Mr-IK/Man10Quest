package red.man10.mquest.plus.quests;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import red.man10.mquest.MQuest;
import red.man10.mquest.Util;

public class SayOhaman implements Listener {
    MQuest quest;
    int id;

    public SayOhaman(MQuest mQuest){
        quest = mQuest;
        mQuest.getLogger().info("Ohaman Quest Enabled");
        Bukkit.getScheduler().runTaskAsynchronously(quest, () -> {
            quest.im.addItem("Diamond",new ItemStack(Material.DIAMOND));
            int i = quest.qm.createQuest("初めてのおはまん","おはまんと言ってみよう！","","First_Ohaman",
                    "Diamond","mquest say §e§l<player_name>さんが§a§l初めてのおはまんクエストを完了しました！;;mquest msg <player_name> §e報酬として10万とダイヤモンドを取得しました！",100000);
            if(i == -1){
                mQuest.getLogger().info("Ohaman Quest already Created ID: "+quest.qm.getQuestID("初めてのおはまん"));
                id = quest.qm.getQuestID("初めてのおはまん");
            }else{
                mQuest.getLogger().info("Ohaman Quest Created! ID: "+i);
                id = i;
            }
        });
        quest.getServer().getPluginManager().registerEvents(this,quest);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        if(e.getMessage().contains("おはまん")||e.getMessage().contains("ohaman")||
                e.getMessage().contains("ohamann")){
            Bukkit.getScheduler().runTaskAsynchronously(quest,()->{
               if(!quest.fm.checkFlag("First_Ohaman",e.getPlayer().getUniqueId())){
                   quest.fm.upFlag("First_Ohaman",e.getPlayer());
                   Util.sendHoverText(e.getPlayer(),quest.prefix+"§e§l§n初めてのおはまん達成！ クリックで報酬をゲットしよう！",
                           "§eクリックで報酬ゲット！","/mquest check "+id);
               }
            });
        }
    }




}
