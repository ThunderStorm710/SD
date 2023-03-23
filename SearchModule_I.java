import java.rmi.*;

public interface SearchModule_I extends Remote {
    public void indexarURL(ClienteInfo cliente, String url) throws RemoteException;

    public void pesquisarPaginas(ClienteInfo cliente, String pesquisa) throws RemoteException;

    public ClienteInfo verificarLogin(String username, String password) throws RemoteException;

    public ClienteInfo verificarRegisto(String nome, String email, String username, String password) throws RemoteException;

}