package red.man10.mquest;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class InvListener implements Listener {
    private JavaPlugin plugin;
    private InventoryAPI inv;
    private UUID player;
    private String unique;

    public InvListener(JavaPlugin plugin,InventoryAPI inv){
        this.plugin = plugin;
        this.inv = inv;
        this.unique = inv.getInvUniqueID();
    }

    public void register(UUID uuid){
        this.player = uuid;
        plugin.getServer().getPluginManager().registerEvents(this,plugin);
    }

    public boolean ClickCheck(InventoryClickEvent e){
        if(e.getWhoClicked()==null||e.getClickedInventory()==null){
            return false;
        }
        if(e.getClickedInventory().equals(e.getWhoClicked().getInventory())){
            return false;
        }
        if(player==null||!e.getWhoClicked().getUniqueId().equals(player)){
            return false;
        }
        return checkUnique();
    }

    public void closeCheck(InventoryCloseEvent e){
        if(e.getPlayer().getUniqueId()==player){
            unregister();
        }
    }

    public boolean checkUnique(){
        return inv.getName().contains(unique);
    }

    public void unregister(){
        HandlerList.unregisterAll(this);
        this.player = null;
    }
}
