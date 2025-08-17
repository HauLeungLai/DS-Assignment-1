import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Calculator extends Remote {
    // push an integer onto the stack
    void pushValue(int val) throws RemoteException;

    // Perform the operation (min, max, lcm, gcd)
    void pushOperation(String operator) throws RemoteException;

    // pop and return the top value from the stack
    int pop() throws RemoteException;

    // check if the stack is empty
    boolean isEmpty() throws RemoteException;

    // wait for milliseconds, then perform pop() and return it.
    int delayPop(int millis) throws RemoteException;
}