package nl.kasper7.bukkit.MenShearSnow;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

/**
 * Main class of the MenShearSnow plugin.
 * Houses all functionality of this simple mech plugin.
 * @author 7kasper
 */
public class MenShearSnow extends JavaPlugin implements Listener {
	
    //=====================================================\\
    //						Variables					   \\
    //=====================================================\\
	
	//Plugin values, will load from plugin.yml
	public String pName = "";
	public String pNameSend = "";
	public String pVersion = "";
	public String pAuthors = "";
	public List<Permission> pPermissions = new ArrayList<>();
	public MenShearSnow plugin;
	public ProtocolManager protocolManager;
	
    //=====================================================\\
    //				    Plugin Functions				   \\
    //=====================================================\\
	
	@Override
	public void onEnable() {
		
    	//Set up for future reference.
    	plugin = this;
    	protocolManager = ProtocolLibrary.getProtocolManager();
    	
    	//Get some data from the plugin.yml
    	if(!loadPluginValues()){
    		cm(ChatColor.RED + "Fatal error reading from plugin.yml! Please fix it or reinstall the plugin!");
    		Bukkit.getPluginManager().disablePlugin(plugin);
    		return;
    	};
    	
    	//Implement the shear hook.
    	Bukkit.getPluginManager().registerEvents(plugin, this);
    	
    	cm("Snowman shearing (MenShearSnow v" + pVersion + " is enabled!");
	}
	
	@Override
	public void onDisable() {
		cm("No more men shear snow now.");
	}
	
    /**
     * Load in plugin commands and name from the plugin.yml inside the jar.
     * @return true, if the operation was successful.
     */
    private boolean loadPluginValues(){
    	try{
        	pName = plugin.getDescription().getName();
        	pNameSend = "[" + ChatColor.WHITE + pName + ChatColor.RESET + "] ";
        	pVersion = plugin.getDescription().getVersion();
        	pPermissions = plugin.getDescription().getPermissions();
        	pAuthors = String.join(", ", plugin.getDescription().getAuthors());
        	return true;
    	}catch (Exception e){
    		return false;
    	}
    }
    
    /***
     * Sends a console message as the plugin.
     * @param msg
     */
    public void cm(String msg){
    	Bukkit.getConsoleSender().sendMessage(pNameSend + msg);
    }
    
    //=====================================================\\
    //				    	Listeners					   \\
    //=====================================================\\
    
    @EventHandler (priority = EventPriority.HIGH)
    public void onClickEntity(PlayerInteractEntityEvent e) {
    	Player p = e.getPlayer();
    	p.sendMessage("Click");
    	ItemStack itemInHand = p.getInventory().getItemInMainHand();
    	if(itemInHand != null && itemInHand.getType() == Material.SHEARS && e.getRightClicked().getType() == EntityType.SNOWMAN) {
    		p.sendMessage("Shearing snowman!");
    		Snowman s = (Snowman) e.getRightClicked();
    		if(!s.isDerp()) {
    			s.setDerp(true);
    			//Apply sound effect. CraftBukkit does the rest.
        		p.getWorld().playSound(p.getLocation(), Sound.ENTITY_MOOSHROOM_SHEAR, 10f, 1f);
        		plugin.sendSnowmanDerp(s, true);
    		}
    	}
    }
    
    public void sendSnowmanMetadata(Snowman s, WrappedDataWatcher watcher) {
    	WrapperPlayServerEntityMetadata update = new WrapperPlayServerEntityMetadata();
        update.setEntityID(s.getEntityId());
        update.setMetadata(watcher.getWatchableObjects());
        broadcastPacket(update.getHandle(), s.getLocation(), true);
    }
    
    public void sendSnowmanDerp(Snowman s, boolean derp) {
        WrappedDataWatcher watcher = new WrappedDataWatcher();
        watcher.setObject(12, (byte) 0xff);
        sendSnowmanMetadata(s, watcher);
    }
    
    public void broadcastPacket(PacketContainer packet, Location location, boolean onlyNearby) {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            // Must be within the range
            if (!onlyNearby || p.getLocation().distanceSquared(location) < 128D) {
                try {
                	protocolManager.sendServerPacket(p, packet);
                } catch (InvocationTargetException e) {
                    Bukkit.getLogger().log(Level.WARNING, "Cannot send " + packet + " to " + p, e);
                }
            }
        }
    }
    
}
