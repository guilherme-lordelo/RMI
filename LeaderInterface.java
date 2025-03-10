
import java.rmi.*;

public interface LeaderInterface extends ReceiveMessageInterface {
    void addMember(ReceiveMessageInterface member) throws RemoteException;
    void checkMemberChecksums() throws IllegalArgumentException, RemoteException;
}
