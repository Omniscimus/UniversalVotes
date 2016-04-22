package net.omniscimus.universalvotes.listeners;

import java.util.List;
import java.sql.SQLException;
import java.util.logging.Level;

import net.omniscimus.universalvotes.UniversalVotes;
import net.omniscimus.universalvotes.VotesSQL;

import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

/**
 * This class gets notified when people vote.
 */
public class VoteListener implements Listener {

    private final UniversalVotes plugin;
    private final VotesSQL database;
    private final List<String> commandsOnVote;

    /**
     * Creates the object.
     *
     * @param plugin UniversalVotes instance
     * @param database a MySQL accessor
     */
    public VoteListener(UniversalVotes plugin, VotesSQL database) {
	this.plugin = plugin;
	this.database = database;
	commandsOnVote = plugin.getConfig().getStringList("votifier.commands_on_vote");
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onVotifierEvent(VotifierEvent event) {
	Vote vote = event.getVote();
	String playerName = vote.getUsername();

	for (String command : commandsOnVote) {
	    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command.replace("%p", playerName));
	}

	try {
	    database.addVote(playerName);
	} catch (SQLException | ClassNotFoundException e) {
	    plugin.getLogger().log(Level.SEVERE, "Couldn't add a vote to player " + playerName + "!", e);
	}

    }

    /**
     * Sends a fake votifier event to the server.
     * 
     * @param plugin UniversalVotes instance
     * @param sender the person who is sending this vote
     */
    public static void fakeVote(UniversalVotes plugin, CommandSender sender) {
	Vote vote = new Vote(
		"ServerList",
		sender.getName(),
		"1337.0.0.1",
		String.valueOf(System.currentTimeMillis()));
	plugin.getServer().getPluginManager().callEvent(new VotifierEvent(vote));
    }

}
