package red.man10.mquest;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ItemManager {

    MQuest quest;
    MySQLManager mysql;

    public ItemManager(MQuest quest){
        this.quest = quest;
        mysql = quest.mysql;
        quest.getLogger().info("ItemManager Enabled");
    }

    public boolean addItem(String name, ItemStack item){
        if(getItemID(name)!=-1){
            return false;
        }
        quest.mysql.execute("INSERT INTO items (name,item) VALUES ('"+name+"'," +
                "'"+itemToBase64(item)+"');");
        return true;
    }

    public boolean removeItem(String name){
        if(getItemID(name)==-1){
            return false;
        }
        quest.mysql.execute("DELETE FROM items WHERE name = '"+name+"';");
        return true;
    }

    public int getItemID(String itemname){
        String sql = "SELECT * FROM items WHERE name = '" + itemname + "' ;";
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

    public String getItemname(int id){
        String sql = "SELECT * FROM items WHERE id = " + id + " ;";
        ResultSet rs = mysql.query(sql);
        if (rs == null) {
            mysql.close();
            return null;
        }
        try {
            if(rs.next()){
                return rs.getString("name");
            }
        } catch (NullPointerException | SQLException e1) {
            e1.printStackTrace();
            return null;
        }
        mysql.close();
        return null;
    }

    public ItemStack getItem(String itemname){
        String sql = "SELECT * FROM items WHERE name = '" + itemname + "' ;";
        ResultSet rs = mysql.query(sql);
        if (rs == null) {
            mysql.close();
            return null;
        }
        try {
            if(rs.next()){
                return itemFromBase64(rs.getString("item"));
            }
        } catch (NullPointerException | SQLException e1) {
            e1.printStackTrace();
            return null;
        }
        mysql.close();
        return null;
    }

    public static ItemStack itemFromBase64(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];

            // Read the serialized inventory
            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            dataInput.close();
            return items[0];
        } catch (Exception e) {
            return null;
        }
    }

    public static String itemToBase64(ItemStack item) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            ItemStack[] items = new ItemStack[1];
            items[0] = item;
            dataOutput.writeInt(items.length);

            for (int i = 0; i < items.length; i++) {
                dataOutput.writeObject(items[i]);
            }

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }
}
