import java.io.*;
import java.util.ArrayList;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;


public class SearchModule extends UnicastRemoteObject implements SearchModule_I {


    private ArrayList<ClienteInfo> clientes;
    private ArrayList<Storage> barrels;

    public SearchModule() throws RemoteException {
        super();
        this.clientes = new ArrayList<ClienteInfo>();
    }


    synchronized public void indexarURL(ClienteInfo cliente, String url) throws RemoteException {
        if (verificarCliente(cliente)) {
            try {
                FilaURL_I h = (FilaURL_I) LocateRegistry.getRegistry(1099).lookup("fila_url");
                h.recUrl(url);


            } catch (Exception e) {
                System.out.println("Error" + e);
            }

        }
    }

    synchronized public void pesquisarPaginas(ClienteInfo cliente, String pesquisa) throws RemoteException {
        if (verificarCliente(cliente)) {
            try {
                String[] palavras = pesquisa.split(" ");
                System.out.println("--- PESQUISA ---");
                for (String palavra : palavras) {
                    for (Storage s: barrels) {
                        if (palavra.charAt(0) >= s.getGama().charAt(0) && palavra.charAt(0) <= s.getGama().charAt(2)){
                            StorageBarrel_I sI = (StorageBarrel_I) LocateRegistry.getRegistry(s.getPorto()).lookup("Storage_Barrel");
                            sI.obterInfoBarrel(palavra);
                        }
                    }
                    System.out.println(palavra);

                }
                System.out.println("--- FIM PESQUISA ---");

            } catch (Exception e) {
                System.out.println("Error" + e);
            }
        }
    }

    synchronized public boolean verificarCliente(ClienteInfo cliente) {

        for (ClienteInfo c : clientes) {
            if (cliente == c) {
                return true;
            }
        }
        return false;
    }

    public ClienteInfo verificarRegisto(String nome, String email, String username, String password) {
        boolean flag = true;
        ClienteInfo c1 = null;

        for (ClienteInfo c : clientes) {
            if (c.getNome().equals(nome) || c.getEmail().equals(email) || c.getUsername().equals(username)) {
                System.out.println("Nome, username ou email  ja se encontram associados a um utilizador ja existente na base de dados...por favor volte a inserir as suas credencias...");
                flag = false;
            } else {
                flag = true;

            }
        }
        if (flag) {
            c1 = new ClienteInfo(nome, username, email, password);
            clientes.add(c1);
            escreverFichObjetos();
            System.out.println("Inscricao validada com sucesso!");
            System.out.println("Seja bem-vindo, " + nome + "!!\n");

        }

        return c1;
    }


    public ClienteInfo verificarLogin(String username, String password) throws RemoteException {
        ClienteInfo c1 = null;
        for (ClienteInfo c : clientes) {
            if (c.getUsername().equals(username) && c.getPassword().equals(password)) {
                c1 = c;
            }
        }
        return c1;
    }

    public void escreverFichObjetos() {
        File fClientesObj = new File("Objetos - Clientes");
        try {
            FileOutputStream iOS = new FileOutputStream(fClientesObj);
            ObjectOutputStream oOS = new ObjectOutputStream(iOS);
            for (ClienteInfo cliente : clientes) {
                oOS.writeObject(cliente);
            }
            oOS.close();
        } catch (IOException e) {
            System.out.println("ERRO " + e);
        }
    }

    public void lerFichClientes() {
        File fClientes = new File("Objetos - Clientes");
        if (fClientes.exists()) {
            ClienteInfo cliente;
            try {
                FileInputStream fIS = new FileInputStream(fClientes);
                ObjectInputStream oIS = new ObjectInputStream(fIS);
                while ((cliente = (ClienteInfo) oIS.readObject()) != null) {
                    clientes.add(cliente);
                }
                oIS.close();
            } catch (EOFException e) {
                System.out.print("");

            } catch (ClassNotFoundException | IOException e) {
                System.out.println("ERRO " + e);
            }
        } else {
            System.out.println("Ficheiro de Objetos de Clientes nao existe...");
        }

    }

    public boolean adicionarInfoInicialBarrel(String gama, String porto) throws RemoteException {
        boolean flag = true;
        if (gama.length() != 3) {
            flag = false;
        } else if ((gama.startsWith("A") && !gama.endsWith("M")) || (gama.startsWith("N") || !gama.endsWith("Z"))) {
            flag = false;
        } else {
            for (Storage s : barrels) {
                if (s.getPorto().equals(porto)) {
                    flag = false;
                }
            }
            if (flag) {
                barrels.add(new Storage(gama, porto));
            }
        }
        return flag;
    }


    public static void main(String[] args) {

        try {
            SearchModule sM = new SearchModule();
            sM.lerFichClientes();

            Registry r = LocateRegistry.createRegistry(1100);
            r.rebind("Search_Module", sM);


        } catch (Exception e) {
            System.out.println("Error" + e);
        }
    }


}