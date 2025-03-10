import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;

public class ReplicationManager {
    private final List<ReceiveMessageInterface> groupMembers;

    public ReplicationManager(List<ReceiveMessageInterface> groupMembers) {
        this.groupMembers = groupMembers;
    }

    public void replicateSQLCommand(String sqlCommand) throws RemoteException, SQLException {
        for (ReceiveMessageInterface member : groupMembers) {
            member.receiveSQLCommand(sqlCommand);
        }
    }

    public void sendDatabaseToMember(ReceiveMessageInterface member, byte[] database) throws RemoteException {
        try {
            member.receiveDatabase(database);
        } catch (RemoteException e) {
            System.out.println("Error sending database to member: " + member.getName());
        }
    }
}