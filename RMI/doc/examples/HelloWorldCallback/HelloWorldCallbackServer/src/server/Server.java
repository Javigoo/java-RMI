package server;

import common.HelloWorldServer;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by eloigabal on 05/10/2019.
 */

public class Server {
    private static Registry startRegistry(Integer port)
            throws RemoteException {
        if(port == null) {
            port = 1099;
        }
        try {
            Registry registry = LocateRegistry.getRegistry(port);
            registry.list( );
            // The above call will throw an exception
            // if the registry does not already exist
            return registry;
        }
        catch (RemoteException ex) {
            // No valid registry at that port.
            System.out.println("RMI registry cannot be located ");
            Registry registry= LocateRegistry.createRegistry(port);
            System.out.println("RMI registry created at port ");
            return registry;
        }
    }

    public static void main(String args[]) {
        try {
            Registry registry = startRegistry(null);
            HelloWorldImplementation obj = new HelloWorldImplementation();
            registry.bind("Hello", (HelloWorldServer) obj);
            System.err.println("Server ready. register clients and notify each 5 seconds");
            while(true) {
                Thread.sleep(5000);
                System.err.println("Server will notify all registered clients");
                obj.notify_clients();
            }
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString()); e.printStackTrace();
        }
    }
}

