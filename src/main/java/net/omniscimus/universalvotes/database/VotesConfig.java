package net.omniscimus.universalvotes.database;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Date;

import net.omniscimus.universalvotes.UniversalVotes;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class VotesConfig extends Database {

	private FileConfiguration votesConfig;
	private File votesConfigFile;

	public VotesConfig(UniversalVotes plugin) {
		this.plugin = plugin;
	}

	@SuppressWarnings("deprecation")
	@Override
	public String setVotes(String playerName, int votes) throws NullPointerException, UnsupportedEncodingException {
		String configPath = "votes." + plugin.getServer().getOfflinePlayer(playerName).getUniqueId().toString();
		getVotesConfig().set(configPath, votes);
		saveVotesConfig();
		return "Successfully set " + playerName + "'s number of votes to " + votes;
	}

	@SuppressWarnings("deprecation")
	@Override
	public String addVote(String playerName) {
		String configPath = "votes." + plugin.getServer().getOfflinePlayer(playerName).getUniqueId().toString();
		try {
			getVotesConfig().set(configPath, getVotes(playerName) + 1);
			saveVotesConfig();
			return "Vote added to player " + playerName;
		} catch (NullPointerException e) {
			return "Player " + playerName + " has never been online! Can't add a vote.";
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public int getVotes(String playerName) {
		String configPath = "votes." + plugin.getServer().getOfflinePlayer(playerName).getUniqueId().toString();
		try {
			return getVotesConfig().getInt(configPath);
		} catch (NullPointerException e) {
			return 0;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return 0;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean removeVotes(String playerName, int value) {
		String configPath = "votes." + plugin.getServer().getOfflinePlayer(playerName).getUniqueId().toString();
		try {
			int currentVotes = getVotesConfig().getInt(configPath);
			if(currentVotes < value) {
				// Insufficient votes
				return false;
			}
			else {
				getVotesConfig().set(configPath, currentVotes - value);
				saveVotesConfig();
				return true;
			}
		} catch (NullPointerException e) {
			// Player not found in config. He probably never voted.
			return false;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
	}

	// -------- Votes config ----------
	private void reloadVotesConfig() throws UnsupportedEncodingException {
		if (votesConfigFile == null) {
			votesConfigFile = new File(plugin.getDataFolder(), "votes.yml");
		}
		votesConfig = YamlConfiguration.loadConfiguration(votesConfigFile);

		// Look for defaults in the jar
		Reader defConfigStream = new InputStreamReader(plugin.getResource("votes.yml"), "UTF8");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			votesConfig.setDefaults(defConfig);
		}
	}
	private FileConfiguration getVotesConfig() throws UnsupportedEncodingException {
	    if (votesConfig == null) {
	        reloadVotesConfig();
	    }
	    return votesConfig;
	}
	private void saveVotesConfig() {
	    if (votesConfig == null || votesConfigFile == null) {
	        return;
	    }
	    try {
	        getVotesConfig().save(votesConfigFile);
	    } catch (IOException e) {
	        plugin.getLogger().severe("Could not save config to " + votesConfigFile);
	        e.printStackTrace();
	    }
	}

	@Override
	public Date getLastVoteDate(String playerName) {
		return null;
	}

}
