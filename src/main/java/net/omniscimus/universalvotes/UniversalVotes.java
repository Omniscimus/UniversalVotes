package net.omniscimus.universalvotes;

import java.sql.SQLException;
import java.util.logging.Level;

import net.omniscimus.universalvotes.listeners.RemindMessager;
import net.omniscimus.universalvotes.listeners.SignListeners;
import net.omniscimus.universalvotes.listeners.VoteListener;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class for this plugin. Instantiated by Bukkit.
 */
public class UniversalVotes extends JavaPlugin {
    
    public static UniversalVotes P;

    private Settings settings;
    private VotesSQL database;

    private UniversalVotesCommandExecutor commandExecutor;
    private VoteListener voteListener;
    private SignListeners signListeners;
    private RemindMessager remindMessager;
    
    /**
     * Gets the settings that should be used in this plugin.
     * 
     * @return a usable instance of Settings
     */
    public Settings getSettings() {
	return settings;
    }
    
    /**
     * Gets the MySQL database accessor.
     * 
     * @return the VotesSQL instance
     */
    public VotesSQL getVotesDatabase() {
	return database;
    }
    
    /**
     * Gets the used VoteListener.
     * 
     * @return VoteListener instance
     */
    public VoteListener getVoteListener() {
	return voteListener;
    }

    /**
     * Contains code that should be executed when the plugin enables.
     */
    @Override
    public void onEnable() {
	P = this;
	
	settings = new Settings(this);

	try {
	    Settings.MySQL mySQLSettings = settings.mySQL();
	    database = new VotesSQL(this,
		    mySQLSettings.getHostName(),
		    mySQLSettings.getPort(),
		    mySQLSettings.getDatabase(),
		    mySQLSettings.getUsername(),
		    mySQLSettings.getPassword());
	} catch (ClassNotFoundException | SQLException e) {
	    getLogger().log(Level.SEVERE, "Couldn't connect to the MySQL database. Disabling the plugin.", e);
	    getServer().getPluginManager().disablePlugin(this);
	    return;
	}

	commandExecutor = new UniversalVotesCommandExecutor(this, database);

	Settings.Votifier votifierSettings = settings.votifier();
	if (votifierSettings.getEnabled()) {
	    voteListener = new VoteListener(this, database);
	    if (votifierSettings.getReminderEnabled()) {
		int reminderDelay = votifierSettings.getReminderDelay();
		remindMessager = new RemindMessager(this, database, reminderDelay);
	    }
	}

	signListeners = new SignListeners(this);

	getServer().getPluginManager().registerEvents(signListeners, this);
	if (voteListener != null) {
	    getServer().getPluginManager().registerEvents(voteListener, this);
	}
	if (remindMessager != null) {
	    getServer().getPluginManager().registerEvents(remindMessager, this);
	}

	getCommand("vote").setExecutor(commandExecutor);
    }

    /**
     * Contains code that should be executed when the plugin disables.
     */
    @Override
    public void onDisable() {
	try {
	    if (database != null) {
		database.closeConnection();
	    }
	} catch (SQLException e) {
	    getLogger().log(Level.WARNING, "Could not close the connection to the database", e);
	}
	if (remindMessager != null) {
	    remindMessager.disable();
	}
    }

}
