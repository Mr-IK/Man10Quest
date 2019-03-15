package red.man10.mquest;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class MQuestCommand implements CommandExecutor {

    MQuest quest;

    public MQuestCommand(MQuest quest){
        this.quest = quest;
        quest.getLogger().info("MQuestCommand Enabled");
        quest.getCommand("mquest").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            if(args.length >= 2 && args[0].equalsIgnoreCase("say")){
                String msg = "";
                for(int i = 1;i<args.length;i++){
                    if(msg.equalsIgnoreCase("")){
                        msg = args[i];
                    }else{
                        msg = msg + " "+ args[i];
                    }
                }
                Bukkit.broadcastMessage(quest.prefix+msg);
                return true;
            }else if(args.length >= 3 && args[0].equalsIgnoreCase("msg")){
                Player player = Bukkit.getPlayer(args[1]);
                if(player == null){
                    quest.getLogger().info("そのプレイヤーはオフラインです");
                    return true;
                }
                String msg = "";
                for(int i = 2;i<args.length;i++){
                    if(msg.equalsIgnoreCase("")){
                        msg = args[i];
                    }else{
                        msg = msg + " "+ args[i];
                    }
                }
                player.sendMessage(quest.prefix+msg);
                return true;
            }

            if (args.length == 3) {
                if (args[0].equalsIgnoreCase("flagup")) {
                    Player player = Bukkit.getPlayer(args[1]);
                    if(player == null){
                        quest.getLogger().info("そのプレイヤーはオフラインです");
                        return true;
                    }
                    String flagname = args[2];
                    quest.fm.upFlag(flagname,player);
                    quest.getLogger().info(player.getName()+"のフラグ「"+flagname+"」を立てました");
                }else if (args[0].equalsIgnoreCase("flagdown")) {
                    Player player = Bukkit.getPlayer(args[1]);
                    if(player == null){
                        quest.getLogger().info("そのプレイヤーはオフラインです");
                        return true;
                    }
                    String flagname = args[2];
                    quest.fm.downFlag(flagname,player);
                    quest.getLogger().info(player.getName()+"のフラグ「"+flagname+"」を下げました");
                }
            }
            return true;
        }
        Player p = (Player) sender;
        if(!p.hasPermission("mquest.use")){
            p.sendMessage(quest.prefix + "§cあなたには権限がありません！");
            return true;
        }
        if (args.length == 0) {
            InventoryAPI inv = new InventoryAPI(quest, quest.prefix, 27);
            inv.setItem(11, inv.createUnbitem("§a§lクエストを受諾する",
                    new String[]{"§eクエストを受諾します！", "§eクリックで開く"}, Material.CHEST, 0, true));
            inv.addOriginalListing(new InvListener(quest, inv) {
                @EventHandler
                public void onClick(InventoryClickEvent e) {
                    if (!super.ClickCheck(e)) {
                        return;
                    }
                    if (e.getSlot() != 11) {
                        return;
                    }
                    p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                    e.setCancelled(true);
                    inv.regenerateID();
                    super.unregister();
                    p.closeInventory();
                    Bukkit.dispatchCommand(p, "mquest accept");
                }

                @EventHandler
                public void onClose(InventoryCloseEvent e) {
                    super.closeCheck(e);
                }
            });
            inv.setItem(15, inv.createUnbitem("§a§l受諾中のクエストを確認する",
                    new String[]{"§eクエストを確認します！", "§eクリックで開く"}, Material.DISPENSER, 0, true));
            inv.addOriginalListing(new InvListener(quest, inv) {
                @EventHandler
                public void onClick(InventoryClickEvent e) {
                    if (!super.ClickCheck(e)) {
                        return;
                    }
                    if (e.getSlot() != 15) {
                        return;
                    }
                    p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                    e.setCancelled(true);
                    inv.regenerateID();
                    super.unregister();
                    p.closeInventory();
                    Bukkit.dispatchCommand(p, "mquest view");
                }

                @EventHandler
                public void onClose(InventoryCloseEvent e) {
                    super.closeCheck(e);
                }
            });
            inv.openInv(p);
            return true;
        }else if (args.length == 1) {
            if(args[0].equalsIgnoreCase("accept")){
                quest.qm.waitingQuestList(p);
                return true;
            }else if(args[0].equalsIgnoreCase("view")){
                quest.qm.acceptingQuestList(p);
                return true;
            }
        }else if (args.length == 2) {
            if(args[0].equalsIgnoreCase("accept")){
                int id = 0;
                try{
                    id = Integer.parseInt(args[1]);
                }catch (NumberFormatException e) {
                    p.sendMessage(quest.prefix+"§cIDは数字で入力してください");
                    return true;
                }
                int finalId = id;
                Bukkit.getScheduler().runTaskAsynchronously(quest,() -> {
                    boolean boo = quest.qm.acceptQuest(finalId,p);
                    if(boo){
                        p.sendMessage(quest.prefix+"§eID:"+finalId+"§aのクエストの受諾に成功しました！");
                    }else{
                        p.sendMessage(quest.prefix+"§eID:"+finalId+"§cのクエストの受諾に失敗しました");
                    }
                });
                return true;
            }else if(args[0].equalsIgnoreCase("check")){
                int id = 0;
                try{
                    id = Integer.parseInt(args[1]);
                }catch (NumberFormatException e) {
                    p.sendMessage(quest.prefix+"§cIDは数字で入力してください");
                    return true;
                }
                int finalId = id;
                Bukkit.getScheduler().runTaskAsynchronously(quest,() -> {
                    boolean boo = quest.qm.completeQuest(finalId,p);
                    if(boo){
                        p.sendMessage(quest.prefix+"§eID:"+finalId+"§aのクエストを完了しました！");
                    }else{
                        p.sendMessage(quest.prefix+"§eID:"+finalId+"§cのクエストの完了に失敗しました");
                    }
                });
                return true;
            }
        }
        p.sendMessage(quest.prefix + "§f/mquest");
        return true;
    }
}
