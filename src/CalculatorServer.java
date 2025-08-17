import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class CalculatorServer {
    public static void main(String[] args) {
        // Port: RMI registry port (1099)
        int port = (args.length >= 1) ? Integer.parseInt(args[0]) : 1099;

        // Name: binding name (Calculator)
        String name = (args.length >= 2) ? args[1] : "Calculator";

        // Mode: "shared" or "bonus"
        String mode = (args.length >= 3) ? args[2] : "shared";

        try {
            // Start or get registry
            try {
                LocateRegistry.createRegistry(port);
            } catch (Exception ignored) {
            }
            Registry registry = LocateRegistry.getRegistry(port);

            Calculator svc;
            if ("bonus".equalsIgnoreCase(mode)) {
                svc = new ClientStackCalculator();
                System.out.println("Start Calculator Server in per-client stack mode");
            } else {
                svc = new CalculatorImplementation();
                System.out.println("Start Calculator Server with Share stack");
            }

            registry.rebind(name, svc);
            System.out.printf("Server bound as '%s' on port %d%n", name, port);
            System.out.println("Press Ctrl+C to stop.");
        } catch (Exception e) {
            // Log and exit if server setup fails
            System.err.println("Server error: " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }
}