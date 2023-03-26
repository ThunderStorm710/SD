import java.rmi.*;
import java.util.ArrayList;
import java.util.HashSet;

public interface StorageBarrel_I extends Remote {

    public HashSet<String[]> obterInfoBarrel(String palavra) throws RemoteException;

    public HashSet<String> obterLinks(String url) throws RemoteException;

}