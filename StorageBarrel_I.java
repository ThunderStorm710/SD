import java.rmi.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public interface StorageBarrel_I extends Remote {

    public HashSet<String[]> obterInfoBarrel(String palavra) throws RemoteException;

    public HashSet<String> obterLinks(String url) throws RemoteException;

    public void adicionarPesquisa(String pesquisa) throws RemoteException;

    public HashMap<String, HashSet<String[]>> obterIndex() throws RemoteException;

    public  HashMap<String, HashSet<String>> obterURLMap() throws RemoteException;

    public HashMap<String, Integer> obterPesquisas() throws RemoteException;

    public ArrayList<ClienteInfo> obterClientesBarrel() throws RemoteException;

    public void adicionarCliente(ClienteInfo cliente) throws RemoteException;

    }