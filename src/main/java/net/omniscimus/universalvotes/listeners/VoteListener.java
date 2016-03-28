package net.omniscimus.universalvotes.listeners;

import java.util.List;

import net.omniscimus.universalvotes.UniversalVotes;
import net.omniscimus.universalvotes.VotesSQL;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

public class VoteListener implements Listener {

    private final UniversalVotes plugin;
    private final VotesSQL database;
    private final List<String> commandsOnVote;
    private final boolean broadcast;

    public VoteListener(UniversalVotes plugin, VotesSQL database) {
	this.plugin = plugin;
	this.database = database;
	commandsOnVote = plugin.getConfig().getStringList("votifier.commands_on_vote");
	broadcast = plugin.getConfig().getBoolean("votifier.broadcast-message-on-vote");
    }

    @EventHandler
    public void onVotifierEvent(VotifierEvent event) {
	Vote vote = event.getVote();
	String playerName = vote.getUsername();

	if (broadcast) {
	    plugin.getServer().broadcastMessage(ChatColor.RED + playerName + ChatColor.GOLD + " voted for the server on " + vote.getServiceName() + "!");
	}
	for (String command : commandsOnVote) {
	    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command.replace("%p", vote.getUsername()));
	}

	try {
	    database.addVote(vote.getUsername());
	} catch (Exception e) {
	    plugin.getLogger().severe("Couldn't add a vote to player " + vote.getUsername() + "!");
	    e.printStackTrace();
	}

    }

    public static void fakeVote(UniversalVotes pl, CommandSender sender) {
	Vote vote = new Vote();
	vote.setUsername(sender.getName());
	vote.setServiceName("ServerList");
	vote.setTimeStamp(String.valueOf(System.currentTimeMillis()));
	vote.setAddress("1337.0.0.1");// IP address of the player who voted
	pl.getServer().getPluginManager().callEvent(new VotifierEvent(vote));
    }

}
