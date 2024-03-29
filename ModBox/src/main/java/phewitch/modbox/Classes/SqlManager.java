package phewitch.modbox.Classes;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sql.rowset.RowSetProvider;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqlManager {

    public static SqlManager Instance;
    MysqlDataSource dataSource;

    public SqlManager(String host, int port, String database, String user, String password){
        Instance = this;

        dataSource = new MysqlConnectionPoolDataSource();

        dataSource.setServerName(host);
        dataSource.setPort(port);
        dataSource.setDatabaseName(database);
        dataSource.setUser(user);
        dataSource.setPassword(password);
    }

    public boolean checkConnection(){
        try (Connection conn = dataSource.getConnection()) {
            if (!conn.isValid(5)) {
                return false;
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace(); // This should be replaced with a propper logging solution. don't do this.
            return false;
        }
    }

    public ResultSet getFromDatabase(@NotNull String query, String[] parameters){
        try(Connection conn = dataSource.getConnection()){
            var statement = conn.prepareStatement(query);

            int index = 1;
            for (String parameter : parameters) {
                statement.setString(index, parameter);
                index ++;
            }

            var resultSet = statement.executeQuery();
            var rowSet = RowSetProvider.newFactory().createCachedRowSet();
            rowSet.populate(resultSet);

            if(rowSet.next()) {
                return rowSet;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace(); // This should be replaced with a propper logging solution. don't do this.
            return null;
        }
    }

    public boolean writeToDatabase(@NotNull String query, String[] parameters){
        try(Connection conn = dataSource.getConnection()){
            var statement = conn.prepareStatement(query);

            int index = 1;
            for (String parameter : parameters) {
                statement.setString(index, parameter);
                index ++;
            }

            return statement.execute();
        } catch (SQLException e) {
            e.printStackTrace(); // This should be replaced with a propper logging solution. don't do this.
            return false;
        }
    }

    public static @Nullable String getResult(ResultSet set, String columnName){
        try {
            return set.getString(columnName);
        } catch (SQLException e) {
            Bukkit.getLogger().warning(e.toString());
            return null;
        }
    }
}
