package net.omniscimus.universalvotes;

import java.sql.SQLException;

import net.omniscimus.universalvotes.database.Database;
import net.omniscimus.universalvotes.database.VotesConfig;
import net.omniscimus.universalvotes.database.VotesSQL;
import net.omniscimus.universalvotes.listeners.RemindMessager;
import net.omniscimus.universalvotes.listeners.SignListeners;
import net.omniscimus.universalvotes.listeners.VoteListener;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class UniversalVotes extends JavaPlugin implements Listener {

    private UniversalVotesCommandExecutor commandExecutor;
    private VoteListener voteListener;
    private SignListeners signListeners;
    private RemindMessager remindMessager;
    private Database database;

    public Database getUniversalVotesDatabase() {
	return database;
    }

    private FileConfiguration config;

    // true: mysql enabled; false: mysql disabled
    private boolean mysql;

    private boolean votifierEnabled;

    protected boolean getVotifierEnabled() {
	return votifierEnabled;
    }
    private boolean reminderEnabled;

    @Override
    public void onEnable() {

	saveDefaultConfig();
	config = getConfig();

	mysql = config.getBoolean("mysql.enabled");

	if (mysql) {
	    try {
		database = new VotesSQL(this, config.getString("mysql.hostname"), config.getString("mysql.port"), config.getString("mysql.database"), config.getString("mysql.username"), config.getString("mysql.password"));
	    } catch (ClassNotFoundException | SQLException e) {
		getLogger().severe("Couldn't connect to the MySQL database! Disabling the plugin!");
		getServer().getPluginManager().disablePlugin(this);
		e.printStackTrace();
		return;
	    }
	} else {
	    database = new VotesConfig(this);
	}

	votifierEnabled = config.getBoolean("votifier.enabled");

	commandExecutor = new UniversalVotesCommandExecutor(this, database);
	if (votifierEnabled) {
	    voteListener = new VoteListener(this, database);
	    reminderEnabled = config.getBoolean("votifier.vote-reminder-enabled");

	    if (reminderEnabled) {
		if (database instanceof VotesSQL) {
		    remindMessager = new RemindMessager(this, database, config.getInt("votifier.reminder-delay"));
		} else {
		    getLogger().warning("You tried to enable vote reminders, but you're not using SQL. Continuing without reminders.");
		}
	    }
	}
	signListeners = new SignListeners(this, database);

	getServer().getPluginManager().registerEvents(signListeners, this);
	if (votifierEnabled) {
	    getServer().getPluginManager().registerEvents(voteListener, this);
	}
	if (remindMessager != null) {
	    getServer().getPluginManager().registerEvents(remindMessager, this);
	}

	getCommand("vote").setExecutor(commandExecutor);

    }

    @Override
    public void onDisable() {
	if (database instanceof VotesSQL) {
	    try {
		((VotesSQL) database).closeConnection();
	    } catch (SQLException e) {
		e.printStackTrace();
	    }
	}
	if (remindMessager != null) {
	    remindMessager.disable();
	}
    }

}
