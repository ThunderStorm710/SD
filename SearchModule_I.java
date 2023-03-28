import java.rmi.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public interface SearchModule_I extends Remote {
    public void indexarURL(ClienteInfo cliente, String url) throws RemoteException;

    public HashSet<String[]> pesquisarPaginas(ClienteInfo cliente, String pesquisa) throws RemoteException;

    public ClienteInfo verificarLogin(String username, String password) throws RemoteException;

    public ClienteInfo verificarRegisto(String nome, String email, String username, String password) throws RemoteException;

    public boolean adicionarInfoInicialBarrel(String gama, String ip, String porto) throws RemoteException;

    public ArrayList<HashSet<String>> obterLinks(ClienteInfo cliente, String url) throws RemoteException;

    public ArrayList<Storage> obterInfoBarrels() throws RemoteException;

    public ArrayList<DownloaderInfo> obterInfoDownloaders() throws RemoteException;

    public HashMap<String, HashSet<String[]>> obterInfoFicheiros(String gama, String ip, String porto) throws RemoteException;

    public HashMap<String, HashSet<String>> obterURLFicheiros(String gama, String ip, String porto) throws RemoteException;


}