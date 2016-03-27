package net.omniscimus.universalvotes.database;

import java.sql.Date;
import java.sql.SQLException;

import net.omniscimus.universalvotes.UniversalVotes;

public abstract class Database {
	
	protected UniversalVotes plugin;
	
	public abstract String setVotes(String playerName, int votes) throws Exception;
	public abstract String addVote(String playerName) throws Exception;
	public abstract int getVotes(String playerName) throws Exception;
	public abstract boolean removeVotes(String playerName, int votes) throws Exception;
	public abstract Date getLastVoteDate(String playerName) throws ClassNotFoundException, SQLException;
	
}
