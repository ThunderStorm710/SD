import java.rmi.*;

public interface StorageBarrel_I extends Remote {

    public String[] obterInfoBarrel() throws RemoteException;

}