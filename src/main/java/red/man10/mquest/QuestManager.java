package red.man10.mquest;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class QuestManager {

    MQuest quest;
    MySQLManager mysql;

    public QuestManager(MQuest quest){
        this.quest = quest;
        mysql = quest.mysql;
        quest.getLogger().info("QuestManager Enabled");
    }

    /*
    超重要！！！！！
    使うときにスレッド化すること！！！！
    例: Bukkit.getScheduler().runTaskAsynchronously(this, () -> quest.createQuest("Quest","テストクエスト","flag1,flag2","flag3,flag4",items,"say test;;say test2",10000));
    */

    public synchronized int createQuest(String name,String lore,String require_flags,String need_flags,String reward_items,String reward_commands,double reward_money){
        if(getQuestID(name)!=-1){
            return -1;
        }
        mysql.execute("INSERT INTO quests (quest_name,quest_lore,require_flags,need_flags,reward_items,reward_commands,reward_money) VALUES ('"+name+"'," +
                "'"+lore+"'," +
                "'"+require_flags+"'," +
                "'"+need_flags+"'," +
                "'"+reward_items+"'," +
                "'"+reward_commands+"'," +
                ""+reward_money+
                ");");
        return getQuestID(name);
    }


    public List<ItemStack> namesToItemStack(String ids){
        List<ItemStack> items = new ArrayList<>();
        String[] idss = ids.split(" ");
        for(String i : idss){
            try{
                int ii = Integer.parseInt(i);
                items.add(quest.im.getItem(quest.im.getItemname(ii)));
            }catch (NumberFormatException e){
                items.add(quest.im.getItem(i));
            }
        }
        return items;
    }

    //String -> FlagList
    public List<String> namesToNames(String ids){
        String[] idss = ids.split(",");
        return new ArrayList<>(Arrays.asList(idss));
    }

    //String -> CommandList
    public List<String> namesToCommandList(String commands){
        String[] idss = commands.split(";;");
        return new ArrayList<>(Arrays.asList(idss));
    }

    //クエスト名からint IDを取得
    //スレッド化必須!!!!
    public int getQuestID(String questname){
        String sql = "SELECT * FROM quests WHERE quest_name = '" + questname + "' ;";
        ResultSet rs = mysql.query(sql);
        if (rs == null) {
            mysql.close();
            return -1;
        }
        try {
            if(rs.next()){
                return rs.getInt("id");
            }
        } catch (NullPointerException | SQLException e1) {
            e1.printStackTrace();
            return -1;
        }
        mysql.close();
        return -1;
    }


    //受諾中のクエスト一覧を取得
    //スレッド化必須!!!!
    public List<Integer> getQuests(UUID uuid){
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT * FROM playerquests WHERE uuid = '" + uuid.toString() + "' AND clear = false ;";
        ResultSet rs = mysql.query(sql);
        if (rs == null) {
            mysql.close();
            return list;
        }
        try {
            while(rs.next()){
                if(doneQuest(rs.getInt("quest_id"),uuid)){
                    continue;
                }
                list.add(rs.getInt("quest_id"));
            }
        } catch (NullPointerException | SQLException e1) {
            e1.printStackTrace();
            return list;
        }
        mysql.close();
        return list;
    }

    //受諾できるクエスト一覧を取得
    //スレッド化必須!!!!
    public List<Integer> getWaitingQuests(UUID uuid){
        List<Integer> lists = getQuests(uuid);
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT * FROM quests ;";
        ResultSet rs = mysql.query(sql);
        if (rs == null) {
            mysql.close();
            return list;
        }
        try {
            while(rs.next()){
                if(checkAccept(rs.getInt("id"),uuid)){
                    if(lists.contains(rs.getInt("id"))){
                        continue;
                    }else if(doneQuest(rs.getInt("id"),uuid)){
                        continue;
                    }
                    list.add(rs.getInt("id"));
                }
            }
        } catch (NullPointerException | SQLException e1) {
            e1.printStackTrace();
            return list;
        }
        mysql.close();
        return list;
    }

    //クエストデータからメッセージをプレイヤーに送信
    //スレッド化必須!!!!
    public void questDataMessageOne(int id,Player p){
        String sql = "SELECT * FROM quests WHERE id = " + id + " ;";
        ResultSet rs = mysql.query(sql);
        if (rs == null) {
            mysql.close();
            return;
        }
        try {
            if(rs.next()){
                Util.sendHoverText(p,quest.prefix+"§e"+id+"§6: §b"+rs.getString("quest_name")+" §e"+rs.getString("quest_lore")+" "+questInfo(questClearCheck(id,p)),
                        "§eクリックでチャット欄にコマンドを入力！","/mquest check "+id);
            }
        } catch (NullPointerException | SQLException e1) {
            e1.printStackTrace();
            return;
        }
        mysql.close();
    }

    public String questInfo(boolean boo){
        if(boo){
            return "§a[報酬受け取り可]";
        }else{
            return "§c[未クリア]";
        }
    }

    //クエストデータからメッセージをプレイヤーに送信
    //スレッド化必須!!!!
    public void questDataMessageTwo(int id,Player p){
        String sql = "SELECT * FROM quests WHERE id = " + id + " ;";
        ResultSet rs = mysql.query(sql);
        if (rs == null) {
            mysql.close();
            return;
        }
        try {
            if(rs.next()){
                Util.sendHoverText(p,quest.prefix+"§e"+id+"§6: §b"+rs.getString("quest_name")+" §e"+rs.getString("quest_lore"),
                        "§eクリックでチャット欄にコマンドを入力！","/mquest accept "+id);
            }
        } catch (NullPointerException | SQLException e1) {
            e1.printStackTrace();
            return;
        }
        mysql.close();
    }

    //受諾中のクエスト一覧を表示
    public void acceptingQuestList(Player p){
        Bukkit.getScheduler().runTaskAsynchronously(quest, () -> {
            List<Integer> idlist = getQuests(p.getUniqueId());
            p.sendMessage(quest.prefix+"§e=============§b受諾中のクエスト一覧§e=============");
            for(int i : idlist){
                questDataMessageOne(i,p);
            }
            p.sendMessage(quest.prefix+"§e================================================");
        });
    }

    //受諾できるクエスト一覧を表示
    public void waitingQuestList(Player p){
        Bukkit.getScheduler().runTaskAsynchronously(quest, () -> {
            List<Integer> idlist = getWaitingQuests(p.getUniqueId());
            p.sendMessage(quest.prefix+"§e=============§b受諾できるクエスト一覧§e=============");
            for(int i : idlist){
                questDataMessageTwo(i,p);
            }
            p.sendMessage(quest.prefix+"§e===================================================");
        });
    }

    //クエストを完了できるか
    //スレッド化必須
    public boolean questClearCheck(int id,Player p){
        String sql = "SELECT * FROM quests WHERE id = " + id + " ;";
        ResultSet rs = mysql.query(sql);
        if (rs == null) {
            mysql.close();
            return false;
        }
        try {
            if(rs.next()){
                List<String> namelist = namesToNames(rs.getString("need_flags"));
                for(String flag: namelist){
                    if(flag.equalsIgnoreCase("")){
                        continue;
                    }
                    if(!quest.fm.checkFlag(flag, p.getUniqueId())){
                        return false;
                    }
                }
            }
        } catch (NullPointerException | SQLException e1) {
            e1.printStackTrace();
            return false;
        }
        mysql.close();
        return true;
    }

    //クエストデータから報酬(アイテム)を取得
    //スレッド化必須！！！！！！！！！
    public String getQuestItems(int id){
        String sql = "SELECT * FROM quests WHERE id = " + id + " ;";
        ResultSet rs = mysql.query(sql);
        if (rs == null) {
            mysql.close();
            return null;
        }
        try {
            if(rs.next()){
                return rs.getString("reward_items");
            }
        } catch (NullPointerException | SQLException e1) {
            e1.printStackTrace();
            return null;
        }
        mysql.close();
        return null;
    }

    //クエストデータから報酬(コマンド)を取得
    //スレッド化必須！！！！！！！！！
    public String getCommands(int id){
        String sql = "SELECT * FROM quests WHERE id = " + id + " ;";
        ResultSet rs = mysql.query(sql);
        if (rs == null) {
            mysql.close();
            return null;
        }
        try {
            if(rs.next()){
                return rs.getString("reward_commands");
            }
        } catch (NullPointerException | SQLException e1) {
            e1.printStackTrace();
            return null;
        }
        mysql.close();
        return null;
    }


    //クエストデータから報酬(お金)を取得
    //スレッド化必須！！！！！！！！！
    public double getMoneys(int id){
        String sql = "SELECT * FROM quests WHERE id = " + id + " ;";
        ResultSet rs = mysql.query(sql);
        if (rs == null) {
            mysql.close();
            return -1;
        }
        try {
            if(rs.next()){
                return rs.getDouble("reward_money");
            }
        } catch (NullPointerException | SQLException e1) {
            e1.printStackTrace();
            return -1;
        }
        mysql.close();
        return -1;
    }

    //超重要!!! クエストを受諾する
    //スレッド化必須!!!!
    public synchronized boolean acceptQuest(int id,Player player){
        if(!checkAccept(id,player.getUniqueId())){
            return false;
        }
        if(doneQuest(id,player.getUniqueId())){
            return false;
        }
        mysql.execute("INSERT INTO playerquests (name,uuid,quest_id,clear) VALUES ('"+player.getName()+"'," +
                "'"+player.getUniqueId().toString()+"'," +
                ""+id+"," +
                "false);");
        return true;
    }

    //クエストが終了しているかどうか
    //スレッド化必須!!!!
    public boolean doneQuest(int id,UUID uuid){
        String sql = "SELECT * FROM playerquests WHERE quest_id = " + id + " AND uuid = '"+uuid.toString()+"';";
        ResultSet rs = mysql.query(sql);
        if (rs == null) {
            mysql.close();
            return false;
        }
        try {
            if(rs.next()){
                return rs.getBoolean("clear");
            }
        } catch (NullPointerException | SQLException e1) {
            e1.printStackTrace();
            return false;
        }
        mysql.close();
        return false;
    }


    //クエストを受諾できるか
    //スレッド化必須！！！！！！！！！
    public boolean checkAccept(int id,UUID player){
        if(getQuests(player).contains(id)){
            return false;
        }
        String sql = "SELECT * FROM quests WHERE id = " + id + " ;";
        ResultSet rs = mysql.query(sql);
        if (rs == null) {
            mysql.close();
            return false;
        }
        try {
            if(rs.next()){
                List<String> list = namesToNames(rs.getString("require_flags"));
                for(String i :list){
                    if(i.equalsIgnoreCase("")){
                        continue;
                    }
                    if(!quest.fm.checkFlag(i, player)){
                        return false;
                    }
                }
                return true;
            }
        } catch (NullPointerException | SQLException e1) {
            e1.printStackTrace();
            return false;
        }
        mysql.close();
        return false;
    }


    //超重要!!! クエストを完了する
    //スレッド化必須!!
    public synchronized boolean completeQuest(int id,Player uuid){
        if(!questClearCheck(id,uuid)){
            return false;
        }
        if(doneQuest(id,uuid.getUniqueId())){
            return false;
        }
        quest.mysql.execute("UPDATE playerquests SET clear = true WHERE uuid = '" + uuid.getUniqueId().toString() + "' AND quest_id = "+id+" ;");
        reward_give(id,uuid);
        return true;
    }

    //超重要!!! クエストを完了時の報酬授与
    //スレッド化必須!!
    public void reward_give(int id,Player player){
        //アイテムギブ
        List<ItemStack> item = namesToItemStack(getQuestItems(id));
        for(ItemStack i : item){
            if(i == null){
                continue;
            }
            player.getInventory().addItem(i);
        }
        //コマンド実行
        List<String> commands = namesToCommandList(getCommands(id));
        for(String cmd : commands){
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),cmd.replaceAll("<player_name>",player.getName()));
        }
        //お金give
        quest.vault.deposit(player.getUniqueId(),getMoneys(id));
    }

    public void denyedQuest(String questname,UUID uuid){
        quest.async_executeSQL("DELETE FROM playerquests WHERE uuid = '" + uuid.toString() + "' AND quest_id = "+getQuestID(questname)+";");
    }
}
