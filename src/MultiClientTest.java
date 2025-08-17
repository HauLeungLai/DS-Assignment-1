import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class MultiClientTest {
    public static void main(String[] args) {
        String host = (args.length >= 1) ? args[0] : "localhost";
        int port = (args.length >= 2) ? Integer.parseInt(args[1]) : 1099;
        String name = (args.length >= 3) ? args[2] : "Calculator";
        int clients = (args.length >= 4) ? Integer.parseInt(args[3]) : 5;

        try {
            Registry registry = LocateRegistry.getRegistry(host, port);
            Calculator calc = (Calculator) registry.lookup(name);

            System.out.printf("Running multi-client test with %d clients...%n", clients);

            CountDownLatch startGun = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(clients);
            List<Thread> threads = new ArrayList<>();

            for (int i = 0; i < clients; i++) {
                final int id = i;
                Thread t = new Thread(() -> {
                    try {
                        Calculator stub = (Calculator) registry.lookup(name);

                        // wait until all threads are ready to start
                        startGun.await();

                        // Each client pushes three unique values
                        stub.pushValue(2 + id);
                        stub.pushValue(3 + id);
                        stub.pushValue(4 + id);

                        // Choose an operation based on client ID
                        String op = switch (id % 4) {
                            case 0 -> "min";
                            case 1 -> "max";
                            case 2 -> "gcd";
                            default -> "lcm";
                        };

                        // Perform the operation and pop the result
                        stub.pushOperation(op);
                        try {
                            int result = stub.pop();
                            System.out.printf("Client %d: op=%s result=%d%n", id, op, result);
                        } catch (RemoteException e) {
                            System.err.printf("Client %d: pop failed: %s%n", id, e.getMessage());
                        }

                        // Test delayPop
                        stub.pushValue(100 + id);
                        int delayed = stub.delayPop(200 + id * 50);
                        System.out.printf("Client %d: delayPop=%d%n", id, delayed);

                        // Test isEmpty
                        boolean empty = stub.isEmpty();
                        System.out.printf("Client %d: isEmpty? %b%n", id, empty);

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        done.countDown(); // Mark as finished
                    }
                });

                threads.add(t);
                t.start();
            }

            // Start timer
            long start = System.currentTimeMillis();
            startGun.countDown(); // all thread to begin
            done.await(); // wait for all clients to finish
            long elapsed = System.currentTimeMillis() - start;

            System.out.printf("Multi-client test completed in %d ms%n", elapsed);

        } catch (Exception e) {
            System.err.println("Multi-client test error: " + e);
            e.printStackTrace();
        }
    }
}