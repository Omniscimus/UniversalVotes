package net.omniscimus.universalvotes.listeners;

import java.util.List;

import net.omniscimus.universalvotes.UniversalVotes;
import net.omniscimus.universalvotes.database.Database;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignListeners implements Listener {

	private UniversalVotes plugin;
	private Database database;

	private List<String> signTexts;
	private List<String> commandRewards;
	private List<String> messagesOnReward;

	public SignListeners(UniversalVotes plugin, Database database) {
		this.plugin = plugin;
		this.database = database;

		signTexts = plugin.getConfig().getStringList("signs.command-rewards.on-sign");
		commandRewards = plugin.getConfig().getStringList("signs.command-rewards.commands");
		messagesOnReward = plugin.getConfig().getStringList("signs.command-rewards.message-on-buy");
	}

	// Sign syntax checker
	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		if(event.getLine(0).contains("[UniversalVotes]")) {
			Player player = event.getPlayer();
			if(!player.hasPermission("universalvotes.placesigns")) {
				player.sendMessage("No permission.");
				event.setCancelled(true);
				return;
			}
			else {

				// Line 1 (irl. line 2) is fully configurable, check if there's a line from the config on it
				String line1 = event.getLine(1);
				if(!signTexts.contains(line1)) {
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
		else return;
	}

	// Sign buy handler
	@EventHandler
	public void onSignClick(PlayerInteractEvent event) {

		Block clickedBlock = event.getClickedBlock();
		if(clickedBlock == null) return;
		Material clickedBlockType = clickedBlock.getType();
		if(clickedBlockType != Material.SIGN_POST && clickedBlockType != Material.WALL_SIGN && clickedBlockType != Material.SIGN) return;

		if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		Sign clickedSign = (Sign)clickedBlock.getState();
		if(!clickedSign.getLine(0).contains("[UniversalVotes]")) return;

		Player player = event.getPlayer();
		if(player.hasPermission("universalvotes.buy")) {
			String playerName = player.getName();
			String line1 = clickedSign.getLine(1);
			String[] line3Array = clickedSign.getLine(3).split(" ");

			for(int i = 0; i < signTexts.size(); i++) {
				if(line1.equalsIgnoreCase(signTexts.get(i))) {
					try {
						if(database.removeVotes(playerName, Integer.parseInt(line3Array[0]))) {
							player.sendMessage(line3Array[0] + " votes were removed from your account.");
							plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), commandRewards.get(i).replace("%p", playerName));
							player.sendMessage(messagesOnReward.get(i));
						}
						else {
							player.sendMessage("You don't have sufficient votes!");
						}
					} catch (NumberFormatException e) {
						player.sendMessage("You can't buy that because of an error!");
						plugin.getLogger().warning("Player " + playerName + " couldn't buy at a sign shop because the number of votes at line 4 is incorrect!");
					} catch (Exception e) {
						player.sendMessage("You can't buy that because of an error!");
						plugin.getLogger().severe("Couldn't connect to the MySQL database!");
						e.printStackTrace();
					}
					break;
				}
			}
		}

	}

}
