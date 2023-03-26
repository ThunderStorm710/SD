import java.rmi.*;
import java.util.HashSet;

public interface SearchModule_I extends Remote {
    public void indexarURL(ClienteInfo cliente, String url) throws RemoteException;

    public HashSet<String[]> pesquisarPaginas(ClienteInfo cliente, String pesquisa) throws RemoteException;

    public ClienteInfo verificarLogin(String username, String password) throws RemoteException;

    public ClienteInfo verificarRegisto(String nome, String email, String username, String password) throws RemoteException;

    public boolean adicionarInfoInicialBarrel(String gama, String porto) throws RemoteException;

    public void obterLinks(ClienteInfo cliente, String url) throws RemoteException;

}