import java.rmi.*;

public interface FilaURL_I extends Remote {
    public String sendUrl() throws java.rmi.RemoteException;
    public void recUrl(String url) throws java.rmi.RemoteException;
}