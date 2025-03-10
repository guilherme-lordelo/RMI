import java.io.IOException;
import java.net.InetAddress;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.*;

public class RmiLeader extends UnicastRemoteObject implements LeaderInterface {
    private static final long serialVersionUID = 1L;
    private int thisPort = 3232;
    private String thisAddress;
    private Registry registry;
    private List<ReceiveMessageInterface> groupMembers = new ArrayList<>();
    private HealthChecker healthChecker;
    private DatabaseHandler databaseHandler;
    private ReplicationManager replicationManager;
    private ChecksumValidator checksumValidator;

    byte[] databaseBytes;

    public RmiLeader() throws RemoteException, SQLException {
        super();
        try {
            thisAddress = InetAddress.getLocalHost().toString();
        } catch (Exception e) {
            throw new RemoteException("Can't get Inet address.");
        }

        System.out.println("Leader address=" + thisAddress + ", port=" + thisPort);

        try {
            registry = LocateRegistry.createRegistry(thisPort);
            registry.rebind("rmiLeader", this);
            databaseHandler = new DatabaseHandler("leader.db");
            healthChecker = new HealthChecker(groupMembers);
            replicationManager = new ReplicationManager(groupMembers);
            checksumValidator = new ChecksumValidator();

            healthChecker.startHealthCheck();
            System.out.println("I am the leader!");
        } catch (RemoteException | SQLException e) {
            throw e;
        }
    }

    @Override
    public String getName() {
        return "Leader";
    }

    @Override
    public boolean isAlive() throws RemoteException {
        return true;
    }

    @Override
public void receiveSQLCommand(String sqlCommand) throws RemoteException, SQLException {
    System.out.println("Leader received command: " + sqlCommand);

    // Execute the SQL command on the leader's database and print the table
    databaseHandler.executeSQLCommand(sqlCommand);

    // Replicate the command to group members
    replicationManager.replicateSQLCommand(sqlCommand);
}

    @Override
    public void addMember(ReceiveMessageInterface member) throws RemoteException {
        try {
            databaseBytes = databaseHandler.readDatabaseFile("leader.db");
        } catch (IOException ex) {
        }
        replicationManager.sendDatabaseToMember(member, databaseBytes);
        groupMembers.add(member);
        System.out.println("Added member: " + member.getName());
    }

    @Override
    public void receiveDatabase(byte[] databaseBytes) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void checkMemberChecksums() {
        System.out.println("Leader is checking member checksums...");
        try {
            String leaderChecksum = getDatabaseChecksum(); // Get leader's checksum
            checksumValidator.compareDatabaseChecksumWithAll(leaderChecksum, groupMembers);
        } catch (RemoteException e) {
            System.err.println("Remote communication error: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Checksum algorithm not found: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            RmiLeader leader = new RmiLeader();
            System.out.println("Leader is running on port " + leader.thisPort);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}