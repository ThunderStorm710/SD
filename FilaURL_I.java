import java.rmi.*;

public interface FilaURL_I extends Remote {
    public String sendUrl() throws RemoteException;

    public boolean recUrl(String url) throws RemoteException;

    public void adicionarAForca(String url) throws RemoteException;
}