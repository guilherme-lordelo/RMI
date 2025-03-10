
import java.rmi.*;
import java.rmi.registry.*;
import java.sql.SQLException;

public class RmiClient {
    static public void main(String args[]) {
        ReceiveMessageInterface rmiServer;
        Registry registry;
        String serverAddress = args[0];  // Leader's address
        int serverPort = Integer.parseInt(args[1]);  // Port number
        String command = args[2];  //  command to be sent
        
        System.out.println("Sending  command '" + command + "' to " + serverAddress + ":" + serverPort);
        
        try {
            // Get the registry and lookup the leader's remote object
            registry = LocateRegistry.getRegistry(serverAddress, serverPort);
            rmiServer = (ReceiveMessageInterface) registry.lookup("rmiLeader");  // Leader ID is 0
            
            if ("checkMemberChecksums".equals(command)) {
                // Call the checkMemberChecksums method on the remote object
               ((LeaderInterface) rmiServer).checkMemberChecksums();
            } else {
                // Otherwise, handle as a SQL command (like before)
                rmiServer.receiveSQLCommand(command);
            }
            
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        } catch (SQLException ex) {
        }
    }
}
