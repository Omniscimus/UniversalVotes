package net.omniscimus.universalvotes;

import java.sql.SQLException;
import java.util.logging.Level;

import net.omniscimus.universalvotes.listeners.VoteListener;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Contains code that should be executed when a player executes one of this
 * plugin's commands.
 */
public class UniversalVotesCommandExecutor implements CommandExecutor {

    private final UniversalVotes plugin;
    private final VotesSQL database;

    public UniversalVotesCommandExecutor(UniversalVotes plugin, VotesSQL database) {
	if (plugin == null || database == null) {
	    throw new IllegalArgumentException("plugin and database can't be null");
	}
	this.plugin = plugin;
	this.database = database;
    }

    private static final String HELP
	    = ChatColor.GOLD + "---- UniversalVotes help ----\n"
	    + ChatColor.GOLD + "/vote add <playername>: " + ChatColor.WHITE + "add one vote to a player.\n"
	    + ChatColor.GOLD + "/vote get <playername>: " + ChatColor.WHITE + "display the specified player's number of votes.\n"
	    + ChatColor.GOLD + "/vote set <playername> <number>: " + ChatColor.WHITE + "set a player's votes to the specified number.\n"
	    + ChatColor.GOLD + "/vote fake: " + ChatColor.WHITE + "send a fake vote to the server.";

    private static final String NO_PERMISSION
	    = ChatColor.RED + "You don't have permission to execute that command.";

    public static boolean hasAdminPermissions(CommandSender person) {
	if (person.hasPermission("universalvotes.admin")) {
	    return true;
	}
	person.sendMessage(NO_PERMISSION);
	return false;
    }

    /**
     * Called by Bukkit whenever someone executes one of this plugin's commands.
     *
     * @param sender command sender
     * @param cmd executed command
     * @param commandLabel base command string
     * @param args command arguments
     * @return true
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
	if (args.length == 0) {
	    voteCommand(sender);
	} else {
	    switch (args[0]) {
		case "help":
		case "h":
		case "?":
		    helpCommand(sender);
		    break;
		case "add":
		    addCommand(sender, args);
		    break;
		case "get":
		    getCommand(sender, args);
		    break;
		case "set":
		    setCommand(sender, args);
		    break;
		case "fake":
		    fakeCommand(sender);
		    break;
	    }
	}
	return true;
    }

    /**
     * Sends a message likely containing the player's amount of votes and
     * instructions on how to vote.
     *
     * @param sender the person to send the message to
     */
    private void voteCommand(CommandSender sender) {
	if (!sender.hasPermission("universalvotes.vote")) {
	    sender.sendMessage(NO_PERMISSION);
	    return;
	}
	int votes;
	try {
	    votes = database.getVotes(sender.getName());
	} catch (SQLException | ClassNotFoundException e) {
	    plugin.getLogger().log(Level.SEVERE, "Couldn't connect to the MySQL database!", e);
	    votes = 0;
	}

	String voteMessage = plugin.getSettings().messages().getVoteCommandMessage();
	voteMessage = voteMessage.replaceAll("%p", sender.getName());
	voteMessage = voteMessage.replaceAll("%v", Integer.toString(votes));
	voteMessage = ChatColor.translateAlternateColorCodes('&', voteMessage);
	sender.sendMessage(voteMessage);
    }

    /**
     * Sends some information on how to use this plugin's commands, if the
     * player has admin permissions.
     *
     * @param sender the person to send this message to
     */
    private void helpCommand(CommandSender sender) {
	if (hasAdminPermissions(sender)) {
	    sender.sendMessage(HELP);
	}
    }

    /**
     * Executes the add command, adding one vote to the player specified in the
     * command's arguments.
     *
     * @param sender the person who executed this command
     * @param args the original array containing the command's arguments
     */
    private void addCommand(CommandSender sender, String[] args) {
	if (hasAdminPermissions(sender)) {
	    if (args.length == 2) {
		try {
		    sender.sendMessage(database.addVote(args[1]));
		} catch (SQLException | ClassNotFoundException e) {
		    String error = "Couldn't add a vote to player " + args[1] + "!";
		    sender.sendMessage(error);
		    plugin.getLogger().log(Level.WARNING, error, e);
		}
	    } else {
		sender.sendMessage("Please specify the player name. /vote add <player>");
	    }
	}
    }

    /**
     * Executes the get command, sending the command sender the amount of votes
     * of the player specified in the command's arguments.
     *
     * @param sender the person who executed this command
     * @param args the original array containing the command's arguments
     */
    private void getCommand(CommandSender sender, String[] args) {
	if (hasAdminPermissions(sender)) {
	    if (args.length == 2) {
		int votes;
		try {
		    votes = database.getVotes(args[1]);
		} catch (SQLException | ClassNotFoundException e) {
		    // Didn't find that player in the database
		    votes = 0;
		}
		sender.sendMessage("Player " + args[1] + " has " + votes + " votes.");
	    } else {
		sender.sendMessage("Please specify the player name. /vote get <player>");
	    }
	}
    }

    /**
     * Executes the set command, changing the amount of votes of the player
     * specified in the command's arguments to the specified value.
     *
     * @param sender the person who executed this command
     * @param args the original array containing the command's arguments
     */
    private void setCommand(CommandSender sender, String[] args) {
	if (args.length == 3) {
	    try {
		sender.sendMessage(database.setVotes(args[1], Integer.parseInt(args[2])));
	    } catch (NumberFormatException e) {
		sender.sendMessage(args[2] + " is not an integer!");
	    } catch (SQLException | ClassNotFoundException e) {
		String error = "Couldn't connect to the MySQL database!";
		sender.sendMessage(error);
		plugin.getLogger().log(Level.SEVERE, error, e);
	    }
	} else {
	    sender.sendMessage("Please specify the player name and the number of votes to set. /vote set <player> <votes>");
	}
    }

    /**
     * Executes the fake command, sending a fake vote to the plugin.
     *
     * @param sender the person who executed this command
     */
    private void fakeCommand(CommandSender sender) {
	if (plugin.getSettings().votifier().getEnabled()) {
	    VoteListener.fakeVote(plugin, sender);
	} else {
	    sender.sendMessage(ChatColor.RED + "Can't send a fake vote; Votifier isn't enabled in the UniversalVotes config!");
	}
    }

}
