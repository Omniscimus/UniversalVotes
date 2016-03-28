package net.omniscimus.universalvotes;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.omniscimus.universalvotes.UniversalVotes;
import com.huskehhh.mysql.mysql.MySQL;

public class VotesSQL {

    private final UniversalVotes plugin;
    private final MySQL mySQL;
    private Connection con;
    private final String database;

    private Connection getCon() throws ClassNotFoundException, SQLException {
	if (con == null) {
	    con = mySQL.openConnection();
	    return con;
	} else if (con.isValid(60000)) {
	    return con;
	} else {
	    con = mySQL.openConnection();
	    return con;
	}
    }

    public VotesSQL(UniversalVotes plugin, String hostName, String port, String database, String username, String password) throws ClassNotFoundException, SQLException {
	this.plugin = plugin;
	this.database = database;
	this.mySQL = new MySQL(hostName, port, database, username, password);

	Statement statement = getCon().createStatement();
		//setVotesStatement = con.prepareStatement("INSERT INTO " + database + ".universalvotes (playeruuid, votes, lastdate) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE playeruuid = VALUES(playeruuid), votes = VALUES(votes), lastdate = VALUES(lastdate)");

	// Create the tables if it's not already there
	statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + database + ".universalvotes (playeruuid CHAR(36) NOT NULL UNIQUE, votes SMALLINT, lastdate DATE)");


	/*	INSERT INTO <database>.universalvotes
	 *	(playeruuid, votes, lastdate)
	 *	VALUES
	 *	(?, ?, ?)
	 *	ON DUPLICATE KEY UPDATE
	 *	playeruuid     = VALUES(playeruuid),
	 *	votes = VALUES(votes)
	 *	lastdate = VALUES(lastdate)
	 */
    }

    public void closeConnection() throws SQLException {
	con.close();
    }

    @SuppressWarnings("deprecation")
    public String setVotes(String playerName, int votes) throws SQLException, ClassNotFoundException {
	PreparedStatement setVotesStatement = getCon().prepareStatement("INSERT INTO " + database + ".universalvotes (playeruuid, votes, lastdate) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE playeruuid = VALUES(playeruuid), votes = VALUES(votes), lastdate = VALUES(lastdate)");
	setVotesStatement.setString(1, String.valueOf(plugin.getServer().getOfflinePlayer(playerName).getUniqueId().toString()));
	setVotesStatement.setInt(2, votes);
	setVotesStatement.setDate(3, new java.sql.Date(System.currentTimeMillis()));
	setVotesStatement.execute();
	return "Successfully set " + playerName + "'s number of votes to " + votes;
    }

    public int getVotes(String playerName) throws SQLException, ClassNotFoundException {
	Statement statement = getCon().createStatement();
	@SuppressWarnings("deprecation")
	ResultSet rs = statement.executeQuery("SELECT votes FROM " + database + ".universalvotes WHERE playeruuid = '" + plugin.getServer().getOfflinePlayer(playerName).getUniqueId().toString() + "';");
	if (rs.next()) {
	    return rs.getInt("votes");
	} else {
	    return 0;
	}
    }

    public String addVote(String playerName) throws SQLException, ClassNotFoundException {
	setVotes(playerName, getVotes(playerName) + 1);
	return "Vote added to player " + playerName;
    }

    public boolean removeVotes(String playerName, int value) throws SQLException, ClassNotFoundException {
	int currentVotes = getVotes(playerName);
	if (currentVotes < value) {
	    // Insufficient votes
	    return false;
	} else {
	    setVotes(playerName, currentVotes - value);
	    return true;
	}
    }

    public Date getLastVoteDate(String playerName) throws ClassNotFoundException, SQLException {
	Statement statement = getCon().createStatement();
	@SuppressWarnings("deprecation")
	ResultSet rs = statement.executeQuery("SELECT lastdate FROM " + database + ".universalvotes WHERE playeruuid = '" + plugin.getServer().getOfflinePlayer(playerName).getUniqueId().toString() + "';");
	if (rs.next()) {
	    return rs.getDate("lastdate");
	} else {
	    return null;
	}
    }

}
