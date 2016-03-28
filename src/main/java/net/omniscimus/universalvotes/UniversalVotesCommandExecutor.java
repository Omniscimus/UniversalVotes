package net.omniscimus.universalvotes;

import java.sql.SQLException;
import java.util.List;

import net.omniscimus.universalvotes.database.Database;
import net.omniscimus.universalvotes.listeners.VoteListener;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class UniversalVotesCommandExecutor implements CommandExecutor {

    private final UniversalVotes plugin;
    private final Database database;

    public UniversalVotesCommandExecutor(UniversalVotes plugin, Database database) {
	this.plugin = plugin;
	this.database = database;

	voteMessages = plugin.getConfig().getStringList("messages.vote");
    }

    private static List<String> voteMessages;

    private void sendVoteMessages(CommandSender receiver, Integer votes) {
	for (String message : voteMessages) {
	    receiver.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("%p", receiver.getName()).replace("%v", votes.toString())));
	}
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
	if (args.length == 0) {
	    if (sender.hasPermission("universalvotes.vote")) {
		int votes;
		try {
		    votes = database.getVotes(sender.getName());
		} catch (NullPointerException e) {
		    // Player isn't in the config yet, so he has probably never voted
		    votes = 0;
		} catch (SQLException e) {
		    plugin.getLogger().severe("Couldn't connect to the MySQL database!");
		    e.printStackTrace();
		    votes = 0;
		} catch (Exception e) {
		    e.printStackTrace();
		    votes = 0;
		}
		sendVoteMessages(sender, votes);
	    }
	} else {
	    if (sender instanceof ConsoleCommandSender || sender.hasPermission("universalvotes.admin")) {
		if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("h") || args[0].equalsIgnoreCase("?")) {
		    sender.sendMessage(ChatColor.GOLD + "---- UniversalVotes help ----\n/vote add <playername>: " + ChatColor.WHITE + "add one vote to a player.\n" + ChatColor.GOLD + "/vote get <playername>: " + ChatColor.WHITE + "display the specified player's number of votes.\n" + ChatColor.GOLD + "/vote set <playername> <number>: " + ChatColor.WHITE + "set a player's votes to the specified number.\n" + ChatColor.GOLD + "/vote fake: " + ChatColor.WHITE + "send a fake vote to the server.");
		} else if (args[0].equalsIgnoreCase("add")) {
		    if (args.length == 2) {
			try {
			    sender.sendMessage(database.addVote(args[1]));
			} catch (Exception e) {
			    sender.sendMessage("Couldn't add a vote to player " + args[1] + "!");
			    e.printStackTrace();
			}
		    } else {
			sender.sendMessage("Please specify the player name. /vote add <player>");
		    }
		} else if (args[0].equalsIgnoreCase("get")) {
		    if (args.length == 2) {
			int votes;
			try {
			    votes = database.getVotes(args[1]);
			} catch (Exception e) {
			    // Didn't find that player in the database
			    votes = 0;
			}
			sender.sendMessage("Player " + args[1] + " has " + votes + " votes.");
		    } else {
			sender.sendMessage("Please specify the player name. /vote get <player>");
		    }
		} else if (args[0].equalsIgnoreCase("set")) {
		    if (args.length == 3) {
			try {
			    sender.sendMessage(database.setVotes(args[1], Integer.parseInt(args[2])));
			} catch (NumberFormatException e) {
			    sender.sendMessage(args[2] + " is not an integer!");
			} catch (SQLException e) {
			    sender.sendMessage("Couldn't connect to the MySQL database!");
			    e.printStackTrace();
			} catch (Exception e) {
			    e.printStackTrace();
			}
		    } else {
			sender.sendMessage("Please specify the player name and the number of votes to set. /vote set <player> <votes>");
		    }
		} else if (args[0].equalsIgnoreCase("fake")) {
		    if (plugin.getVotifierEnabled()) {
			VoteListener.fakeVote(plugin, sender);
		    } else {
			sender.sendMessage(ChatColor.RED + "Can't send a fake vote; Votifier isn't enabled in the UniversalVotes config!");
		    }
		}
	    } else {
		sender.sendMessage("No permission.");
	    }
	}
	return true;
    }

}
