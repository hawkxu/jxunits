package win.zqxu.jxunits.jre;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to make singleton between JVMs using RMI
 */
public class XSingletonSignal {
  private static Map<String, Singleton> singletonHolder = new HashMap<>();
  private static Map<String, Runnable> callbackHolder = new HashMap<>();

  /**
   * Start singleton if possible, failed if another JVM or thread already started
   * singleton for the name
   * 
   * @param name
   *          the singleton name
   * @param callback
   *          the callback when another thread try to start same singleton
   * @return true if start succeed, false if failed.
   * @throws RemoteException
   *           if call RMI failed
   */
  public static boolean start(String name, Runnable callback) throws RemoteException {
    synchronized (singletonHolder) {
      if (singletonHolder.containsKey(name))
        throw new RemoteException("the singleton was already started");
      return tryStartSingleton(name, callback);
    }
  }

  private static boolean tryStartSingleton(String name, Runnable callback)
      throws RemoteException {
    Registry registry = getRegistry();
    try {
      // Can not use class Service at this point
      Remote remote = registry.lookup(name);
      if (remote instanceof Singleton) {
        Singleton singleton = (Singleton) remote;
        singleton.notifyOwner(name);
        return false;
      }
      throw new RemoteException("other application started same singleton");
    } catch (NotBoundException ex) {
      try {
        Singleton singleton = new Service();
        registry.bind(name, UnicastRemoteObject.exportObject(singleton, 0));
        singletonHolder.put(name, singleton);
        callbackHolder.put(name, callback);
        return true;
      } catch (AlreadyBoundException ex2) {
        throw new RemoteException("Conflict while starting singleton", ex2);
      }
    }
  }

  private static Registry getRegistry() throws RemoteException {
    try {
      return LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
    } catch (RemoteException ex) {
      return LocateRegistry.getRegistry(); // registry should be started
    }
  }

  private static void notifyByAnother(String name) {
    Runnable callback = callbackHolder.get(name);
    if (callback != null) callback.run();
  }

  /**
   * Determine whether the singleton was started by current process
   * 
   * @param name
   *          the singleton name
   * @return true or false
   */
  public static boolean isOwnerOf(String name) {
    synchronized (singletonHolder) {
      return singletonHolder.containsKey(name);
    }
  }

  /**
   * Stop singleton for the name
   * 
   * @param name
   *          the singleton name
   * @throws RemoteException
   *           if call RMI failed
   */
  public static void stop(String name) throws RemoteException {
    synchronized (singletonHolder) {
      if (singletonHolder.containsKey(name)) {
        Singleton singleton = singletonHolder.get(name);
        UnicastRemoteObject.unexportObject(singleton, true);
        try {
          getRegistry().unbind(name);
        } catch (NotBoundException ex) {
          throw new RemoteException("stop singleton " + name + "failed", ex);
        }
        singletonHolder.remove(name);
        callbackHolder.remove(name);
        return;
      }
    }
    throw new RemoteException("current process not own the singleton " + name);
  }

  private static interface Singleton extends Remote {
    public void notifyOwner(String name) throws RemoteException;
  }

  private static class Service implements Singleton {
    @Override
    public void notifyOwner(String name) throws RemoteException {
      XSingletonSignal.notifyByAnother(name);
    }
  }
}
