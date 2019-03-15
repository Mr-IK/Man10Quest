package red.man10.mquest;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class FlagManager {

    MQuest quest;

    public FlagManager(MQuest quest){
        this.quest = quest;
        quest.getLogger().info("FlagManager Enabled");
    }

    public void upFlag(String name, Player player){
        Bukkit.getScheduler().runTaskAsynchronously(quest,() ->{
            if(!containFlag(name,player.getUniqueId())){
                quest.mysql.execute("INSERT INTO flags (name,uuid,flag_name,flag_up) VALUES ('"+player.getName()+"'," +
                        "'"+player.getUniqueId().toString()+"'," +
                        "'"+name+"'," +
                        "true);");
            }
            quest.mysql.execute("UPDATE flags SET flag_up = true WHERE uuid = '" + player.getUniqueId().toString() + "' AND name = '"+name+"';");
        });
    }

    public boolean checkFlag(String name,UUID uuid){
        if(!containFlag(name,uuid)){
            return false;
        }
        boolean clear = false;
        String sql = "SELECT * FROM flags WHERE flag_name = '" + name + "' AND uuid = '"+uuid.toString()+"' ;";
        ResultSet rs = quest.mysql.query(sql);
        if (rs == null) {
            quest.mysql.close();
            return false;
        }
        try {
            if(rs.next()){
                clear = rs.getBoolean("flag_up");
            }
        } catch (NullPointerException | SQLException e1) {
            e1.printStackTrace();
            return false;
        }
        quest.mysql.close();
        return clear;
    }

    public boolean containFlag(String name,UUID uuid){
        String sql = "SELECT * FROM flags WHERE flag_name = '" + name + "' AND uuid = '"+uuid.toString()+"' ;";
        ResultSet rs = quest.mysql.query(sql);
        if (rs == null) {
            quest.mysql.close();
            return false;
        }
        try {
            if(rs.next()){
                rs.close();
                quest.mysql.close();
                return true;
            }
        } catch (NullPointerException | SQLException e1) {
            e1.printStackTrace();
            return false;
        }
        quest.mysql.close();
        return false;
    }

    public void downFlag(String name,Player player){
        Bukkit.getScheduler().runTaskAsynchronously(quest,() ->{
            if(!containFlag(name,player.getUniqueId())){
                quest.mysql.execute("INSERT INTO flags (name,uuid,flag_name,flag_up) VALUES ('"+player.getName()+"'," +
                        "'"+player.getUniqueId().toString()+"'," +
                        "'"+name+"'," +
                        "false);");
            }
            quest.mysql.execute("UPDATE flags SET flag_up = false WHERE uuid = '" + player.getUniqueId().toString() + "' AND name = '"+name+"';");
        });
    }
}
