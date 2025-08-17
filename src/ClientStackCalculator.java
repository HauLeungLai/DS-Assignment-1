import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

public class ClientStackCalculator extends UnicastRemoteObject implements Calculator {

    // One stack per client host
    private final Map<String, Deque<Integer>> stacks = new ConcurrentHashMap<>();

    public ClientStackCalculator() throws RemoteException {
        super();
    }

    private Deque<Integer> stackForCaller() throws RemoteException {
        try {
            String host = RemoteServer.getClientHost();
            return stacks.computeIfAbsent(host, h -> new ArrayDeque<>());
        } catch (ServerNotActiveException e) {
            throw new RemoteException("Unable to find client identity", e);
        }
    }

    @Override
    public synchronized void pushValue(int val) throws RemoteException {
        // Push a value onto the client's stack
        stackForCaller().push(val);
    }

    @Override
    public synchronized void pushOperation(String operator) throws RemoteException {
        // Apply an operation to all values
        Deque<Integer> s = stackForCaller();
        if (s.isEmpty()) {
            throw new IllegalStateException("pushOperation called on empty stack");
        }

        // Pop all values on the stack
        Deque<Integer> popped = new ArrayDeque<>();
        while (!s.isEmpty()) {
            popped.push(s.pop());
        }

        // Calculate the result
        int result;
        switch (operator) {
            case "min":
                result = popped.stream().min(Integer::compareTo).orElseThrow();
                break;
            case "max":
                result = popped.stream().max(Integer::compareTo).orElseThrow();
                break;
            case "gcd":
                result = reduceGcd(popped);
                break;
            case "lcm":
                result = reduceLcm(popped);
                break;
            default:
                throw new IllegalArgumentException("Unsupported operator: " + operator);
        }

        // Push the single result back
        s.push(result);
    }

    @Override
    public synchronized int pop() throws RemoteException {
        Deque<Integer> s = stackForCaller();
        Integer v = s.poll();
        if (v == null) throw new RemoteException("Pop on empty stack");
        return v;
    }

    @Override
    public synchronized boolean isEmpty() throws RemoteException {
        return stackForCaller().isEmpty();
    }

    @Override
    public int delayPop(int millis) throws RemoteException {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RemoteException("delayPop interrupted", e);
        }
        return pop();
    }

    // Helper methods for Gcd and Lcm
    //Gcd
    private static int gcd(int a, int b){
        a = Math.abs(a);
        b = Math.abs(b);
        if (a == 0) return b;
        if (b == 0) return a;
        while (b != 0){
            int t = a % b;
            a = b;
            b = t;
        }
        return a;
    }

    //Lcm
    private static int lcm(int a, int b) {
        if (a == 0 || b == 0) return 0;
        int g = gcd(a, b);
        long res = Math.abs((long) a / g * (long) b);
        return (int) res;
    }

    // Reduce value to their Gcd
    private static int reduceGcd(Deque<Integer> vals) {
        int acc = 0;
        for (int v : vals) acc = gcd(acc, v);
        return acc;
    }

    // Reduce value to their Lcm
    private static int reduceLcm(Deque<Integer> vals) {
        int acc = 1;
        boolean any = false;
        for (int v : vals) {
            any = true;
            acc = lcm(acc, v);
        }
        if (!any) throw new IllegalStateException("No values to reduce");
        return acc;
    }
}
