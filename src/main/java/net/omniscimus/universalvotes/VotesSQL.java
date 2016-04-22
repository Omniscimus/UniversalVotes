package net.omniscimus.universalvotes;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.huskehhh.mysql.mysql.MySQL;
import java.util.UUID;

/**
 * Contains code used to connect to the MySQL database.
 */
public class VotesSQL {

    private final UniversalVotes plugin;

    private final MySQL mySQL;
    private Connection con;

    /**
     * Creates the object.
     * 
     * @param plugin UniversalVotes instance
     * @param hostName the hostname of the MySQL server to connect to
     * @param port the port of the MySQL server to connect to
     * @param database the database in the MySQL server which should be used
     * @param username a valid username for the MySQL server
     * @param password the password for the specified username
     * @throws ClassNotFoundException if connecting to the database fails
     * @throws SQLException if connecting to the database fails
     */
    public VotesSQL(UniversalVotes plugin, String hostName, String port,
	    String database, String username, String password)
	    throws ClassNotFoundException, SQLException {

	if (plugin == null || hostName == null || port == null
		|| database == null || username == null || password == null) {
	    throw new IllegalArgumentException("None of the arguments can be null.");
	}

	this.plugin = plugin;
	this.mySQL = new MySQL(hostName, port, database, username, password);

	// Create the tables if it's not already there
	Statement statement = getConnection().createStatement();
	statement.executeUpdate("CREATE TABLE IF NOT EXISTS universalvotes (playeruuid CHAR(36) NOT NULL UNIQUE, votes SMALLINT, lastdate DATE)");
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
    
    /**
     * Gets the Unique User ID for the user with the specified name. Looking up
     * offline players or players who never joined the server is allowed, but
     * may return null.
     * 
     * @param playerName the name of the player whose UUID should be looked up
     * @return the UUID of the specified player, or null if it couldn't be found
     */
    @Deprecated
    private UUID getUUID(String playerName) {
	return plugin.getServer().getOfflinePlayer(playerName).getUniqueId();
    }

    /**
     * Gets a usable connection. Creates a new connection if an existing one is
     * not valid.
     *
     * @return a usable connection to the MySQL server
     * @throws ClassNotFoundException if connecting to the database failed
     * @throws SQLException if connecting to the database failed
     */
    private Connection getConnection() throws SQLException, ClassNotFoundException {
	if (con == null) {
	    con = mySQL.openConnection();
	    return con;
	} else if (con.isValid(5)) {
	    return con;
	} else {
	    con = mySQL.openConnection();
	    return con;
	}
    }

    /**
     * Closes the current connection to the MySQL database, if it exists.
     *
     * @throws SQLException if a database access exception occurs
     */
    public void closeConnection() throws SQLException {
	if (con != null) {
	    con.close();
	}
    }

    /**
     * Sets the number of votes of the specified player to the specified amount.
     *
     * @param playerName the player whose amount of votes should be changed
     * @param votes the player's new amount of votes
     * @return false if the UUID of the specified player couldn't be found and
     * the query was not executed; true otherwise
     * @throws SQLException if the database couldn't be accessed
     * @throws ClassNotFoundException if the database couldn't be accessed
     * @deprecated use {@link #setVotes(UUID, int)}
     */
    @Deprecated
    public boolean setVotes(String playerName, int votes) throws SQLException, ClassNotFoundException {
	return setVotes(getUUID(playerName), votes);
    }
    
    /**
     * Sets the number of votes of the specified player to the specified amount.
     *
     * @param player the UUID of the player whose amount of votes should be
     * changed
     * @param votes the player's new amount of votes
     * @return false if the UUID of the specified player couldn't be found and
     * the query was not executed; true otherwise
     * @throws SQLException if the database couldn't be accessed
     * @throws ClassNotFoundException if the database couldn't be accessed
     */
    public boolean setVotes(UUID player, int votes) throws SQLException, ClassNotFoundException {
	if (player == null) {
	    return false;
	}
	PreparedStatement setVotesStatement = getConnection().prepareStatement("INSERT INTO universalvotes (playeruuid, votes, lastdate) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE playeruuid = VALUES(playeruuid), votes = VALUES(votes), lastdate = VALUES(lastdate)");
	setVotesStatement.setString(1, player.toString());
	setVotesStatement.setInt(2, votes);
	setVotesStatement.setDate(3, new java.sql.Date(System.currentTimeMillis()));
	setVotesStatement.execute();
	return true;
    }

    /**
     * Gets the amount of votes of the specified player.
     *
     * @param playerName the name of the player whose amount of votes should be
     * given
     * @return the amount of votes of the player, or 0 if the player couldn't be
     * found
     * @throws SQLException if the database couldn't be accessed
     * @throws ClassNotFoundException if the database couldn't be accessed
     * @deprecated use {@link #getVotes(UUID)}
     */
    @Deprecated
    public int getVotes(String playerName) throws SQLException, ClassNotFoundException {
	return getVotes(getUUID(playerName));
    }
    
    /**
     * Gets the amount of votes of the specified player.
     *
     * @param player the UUID of the player whose amount of votes should be
     * given
     * @return the amount of votes of the player, or 0 if the player couldn't be
     * found
     * @throws SQLException if the database couldn't be accessed
     * @throws ClassNotFoundException if the database couldn't be accessed
     */
    public int getVotes(UUID player) throws SQLException, ClassNotFoundException {
	if (player == null) {
	    return 0;
	}
	Statement statement = getConnection().createStatement();
	ResultSet rs = statement.executeQuery("SELECT votes FROM universalvotes WHERE playeruuid = '" + player + "';");
	if (rs.next()) {
	    return rs.getInt("votes");
	} else {
	    return 0;
	}
    }

    /**
     * Increments the player's vote count with one.
     *
     * @param playerName the name of the player whose vote count should be
     * incremented
     * @return false if the UUID of the specified player couldn't be found and
     * the query was not executed; true otherwise
     * @throws SQLException if the database couldn't be accessed
     * @throws ClassNotFoundException if the database couldn't be accessed
     * @deprecated use {@link #addVote(UUID)}
     */
    @Deprecated
    public boolean addVote(String playerName) throws SQLException, ClassNotFoundException {
	return addVote(getUUID(playerName));
    }
    
    /**
     * Increments the player's vote count by one.
     *
     * @param player the UUID of the player whose vote count should be
     * incremented
     * @return false if the UUID of the specified player couldn't be found and
     * the query was not executed; true otherwise
     * @throws SQLException if the database couldn't be accessed
     * @throws ClassNotFoundException if the database couldn't be accessed
     */
    public boolean addVote(UUID player) throws SQLException, ClassNotFoundException {
	return setVotes(player, getVotes(player) + 1);
    }

    /**
     * Removes an amount of votes from a player's vote count.
     *
     * @param playerName the player whose votes to lessen
     * @param toRemove the amount of votes that should be subtracted from the
     * player's vote count
     * @return false if the player does not have enough votes for this
     * subtraction; true otherwise
     * @throws SQLException if the database couldn't be accessed
     * @throws ClassNotFoundException if the database couldn't be accessed
     * @deprecated use {@link #removeVotes(UUID, int)}
     */
    @Deprecated
    public boolean removeVotes(String playerName, int toRemove) throws SQLException, ClassNotFoundException {
	UUID uuid = getUUID(playerName);
	int currentVotes = getVotes(uuid);
	if (currentVotes < toRemove) {
	    // Insufficient votes
	    return false;
	} else {
	    setVotes(uuid, currentVotes - toRemove);
	    return true;
	}
    }
    
    /**
     * Removes an amount of votes from a player's vote count.
     *
     * @param player the Unique User ID of the player whose votes to lessen
     * @param toRemove the amount of votes that should be subtracted from the
     * player's vote count
     * @return false if the player does not have enough votes for this
     * subtraction; true otherwise
     * @throws SQLException if the database couldn't be accessed
     * @throws ClassNotFoundException if the database couldn't be accessed
     */
    public boolean removeVotes(UUID player, int toRemove) throws SQLException, ClassNotFoundException {
	int currentVotes = getVotes(player);
	if (currentVotes < toRemove) {
	    // Insufficient votes
	    return false;
	} else {
	    setVotes(player, currentVotes - toRemove);
	    return true;
	}
    }

    /**
     * Gets the date on which a player voted for the last time.
     *
     * @param playerName the name of the player whose last vote time should be
     * given
     * @return the date of the last time the player voted, or null if the player couldn't be found
     * @throws SQLException if the database couldn't be accessed
     * @throws ClassNotFoundException if the database couldn't be accessed
     * @deprecated use {@link #getLastVoteDate(UUID)}
     */
    @Deprecated
    public Date getLastVoteDate(String playerName) throws ClassNotFoundException, SQLException {
	String uuid = plugin.getServer().getOfflinePlayer(playerName).getUniqueId().toString();
	Statement statement = getConnection().createStatement();
	ResultSet rs = statement.executeQuery("SELECT lastdate FROM universalvotes WHERE playeruuid = '" + uuid + "';");
	if (rs.next()) {
	    return rs.getDate("lastdate");
	} else {
	    return null;
	}
    }

    /**
     * Gets the date on which a player voted for the last time.
     *
     * @param player the UUID of the player whose last vote time should be
     * given
     * @return the date of the last time the player voted, or null if the player couldn't be found
     * @throws SQLException if the database couldn't be accessed
     * @throws ClassNotFoundException if the database couldn't be accessed
     */
    public Date getLastVoteDate(UUID player) throws ClassNotFoundException, SQLException {
	Statement statement = getConnection().createStatement();
	ResultSet rs = statement.executeQuery("SELECT lastdate FROM universalvotes WHERE playeruuid = '" + player.toString() + "';");
	if (rs.next()) {
	    return rs.getDate("lastdate");
	} else {
	    return null;
	}
    }

}
