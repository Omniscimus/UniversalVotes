package net.omniscimus.universalvotes;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Contains configuration information.
 */
public class Settings {

    private final transient UniversalVotes plugin;
    private transient FileConfiguration config;

    private MySQL mySQL;
    private Votifier votifier;
    private Signs signs;
    private Messages messages;

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
     * Gets the configuration for reward signs.
     *
     * @return an instance of Settings.Signs
     */
    public Signs signs() {
	if (signs == null) {
	    signs = new Signs();
	}
	return signs;
    }

    /**
     * Gets the configuration for certain messages which can be sent to players.
     *
     * @return an instance of Settings.Messages
     */
    public Messages messages() {
	if (messages == null) {
	    messages = new Messages();
	}
	return messages;
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
	    return config.getString(path + "username", "root");
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

    /**
     * Contains configuration for the signs functionality.
     */
    public class Signs {

	/**
	 * The path leading to the Votifier configuration section.
	 */
	private static final String path = "signs.";
	
	/**
	 * Gets a list of reward templates.
	 * 
	 * @return a list of possible sign rewards
	 */
	public List<Reward> getRewardTemplates() {
	    List<String> signTexts = getSignTexts();
	    List<String> commandRewards = getCommandRewards();
	    List<String> messagesOnReward = getMessagesOnReward();
	    int size = Math.min(Math.min(signTexts.size(), commandRewards.size()), messagesOnReward.size());
	    
	    List<Reward> rewards = new ArrayList<>();
	    for (int i = 0; i < size; i++) {
		rewards.add(new Reward(signTexts.get(i), commandRewards.get(i), messagesOnReward.get(i)));
	    }
	    return rewards;
	}

	/**
	 * Gets the list of possible texts on reward signs.
	 *
	 * @return a list with reward texts
	 */
	public List<String> getSignTexts() {
	    return config.getStringList(path + "command-rewards.on-sign");
	}

	/**
	 * Gets the list of reward commands.
	 *
	 * @return a list of commands
	 */
	public List<String> getCommandRewards() {
	    return config.getStringList(path + "command-rewards.commands");
	}

	/**
	 * Gets the list of messages that players receive upon getting their
	 * reward.
	 *
	 * @return a list of reward messages
	 */
	public List<String> getMessagesOnReward() {
	    return config.getStringList(path + "command-rewards.message-on-buy");
	}

    }

    /**
     * Contains messages that can be sent to players.
     */
    public class Messages {

	/**
	 * The path leading to the Votifier configuration section.
	 */
	private static final String path = "messages.";

	/**
	 * Gets the message that should be sent when a player executes the
	 * 'vote' command. Contains parameters '%p' and '%v', which should be
	 * replaced with the player's name and his amount of votes. Also
	 * contains color codes.
	 *
	 * @return the vote command message, or a default message if the setting
	 * can't be found
	 */
	public String getVoteCommandMessage() {
	    return config.getString(path + "vote", "&6Hey %p! You have got %v vote(s).");
	}

    }

}
