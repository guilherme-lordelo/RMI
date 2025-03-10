import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.sql.*;
import java.util.*;

public class RmiMember extends UnicastRemoteObject implements ReceiveMessageInterface {
    private static final long serialVersionUID = 1L;
    private int serverId;
    private Connection dbConnection;
    private DatabaseHandler databaseHandler;

    public RmiMember(int serverId) throws RemoteException, SQLException, NotBoundException {
        super();
        this.serverId = serverId;
        this.databaseHandler = new DatabaseHandler("member" + serverId + ".db");

        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 3232);
            LeaderInterface leader = (LeaderInterface) registry.lookup("rmiLeader");
            leader.addMember(this);
            System.out.println("I am a member with ID " + serverId);
        } catch (RemoteException e) {
            e.printStackTrace();
            throw new RemoteException("Error connecting to leader.");
        }
    }

    @Override
    public void receiveSQLCommand(String sqlCommand) throws RemoteException, SQLException {
        System.out.println("Member received command: " + sqlCommand);
        databaseHandler.executeSQLCommand(sqlCommand); // Call on instance, not statically
    }

    @Override
    public String getName() {
        return "Member " + serverId;
    }

    @Override
    public boolean isAlive() throws RemoteException {
        return true;
    }

    public ResultSet selectFromTable(String tableName) throws RemoteException, SQLException {
        Statement stmt = dbConnection.createStatement();
        return stmt.executeQuery("SELECT * FROM " + tableName);
    }

    public String[] getDatabaseNames() throws RemoteException, SQLException {
        Statement stmt = dbConnection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table';");
        
        List<String> databases = new ArrayList<>();
        while (rs.next()) {
            databases.add(rs.getString("name"));
        }
        
        rs.close();
        stmt.close();
        
        return databases.toArray(new String[0]);
    }

    @Override
    public void receiveDatabase(byte[] databaseBytes) throws RemoteException {
        try (FileOutputStream fos = new FileOutputStream("member" + serverId + ".db")) {
            fos.write(databaseBytes);
            System.out.println("Database received and written to file.");
        } catch (IOException e) {
            System.out.println("Error writing database file.");
        }
    }

    public static void main(String[] args) {
        try {
            if (args.length < 1) {
                System.out.println("Usage: java RmiMember <serverId>");
                System.exit(1);
            }

            int serverId = Integer.parseInt(args[0]);
            RmiMember member = new RmiMember(serverId);
        } catch (NumberFormatException e) {
            System.out.println("Error: serverId must be an integer.");
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}