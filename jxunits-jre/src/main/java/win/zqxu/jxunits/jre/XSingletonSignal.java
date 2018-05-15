package win.zqxu.jxunits.jre;

import java.rmi.AlreadyBoundException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class to make singleton signal between JVMs
 */
public class XSingletonSignal {
  private static Map<String, SingletonService> holder;
  private static Map<String, List<Runnable>> handler;

  /**
   * start singleton signal if possible, failed if another JVM already start the singleton
   * signal for the name
   * 
   * @param name
   *          the singleton signal name
   * @return true if start succeed, false if failed.
   * @throws RemoteException
   *           if call RMI failed
   */
  public static boolean start(String name) throws RemoteException {
    Registry registry;
    try {
      registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
    } catch (RemoteException ex) {
      registry = LocateRegistry.getRegistry();
    }
    try {
      SingletonSignal service = (SingletonSignal) registry.lookup(name);
      service.notifySingleton(name);
      return false;
    } catch (NotBoundException ex) {
      try {
        SingletonService service = createSingleton(name);
        registry.bind(name, UnicastRemoteObject.exportObject(service, 0));
      } catch (AlreadyBoundException ex2) {
        throw new RemoteException("starting conflict", ex2);
      }
      return true;
    }
  }

  private static SingletonService createSingleton(String name) {
    if (holder == null) {
      holder = new ConcurrentHashMap<>();
    }
    if (!holder.containsKey(name)) {
      holder.put(name, new SingletonService());
    }
    return holder.get(name);
  }

  /**
   * handle singleton signal notify, when another singleton signal for the name trying to
   * start, the callback will be called
   * 
   * @param name
   *          the singleton signal name
   * @param callback
   *          the callback to call
   */
  public static void handle(String name, Runnable callback) {
    if (handler == null) {
      handler = new ConcurrentHashMap<>();
    }
    List<Runnable> list = handler.get(name);
    if (list == null) {
      handler.put(name, list = new ArrayList<>());
    }
    if (!list.contains(callback)) list.add(callback);
  }

  /**
   * stop singleton signal for the name, NO-OP if the singleton signal not started by
   * current process
   * 
   * @param name
   *          the singleton signal name
   */
  public static void stop(String name) {
    try {
      if (holder != null && holder.containsKey(name)) {
        UnicastRemoteObject.unexportObject(holder.remove(name), true);
      }
    } catch (NoSuchObjectException ex) {
      // safety ignore this exception
    }
  }

  private static interface SingletonSignal extends Remote {
    public void notifySingleton(String name) throws RemoteException;
  }

  private static class SingletonService implements SingletonSignal {
    @Override
    public void notifySingleton(String name) {
      if (handler == null) return;
      List<Runnable> list = handler.get(name);
      if (list == null) return;
      for (Runnable callback : list) callback.run();
    }
  }
}
