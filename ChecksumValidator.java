import java.io.IOException;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;

public class ChecksumValidator {

    public void compareDatabaseChecksum(String checksum, ReceiveMessageInterface member, List<ReceiveMessageInterface> groupMembers) 
            throws RemoteException, IOException, NoSuchAlgorithmException {
        String memberChecksum = member.getDatabaseChecksum();
        if (!checksum.equals(memberChecksum)) {
            System.out.println("Checksum mismatch with member " + member.getName() + "!");
            System.out.println("Leader checksum: " + checksum);
            System.out.println("Member checksum: " + memberChecksum);

            // Remove the mismatching member from the group
            removeMember(member, groupMembers);
        }
        System.out.println("No checksum mismatch with member " + member.getName());
    }

    public void compareDatabaseChecksumWithAll(String checksum, List<ReceiveMessageInterface> groupMembers) 
            throws RemoteException, IOException, NoSuchAlgorithmException {
        // Uses an iterator to safely remove members during iteration
        Iterator<ReceiveMessageInterface> iterator = groupMembers.iterator();
        while (iterator.hasNext()) {
            ReceiveMessageInterface member = iterator.next();
            compareDatabaseChecksum(checksum, member, groupMembers);
        }
    }

    private void removeMember(ReceiveMessageInterface member, List<ReceiveMessageInterface> groupMembers) throws RemoteException {
        // Uses an iterator to safely remove the member
        Iterator<ReceiveMessageInterface> iterator = groupMembers.iterator();
        while (iterator.hasNext()) {
            ReceiveMessageInterface currentMember = iterator.next();
            if (currentMember.equals(member)) {
                iterator.remove();
                System.out.println("Removed member: " + member.getName());
                break; // Exit after removing the member
            }
        }
    }
}