import java.io.*;
import java.rmi.*;
import java.security.*;
import java.sql.*;

public interface ReceiveMessageInterface extends Remote {
    void receiveSQLCommand(String sqlCommand) throws RemoteException, SQLException;
    boolean isAlive() throws RemoteException;
    String getName() throws RemoteException;

    // Default method to calculate the database checksum
    default String getDatabaseChecksum() throws RemoteException, IOException, NoSuchAlgorithmException {
        String dbFilePath = "./" + getName().toLowerCase().replace(" ", "") + ".db";
        return getDatabaseChecksumFromFile(dbFilePath);
    }

    // Helper method to calculate the checksum from the database file
    private static String getDatabaseChecksumFromFile(String dbFilePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        FileInputStream fis = new FileInputStream(dbFilePath);
        byte[] byteArray = new byte[1024];
        int bytesRead;

        while ((bytesRead = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesRead);
        }

        fis.close();
        byte[] hashBytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public void receiveDatabase(byte[] databaseBytes)  throws RemoteException ;
}

