package net.omniscimus.universalvotes.listeners;

import java.sql.Date;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

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

/**
 * Provides functionality to regularly send players who haven't voted yet a
 * message.
 */
public class RemindMessager implements Listener {

    private final UniversalVotes plugin;
    private final VotesSQL database;

    /* Keeps a task running for every player who hasn't voted yet. */
    private final HashMap<UUID, BukkitTask> reminderRunnables;
    private final long delay;

    private static final String MESSAGE
	    = ChatColor.GOLD + "------------------------------\n"
	    + ChatColor.RED + "Hi! You haven't voted yet today.\n"
	    + "If you like our server, please support it by voting.\n"
	    + "To vote, type /vote\n"
	    + ChatColor.GOLD + "------------------------------";

    /**
     * Creates the object.
     *
     * @param plugin the UniversalVotes instance
     * @param database the MySQL database to use
     * @param delay the delay in seconds between reminder messages
     */
    public RemindMessager(UniversalVotes plugin, VotesSQL database, int delay) {
	this.plugin = plugin;
	this.database = database;
	/* delay is in minutes, so the number of ticks (long) this.delay is
	 delay*1200 */
	this.delay = delay * 1200;

	reminderRunnables = new HashMap<>();
    }

    /**
     * Called by Bukkit when a player joins the server.
     *
     * @param event the PlayerJoinEvent that occurred
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
	Player player = event.getPlayer();
	UUID uuid = player.getUniqueId();
	if (!reminderRunnables.containsKey(uuid)) {
	    reminderRunnables.put(uuid, new BukkitRunnable() {
		@Override
		public void run() {
		    try {
			if (!playerHasVotedToday(player.getName())) {
			    player.sendMessage(MESSAGE);
			} else {
			    /* Don't keep running if the player already voted. */
			    cancel();
			}
		    } catch (ClassNotFoundException | SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "Could not connect to the MySQL database to look up if " + player.getName() + " has already voted today.", ex);
		    }
		}
	    }.runTaskTimer(plugin, delay, delay));
	    /* The first delay argument is how long it should wait before it
	     executes for the first time; the second is the delay between runs
	     */
	}
    }

    /**
     * Called by Bukkit when a player quits the server.
     *
     * @param event the PlayerQuitEvent that occurred
     */
    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
	UUID uuid = event.getPlayer().getUniqueId();
	BukkitTask task = reminderRunnables.get(uuid);
	if (task != null) {
	    task.cancel();
	}
	if (reminderRunnables.containsKey(uuid)) {
	    reminderRunnables.remove(uuid);
	}
    }

    /**
     * Gets if a player did already vote today.
     *
     * @param playerName the name of the player who should be checked
     * @return true if the player has not yet voted today; false if he has
     * @throws ClassNotFoundException if connecting to the database failed
     * @throws SQLException if connecting to the database failed
     * @deprecated use {@link #playerHasVotedToday(Player)}
     */
    @Deprecated
    public boolean playerHasVotedToday(String playerName) throws ClassNotFoundException, SQLException {
	return playerHasVotedToday(plugin.getServer().getPlayer(playerName));
    }
    
    /**
     * Gets if a player did already vote today.
     *
     * @param player the player who should be checked
     * @return true if the player has not yet voted today; false if he has
     * @throws ClassNotFoundException if connecting to the database failed
     * @throws SQLException if connecting to the database failed
     */
    public boolean playerHasVotedToday(Player player) throws ClassNotFoundException, SQLException {
	Calendar cal = Calendar.getInstance();
	cal.add(Calendar.HOUR_OF_DAY, -24);
	Date dateMinus24Hours = new Date(cal.getTime().getTime());
	/* The last getTime() is to convert correctly from java.util.Date to
	 java.sql.Date */
	if (database.getLastVoteDate(player.getUniqueId()) == null) {
	    return false;
	} else {
	    return !database.getLastVoteDate(player.getUniqueId()).before(dateMinus24Hours);
	}
    }

    /**
     * Disables all vote reminders.
     */
    public void disable() {
	reminderRunnables.forEach((playerName, bukkitTask) -> bukkitTask.cancel());
	reminderRunnables.clear();
    }

}
