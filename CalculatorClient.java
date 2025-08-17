import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class CalculatorClient {
    public static void main(String[] args) {
        String host = (args.length >= 1) ? args[0] : "localhost";
        int port = (args.length >= 2) ? Integer.parseInt(args[1]) : 1099;
        String name = (args.length >= 3) ? args[2] : "Calculator";

        try {
            // Get reference to the registry at the specified host and port
            Registry registry = LocateRegistry.getRegistry(host, port);

            // Look up the Calculator service in the registry
            Calculator calc = (Calculator) registry.lookup(name);

            // Test gcd: push 6, 9, 15 -> gcd = 3
            calc.pushValue(6);
            calc.pushValue(9);
            calc.pushValue(15);
            calc.pushOperation("gcd");
            System.out.println("After gcd of {6,9,15}, pop() => " + calc.pop()); // Expected: 3

            // Test min: push 12, 4, 18 -> min = 4
            calc.pushValue(12);
            calc.pushValue(4);
            calc.pushValue(18);
            calc.pushOperation("min");
            System.out.println("min pop => " + calc.pop()); // Expected: 4

            // Test max: push 5, 20, 7 -> max = 20
            calc.pushValue(5);
            calc.pushValue(20);
            calc.pushValue(7);
            calc.pushOperation("max");
            System.out.println("max pop => " + calc.pop()); // Expected: 20

            // Test lcm: push 4, 6, 8 -> lcm = 24
            calc.pushValue(4);
            calc.pushValue(6);
            calc.pushValue(8);
            calc.pushOperation("lcm");
            System.out.println("lcm pop => " + calc.pop()); // Expected: 24

            // Test delayPop: push 42, then pop after 0.5 seconds
            calc.pushValue(42);
            int delayed = calc.delayPop(500); // 500 ms delay
            System.out.println("delayPop(500) => " + delayed); // Expected: 42

            // Final stack check
            System.out.println("isEmpty? " + calc.isEmpty());

        } catch (Exception e) {
            // Handle errors such as connection issues or remote exceptions
            System.err.println("Client error: " + e);
            e.printStackTrace();
            System.exit(2);
        }
    }
}
