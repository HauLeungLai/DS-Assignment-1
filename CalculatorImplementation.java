import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReentrantLock;

public class CalculatorImplementation extends UnicastRemoteObject implements Calculator {
    // Shared stack for storing integer values
    private final Deque<Integer> stack = new ArrayDeque<>();
    // Fair lock to make sure fair for multi-client access
    private final ReentrantLock lock = new ReentrantLock(true);

    public CalculatorImplementation() throws RemoteException {
        super();
    }

    @Override
    public void pushValue(int val) throws RemoteException {
        lock.lock();
        try {
            // Push a value onto the stack
            stack.push(val);
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void pushOperation(String operator) throws RemoteException {
        lock.lock();
        try {
            if (stack.isEmpty()){
                // Fail if the stack is empty
                throw new IllegalStateException("empty stack");
            }

            // Pop all current values on the stack
            Deque<Integer> popped = new ArrayDeque<>();
            while (!stack.isEmpty()){
                popped.push(stack.pop());
            }

            int result;
            // Apply the requested operation
            switch (operator){
                case "min":
                    result = popped.stream().min(Integer::compareTo).orElseThrow();
                    break;
                case "max":
                    result = popped.stream().max(Integer::compareTo).orElseThrow();
                    break;
                case "lcm":
                    result = reduceLcm(popped);
                    break;
                case "gcd":
                    result = reduceGcd(popped);
                    break;
                default:
                    throw new IllegalStateException("operator error: " + operator);

            }

            // Push the calculated result back onto the stack
            stack.push(result);
        }finally {
            lock.unlock();
        }
    }

    @Override
    public int pop() throws RemoteException{
        lock.lock();
        try{
            // Remove and return the top value
            Integer x = stack.poll();
            if (x == null) throw new RemoteException("empty stack");
            return x;
        }finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isEmpty() throws RemoteException{
        lock.lock();
        try{
            // Return true if stack empty
            return stack.isEmpty();
        }finally {
            lock.unlock();
        }
    }

    @Override
    public int delayPop(int millis) throws RemoteException{
        // wait outside the lock allows clients to modify the stack
        try{
            Thread.sleep(millis);
        }catch (InterruptedException ie){
            Thread.currentThread().interrupt();
            throw new RemoteException("delayPop interrupted", ie);
        }
        // Perform a normal pop
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

