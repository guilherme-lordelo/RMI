import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.*;

public class HealthChecker {
    private final List<ReceiveMessageInterface> groupMembers;
    private ScheduledExecutorService healthCheckScheduler;

    public HealthChecker(List<ReceiveMessageInterface> groupMembers) {
        this.groupMembers = groupMembers;
    }

    public void startHealthCheck() {
        healthCheckScheduler = Executors.newSingleThreadScheduledExecutor();
        healthCheckScheduler.scheduleAtFixedRate(() -> {
            System.out.print("Performing health check on members... ");
            
            groupMembers.removeIf(member -> {
                try {
                    return !member.isAlive();
                } catch (RemoteException e) {
                    System.out.println("Member failed! Removing from group.");
                    return true;
                }
            });

            System.out.print("Active members: |Leader| ");
            for (ReceiveMessageInterface member : groupMembers) {
                try {
                    System.out.print("|" + member.getName() + "| ");
                } catch (RemoteException ignored) {
                    // Ignore failed getName() calls
                }
            }
            System.out.print("\n");
        }, 0, 5, TimeUnit.SECONDS);
    }

    public void stopHealthCheck() {
        if (healthCheckScheduler != null) {
            healthCheckScheduler.shutdown();
        }
    }
}