package tweetcapturing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLBridge {
    /**
     * @uml.property  name="connect"
     */
    private Connection connect = null;

    /**
     * 
     * @param server server name
     * @param user username
     * @param passwd db password
     * @param db schema name
     * @param table table in the schema
     * @throws ClassNotFoundException
     * @throws SQLException 
     */
    public MySQLBridge(String server, String user, String passwd, String db, String table) 
            throws ClassNotFoundException,SQLException {
        // Caricamento dei driver per la connessione al DB
        Class.forName("com.mysql.jdbc.Driver");
        
        //inizializzo la connessione al DB
        if(passwd.equals("NULL")) {
            connect = DriverManager.getConnection("jdbc:mysql://"+server,user,null);
        } else {
            connect = DriverManager.getConnection("jdbc:mysql://"+server+"?"+"user="+user+"&password="+passwd);
        }
        
        connect.setAutoCommit(false);

        connectSchema(db, table);
        
    }
    
    private void connectSchema(String db, String table) throws SQLException{
        //se non esistono, creo lo schema e le tabelle
        String trydb = "CREATE DATABASE IF NOT EXISTS `"+db+"`;";
        String usedb = "USE `"+db+"`";
        String tableDefinition = "CREATE TABLE IF NOT EXISTS `"+db+"`.`"+table+"` (" +
                                   " `statusId` BIGINT NOT NULL," +
                                   " `userId` BIGINT NULL," +
                                   " `text` TEXT NULL," +
                                   " `timestamp` TIMESTAMP NULL," +
                                   " `favoriteCount` INT NULL," +
                                   " `latitude` FLOAT(10,6) NULL," +
                                   " `longitude` FLOAT(10,6) NULL," +
                                   " `lang` TINYTEXT NULL," +
                                   " `place` TINYTEXT NULL," +
                                   " `retweetCount` INT NULL," +
                                   " `msgType` BIGINT NULL," +
                                   " PRIMARY KEY (`statusId`))" +
                                   " ENGINE = InnoDB" +
                                   " DEFAULT CHARACTER SET = utf8" +
                                   " COLLATE = utf8_unicode_ci;";
                                   //TODO: try utf8mb4_bin
                                   //utf8_unicode_ci
        Statement statement = connect.createStatement();
        statement.addBatch(trydb);
        statement.addBatch(usedb);
        statement.addBatch(tableDefinition);
        statement.executeBatch();
    }

    
    /**
     * query example in mysql
     * INSERT INTO `twitter_test`.`test` (`statusId`, `userId`, `text`, `timestamp`, `favoriteCount`, `latitude`, `longitude`, `lang`, `place`, `retweetCount`) VALUES ('456', '46', 'jfhg', '2016-09-10 09:33:30', '0', '15.4', '2.456', 'eng', '456', '0');
     * @param query simple query as a string
     * @return as ResultSet
     * @throws java.sql.SQLException
     */
    public ResultSet retrieveData(String query) throws SQLException{
        Statement statement = connect.createStatement();
        ResultSet resultSet = statement.executeQuery(query);
        return resultSet;
    }

    public long insertData(String query) throws SQLException{
        //System.out.println("tweetcapturing.MySQLBridge.insertData()");
        Statement statement = connect.createStatement();
        long id=statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
        statement.close();
        return id;
    }


    public Connection getConnection(){
        return connect;
    }

    public void closeConnection() {
        //chiudo tutti gli oggetti del DB
        try {
            if(connect!=null) {
                    connect.commit();
                    connect.close();
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
