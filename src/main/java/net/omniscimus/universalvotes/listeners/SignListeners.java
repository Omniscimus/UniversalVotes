package net.omniscimus.universalvotes.listeners;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

import net.omniscimus.universalvotes.Reward;
import net.omniscimus.universalvotes.UniversalVotes;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Provides functionality for the sign shops, allowing players to buy stuff with
 * their vote points.
 */
public class SignListeners implements Listener {

    private final UniversalVotes plugin;

    private final List<Reward> possibleRewards;

    /**
     * Creates the object.
     *
     * @param plugin the UniversalVotes instance
     */
    public SignListeners(UniversalVotes plugin) {
	this.plugin = plugin;

	possibleRewards = plugin.getSettings().signs().getRewardTemplates();
    }

    /**
     * Gets whether the given String is the reward text for any possible
     * rewards, and returns it if so.
     *
     * @param text the string to check against the list of reward descriptions
     * @return the appropriate reward template, or null if there is no reward
     * with the specified sign text
     */
    private Reward getRewardTemplate(String rewardText) {
	for (Reward reward : possibleRewards) {
	    if (reward.getSignText().equalsIgnoreCase(rewardText)) {
		try {
		    return reward.clone();
		} catch (CloneNotSupportedException ex) {
		}
	    }
	}
	return null;
    }

    /**
     * Sign syntax checker. Called by Bukkit whenever the text on a sign gets
     * updated.
     *
     * @param event the event that occurred
     */
    @EventHandler
    public void onSignChange(SignChangeEvent event) {
	if (event.getLine(0).contains("[UniversalVotes]")) {
	    Player player = event.getPlayer();
	    if (!player.hasPermission("universalvotes.placesigns")) {
		player.sendMessage("No permission.");
		event.setCancelled(true);
	    } else {
		// Second line is fully configurable, check if there's a line from the config on it
		String line1 = event.getLine(1);
		if (getRewardTemplate(line1) == null) {
		    player.sendMessage("[UniversalVotes] Wrong syntax at line 2!");
		    event.setCancelled(true);
		    return;
		}

		// Line 2 could be anything and doesn't matter
		// Line 3 should say: <int> votes where votes can be replaced with anything. So e.g. 10 votes 
		String line3 = event.getLine(3);
		try {
		    Integer.parseInt(line3.split(" ")[0]);
		} catch (NumberFormatException e) {
		    player.sendMessage("[UniversalVotes] Wrong syntax at line 4!");
		    event.setCancelled(true);
		    return;
		}
		// if all went well
		player.sendMessage("[UniversalVotes] Sign recognised.");
		player.sendMessage("[UniversalVotes] Using this sign, players can buy " + line1 + " for " + line3 + ".");
	    }
	}
    }

    /**
     * Handles what happens if someone clicks a reward sign. Called by Bukkit
     * whenever someone clicks on a block.
     *
     * @param event
     */
    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {

	Block clickedBlock = event.getClickedBlock();
	if (clickedBlock == null) {
	    return;
	}
	Material clickedBlockType = clickedBlock.getType();
	if (clickedBlockType != Material.SIGN_POST && clickedBlockType != Material.WALL_SIGN && clickedBlockType != Material.SIGN) {
	    return;
	}
	if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
	    return;
	}
	Sign clickedSign = (Sign) clickedBlock.getState();
	if (!clickedSign.getLine(0).contains("[UniversalVotes]")) {
	    return;
	}

	Player player = event.getPlayer();
	if (player.hasPermission("universalvotes.buy")) {
	    String line1 = clickedSign.getLine(1);
	    Reward reward = getRewardTemplate(line1);
	    
	    String[] line3Array = clickedSign.getLine(3).split(" ");
	    reward.setCost(Integer.parseInt(line3Array[0]));

	    try {
		reward.give(player);
	    } catch (SQLException | ClassNotFoundException e) {
		player.sendMessage("You can't buy that because of an error!");
		plugin.getLogger().log(Level.SEVERE, "Couldn't connect to the MySQL database!", e);
	    }
	}

    }

}
