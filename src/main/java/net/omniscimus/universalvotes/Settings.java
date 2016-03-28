package net.omniscimus.universalvotes;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Contains configuration information.
 */
public class Settings {

    private final transient UniversalVotes plugin;
    private transient FileConfiguration config;

    private MySQL mySQL;
    private Votifier votifier;

    /**
     * Constructs the object.
     *
     * @param plugin UnknownUtilities instance
     */
    public Settings(UniversalVotes plugin) {
	this.plugin = plugin;
	plugin.saveDefaultConfig();
	this.config = plugin.getConfig();
    }

    /**
     * Reloads the configuration.
     */
    public void reload() {
	plugin.reloadConfig();
	config = plugin.getConfig();
    }

    /**
     * Gets the configuration for MySQL connections.
     *
     * @return an instance of Settings.MySQL
     */
    public MySQL mySQL() {
	if (mySQL == null) {
	    mySQL = new MySQL();
	}
	return mySQL;
    }

    /**
     * Gets the configuration for Votifier.
     *
     * @return an instance of Settings.Votifier
     */
    public Votifier votifier() {
	if (votifier == null) {
	    votifier = new Votifier();
	}
	return votifier;
    }

    /**
     * Contains settings for the MySQL connection.
     */
    public class MySQL {

	/**
	 * The path leading to the MySQL configuration section.
	 */
	private static final String path = "mysql.";

	/**
	 * Gets the hostname to use in MySQL connections.
	 *
	 * @return the hostname, or "127.0.0.1" if it can't be found
	 */
	public String getHostName() {
	    return config.getString(path + "hostname", "127.0.0.1");
	}

	/**
	 * Gets the port to use in MySQL connections.
	 *
	 * @return the port in String format, or "3306" if it can't be found
	 */
	public String getPort() {
	    return config.getString(path + "port", "3306");
	}

	/**
	 * Gets the database to use in MySQL connections.
	 *
	 * @return the database, or "votes" if it can't be found
	 */
	public String getDatabase() {
	    return config.getString(path + "database", "votes");
	}

	/**
	 * Gets the username to use in MySQL connections.
	 *
	 * @return the username, or "root" if it can't be found
	 */
	public String getUsername() {
	    return config.getString(path + "database", "root");
	}

	/**
	 * Gets the password to use in MySQL connections.
	 *
	 * @return the password, or an empty String if it can't be found
	 */
	public String getPassword() {
	    return config.getString(path + "password", "");
	}

    }

    /**
     * Contains settings to do with Votifier.
     */
    public class Votifier {

	/**
	 * The path leading to the Votifier configuration section.
	 */
	private static final String path = "votifier.";

	/**
	 * Gets whether the Votifier listener should be enabled.
	 *
	 * @return if the Votifier listener should be enabled, or true if the
	 * setting can't be found
	 */
	public boolean getEnabled() {
	    return config.getBoolean(path + "enabled", true);
	}

	/**
	 * Gets whether a reminder message should be sent at regular intervals
	 * to players who haven't voted yet.
	 *
	 * @return if the reminder message should be enabled, or true if the
	 * setting can't be found
	 */
	public boolean getReminderEnabled() {
	    return config.getBoolean(path + "vote-reminder-enabled", true);
	}

	/**
	 * Gets the time, in minutes, that should be waited in between each
	 * issue of a reminder message to a player.
	 *
	 * @return the time between reminder messages, or 5 if the setting can't
	 * be found
	 */
	public int getReminderDelay() {
	    return config.getInt(path + "reminder-delay", 5);
	}

    }

}
