package net.omniscimus.universalvotes.listeners;

import java.sql.Date;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;

import net.omniscimus.universalvotes.UniversalVotes;
import net.omniscimus.universalvotes.VotesSQL;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class RemindMessager implements Listener {

    private UniversalVotes plugin;
    private VotesSQL database;

    private HashMap<String, BukkitTask> reminderRunnables;
    private long delay;

    public RemindMessager(UniversalVotes plugin, VotesSQL database, int delay) {
	this.plugin = plugin;
	this.database = database;
	plugin.getServer().getPluginManager().registerEvents(this, plugin);
	this.delay = delay * 1200; // delay is in minutes, so the number of ticks (long) this.delay is delay*1200
	reminderRunnables = new HashMap<String, BukkitTask>();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
	Player player = event.getPlayer();
	if (!reminderRunnables.containsKey(player.getName())) {
	    reminderRunnables.put(player.getName(), new BukkitRunnable() {
		@Override
		public void run() {
		    if (playerHasNotVotedToday(player.getName())) {
			player.sendMessage(ChatColor.GOLD + "------------------------------\n" + ChatColor.RED + "Hi! You haven't voted yet today.\nIf you like our server, please support it by voting.\nTo vote, type /vote\n" + ChatColor.GOLD + "------------------------------");
		    }
		}
	    }.runTaskTimer(plugin, delay, delay));
	}
	// The first delay argument is how long it should wait before it executes for the first time; the second is the delay between runs
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
	String playerName = event.getPlayer().getName();
	BukkitTask task = reminderRunnables.get(playerName);
	if (task != null) {
	    task.cancel();
	}
	if (reminderRunnables.containsKey(playerName)) {
	    reminderRunnables.remove(playerName);
	}
    }

    public boolean playerHasNotVotedToday(String playerName) {
	Calendar cal = Calendar.getInstance();
	cal.add(Calendar.HOUR_OF_DAY, -24);
	// .getTime().getTime() : the last getTime is to convert correctly from java.util.Date to java.sql.Date
	Date dateMinus24Hours = new Date(cal.getTime().getTime());
	try {
	    if (database.getLastVoteDate(playerName) == null) {
		return true;
	    }
	    if (database.getLastVoteDate(playerName).before(dateMinus24Hours)) {
		return true;
	    } else {
		return false;
	    }
	} catch (ClassNotFoundException | SQLException e) {
	    plugin.getLogger().severe("Couldn't connect to the MySQL database!");
	    e.printStackTrace();
	    return false;
	}
    }

    public void disable() {
	reminderRunnables.forEach((playerName, bukkitTask) -> bukkitTask.cancel());
	reminderRunnables.clear();
    }

}
