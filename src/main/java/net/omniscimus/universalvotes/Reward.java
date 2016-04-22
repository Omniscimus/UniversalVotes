package net.omniscimus.universalvotes;

import java.sql.SQLException;
import org.bukkit.entity.Player;

/**
 * Represents a reward that a player can buy using vote points.
 */
public class Reward {

    private final String signText;
    private final String command;
    private final String rewardMessage;

    /**
     * The amount of vote points a player has to spend to get this reward.
     */
    private int cost;

    /**
     * Creates the object.
     *
     * @param signText the text that is put on a sign as a textual
     * representation of the reward
     * @param command the command that will be executed when the player buys
     * this reward; this command actually is the reward itself
     * @param rewardMessage the message that will be sent to the player when he
     * buys this reward
     */
    public Reward(String signText, String command, String rewardMessage) {
	this.signText = signText;
	this.command = command;
	this.rewardMessage = rewardMessage;
    }

    /**
     * Gets the text that should be on signs for players as a textual
     * representation of this reward.
     *
     * @return the short description of this reward
     */
    public String getSignText() {
	return signText;
    }

    /**
     * Set the amount of votes that this reward should cost to a new value.
     *
     * @param cost the new vote cost of this reward
     */
    public void setCost(int cost) {
	this.cost = cost;
    }

    /**
     * Gives this reward to the specified player by executing the command and
     * removes the vote cost from the player's vote point balance.
     *
     * @param player the player who bought this reward
     * @throws SQLException if connecting with the database failed
     * @throws ClassNotFoundException if connecting with the database failed
     */
    public void give(Player player) throws SQLException, ClassNotFoundException {
	String playerName = player.getName();
	UniversalVotes plugin = UniversalVotes.P;
	if (plugin.getVotesDatabase().removeVotes(player.getUniqueId(), cost)) {
	    player.sendMessage(cost + " votes were removed from your account.");
	    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command.replace("%p", playerName));
	    player.sendMessage(rewardMessage);
	} else {
	    player.sendMessage("You don't have sufficient votes!");
	}
    }

    /**
     * Clones this reward template, excluding the vote cost.
     *
     * @return a clone of this object
     * @throws CloneNotSupportedException never
     */
    @Override
    public Reward clone() throws CloneNotSupportedException {
	super.clone();
	return new Reward(signText, command, rewardMessage);
    }

}
