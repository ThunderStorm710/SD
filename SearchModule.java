import java.io.*;
import java.util.ArrayList;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.util.HashSet;


public class SearchModule extends UnicastRemoteObject implements SearchModule_I {


    private ArrayList<ClienteInfo> clientes;
    private ArrayList<Storage> barrels;

    public SearchModule() throws RemoteException {
        super();
        this.clientes = new ArrayList<ClienteInfo>();
        this.barrels = new ArrayList<Storage>();
    }


    synchronized public void indexarURL(ClienteInfo cliente, String url) throws RemoteException {
        if (verificarCliente(cliente)) {
            try {
                FilaURL_I h = (FilaURL_I) LocateRegistry.getRegistry(1099).lookup("fila_url");
                System.out.println("Vou adicionar o url " + url + " a fila!");
                h.recUrl(url);


            } catch (Exception e) {
                System.out.println("Error" + e);
            }

        }
    }
    synchronized public HashSet<String[]> pesquisarPaginas(ClienteInfo cliente, String pesquisa) throws RemoteException {
        ArrayList<HashSet<String[]>> lista = new ArrayList<>();
        HashSet<String[]> aux;
        if (verificarCliente(cliente)) {
            try {
                String[] palavras = pesquisa.split(" ");
                System.out.println("--- PESQUISA ---");
                System.out.println(barrels);
                for (String palavra : palavras) {
                    for (Storage s : barrels) {
                        System.out.println(palavra.charAt(0));
                        System.out.println(s.getGama().charAt(1));
                        System.out.println(s.getGama().charAt(3));
                        if (Character.toUpperCase(palavra.charAt(0)) >= s.getGama().charAt(1) && Character.toUpperCase(palavra.charAt(0)) <= s.getGama().charAt(3)) {
                            StorageBarrel_I sI = (StorageBarrel_I) LocateRegistry.getRegistry(s.getPorto()).lookup("Storage_Barrel");
                            aux = sI.obterInfoBarrel(palavra);
                            lista.add(aux);
                            System.out.println(aux);

                        }
                    }
                    System.out.println(palavra);

                }

                System.out.println("--- FIM PESQUISA ---");
                return intersection(lista);

            } catch (Exception e) {
                System.out.println("Error" + e);
            }
        }
        return null;
    }

    public static HashSet<String[]> intersection(ArrayList<HashSet<String[]>> sets) {
        // passo 1
        HashSet<String> allLinks = new HashSet<>();
        for (String[] set : sets.get(0)) {
            allLinks.add(set[0]);
        }

        // passo 2
        for (int i = 1; i < sets.size(); i++) {
            HashSet<String[]> currentSet = sets.get(i);
            HashSet<String> currentLinks = new HashSet<>();
            for (String[] set : currentSet) {
                currentLinks.add(set[0]);
            }
            allLinks.retainAll(currentLinks); // passo 3
        }

        // passo 4
        HashSet<String[]> result = new HashSet<>();

        // passo 5
        for (String[] set : sets.get(0)) {
            if (allLinks.contains(set[0])) {
                result.add(set);
            }
        }

        return result; // passo 6
    }



    synchronized public boolean verificarCliente(ClienteInfo cliente) {

        for (ClienteInfo c : clientes) {
            if (cliente.getNome().equals(c.getNome())) {
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
        if (gama.length() != 5) {
            System.out.println("1");
            flag = false;
        } else if ((gama.charAt(0) == 'A' && gama.charAt(gama.length() - 2) != 'M') || (gama.charAt(0) == 'N' && gama.charAt(gama.length() - 2) != 'Z') || (gama.charAt(0) == 'a' && gama.charAt(gama.length() - 2) != 'm') || (gama.charAt(0) == 'n' && gama.charAt(gama.length() - 2) != 'z')) {
            System.out.println("2");
            flag = false;
        } else {
            if (barrels != null){
                for (Storage s : barrels) {
                    if (s.getPorto().equals(porto)) {
                        flag = false;
                        System.out.println("3");
                    }
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