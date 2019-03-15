package red.man10.mquest;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import red.man10.mquest.plus.ItemCommand;

public final class MQuest extends JavaPlugin {

    public MQuestCommand command;
    public MySQLManager mysql;
    public ItemManager im;
    public QuestManager qm;
    public FlagManager fm;
    public VaultManager vault;
    public ItemCommand ic;

    public String prefix = "§f§l[§d§lM§f§lQue§a§lst§f§l]§r";

    private static MQuest instance;
    public static MQuest getInstance(){
        return instance;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        saveDefaultConfig();
        mysql = new MySQLManager(this,"MQuest");
        vault = new VaultManager(this);
        im = new ItemManager(this);
        qm = new QuestManager(this);
        fm = new FlagManager(this);
        command = new MQuestCommand(this);
        ic = new ItemCommand(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void async_executeSQL(String sql){
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> mysql.execute(sql));
    }
}
