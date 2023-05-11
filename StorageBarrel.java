import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.rmi.RemoteException;

public class StorageBarrel implements Runnable, StorageBarrel_I, Serializable {
    File fClientesObj;
    File fClientesObjHashUrl;
    HashMap<String, HashSet<String[]>> index;
    HashMap<String, HashSet<String>> urlHashmap;
    HashMap<String, Integer> mapaPesquisas;
    ArrayList<ClienteInfo> clientes;
    ArrayList<String> stopwords;
    int type_t;
    String porto;
    String gama_palavra;
    transient Thread t;

    public StorageBarrel(String nome_fich, String nome_fich2, int type_t, String gama_palavra, String porto) {
        t = new Thread(this);
        this.index = new HashMap<>();
        this.urlHashmap = new HashMap<>();
        this.stopwords = new ArrayList<>();
        this.mapaPesquisas = new HashMap<>();
        this.clientes = new ArrayList<>();
        this.fClientesObj = new File(nome_fich);
        this.fClientesObjHashUrl = new File(nome_fich2);
        this.type_t = type_t;
        this.porto = porto;
        this.gama_palavra = gama_palavra;
        t.start();
    }

    public void run() {
        LerArquivoTexto();
        if (type_t == 0) {
            MulticastSocket socket = null;
            String MULTICAST_ADDRESS = "224.3.2.1";
            int PORT = 4321;
            try {
                socket = new MulticastSocket(PORT);  // create socket and bind it
                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                socket.joinGroup(group);
                while (true) {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    //System.out.println("Received packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " with message:");
                    String message = new String(packet.getData(), 0, packet.getLength());

                    String[] protocolo = message.split("\\|");
                    System.out.println("[BARREL] Mensagem recebida " + message);
                    if (protocolo[0].equals("1")) {
                        escreverFichObjetos(message);
                        lerFichObjetos();
                    }
                    if (protocolo[0].equals("2")) {
                        //System.out.println(message);
                        if (!urlHashmap.containsKey(protocolo[1])) {
                            // Se não existir, cria um conjunto de valores
                            HashSet<String> values = new HashSet<>();
                            values.add(protocolo[2]);
                            urlHashmap.put(protocolo[1], values);
                        } else {
                            HashSet<String> values = urlHashmap.get(protocolo[1]);
                            if (values != null) {
                                values.add(protocolo[2]);
                                urlHashmap.put(protocolo[1], values);
                            }
                        }
                        escreverFichObjetosHashmap(urlHashmap);
                        }
                    }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                socket.close();
            }
        } else if (type_t == 1) {

            try {
                SearchModule_I h = (SearchModule_I) LocateRegistry.getRegistry(1100).lookup("Search_Module");
                InetAddress enderecoIP = InetAddress.getLocalHost();
                String ip = enderecoIP.getHostAddress();

                index = h.obterInfoFicheiros(gama_palavra, ip, porto);
                urlHashmap = h.obterURLFicheiros(gama_palavra, ip, porto);
                lerFichClientes(h, gama_palavra, ip, porto);

            } catch (RemoteException | java.rmi.NotBoundException e) {
                System.out.println("Interrupted");
            } catch (UnknownHostException e) {
                System.out.println("UnknownHostException");
            }
        } else if (type_t == 2) {
            final String MULTICAST_ADDRESS_2 = "224.3.2.2";
            final int PORT_2 = 4322;
            MulticastSocket socket2 = null;
            while (true) {
                try {
                    socket2 = new MulticastSocket();
                    InetAddress enderecoIP = InetAddress.getLocalHost();
                    String di = "20|" + enderecoIP.getHostAddress() + "|" + porto + "|" + gama_palavra;
                    byte[] buffer2 = di.getBytes();

                    InetAddress group2 = InetAddress.getByName(MULTICAST_ADDRESS_2);
                    DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length, group2, PORT_2);

                    socket2.send(packet2);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    socket2.close();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public HashMap<String, HashSet<String[]>> obterIndex() throws RemoteException {
        return lerFichObjetos();
    }

    public HashMap<String, HashSet<String>> obterURLMap() throws RemoteException {
        return lerFichObjetosHashmap();
    }

    public HashMap<String, Integer> obterPesquisas() throws RemoteException {
        mapaPesquisas = lerMapaPesquisas();
        return mapaPesquisas;
    }

    public void escreverMapaPesquisas(HashMap<String, Integer> mapaPesquisas) {
        File fich = new File("MapaPesquisas");
        try {
            FileOutputStream iOS = new FileOutputStream(fich);
            ObjectOutputStream oOS = new ObjectOutputStream(iOS);

            oOS.writeObject(mapaPesquisas);
            oOS.close();
        } catch (IOException e) {
            System.out.println("ERRO " + e);
        }

    }

    public HashMap<String, Integer> lerMapaPesquisas() {
        HashMap<String, Integer> mapa = null;
        File fich = new File("MapaPesquisas");

        if (fich.exists()) {
            try {
                FileInputStream fIS = new FileInputStream(fich);
                ObjectInputStream oIS = new ObjectInputStream(fIS);
                mapa = (HashMap<String, Integer>) oIS.readObject();
                oIS.close();


            } catch (EOFException e) {
                System.out.print("");
            } catch (ClassNotFoundException | IOException e) {
                System.out.println("ERRO " + e);
            }
        } else {
            System.out.println("Ficheiro de Objetos não existe...");
        }
        return mapa;

    }

    public void escreverFichClientes() {
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

    public void lerFichClientes(SearchModule_I s, String gama_palavra, String ip, String porto) {
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
            try {
                System.out.println("[LEITURA FICHEIRO] Ficheiro de Objetos de Clientes nao existe...");
                clientes = s.obterClientes(gama_palavra, ip, porto);
            } catch (RemoteException e) {
                System.out.println("Interrupted Remote: " + e);
            }


        }
    }

    public ArrayList<ClienteInfo> obterClientesBarrel() throws RemoteException {
        try {
            InetAddress enderecoIP = InetAddress.getLocalHost();
            String ip = enderecoIP.getHostAddress();
            SearchModule_I h = (SearchModule_I) LocateRegistry.getRegistry(1100).lookup("Search_Module");

            lerFichClientes(h, gama_palavra, ip, porto);
        } catch (UnknownHostException | NotBoundException e) {
            e.printStackTrace();
        }

        // não era suposto ser preciso fazer a ligação, mas só funciona assim
        return clientes;
    }

    public void adicionarCliente(ClienteInfo cliente) throws RemoteException {
        if (this.clientes == null) {
            clientes = new ArrayList<>();
        }
        this.clientes.add(cliente);
        escreverFichClientes();
    }


    public void escreverFichObjetosHashmap(HashMap<String, HashSet<String>> urlsHash) {

        try {
            FileOutputStream iOS = new FileOutputStream(fClientesObjHashUrl);
            ObjectOutputStream oOS = new ObjectOutputStream(iOS);

            oOS.writeObject(urlsHash);
            oOS.close();
        } catch (IOException e) {
            System.out.println("ERRO " + e);
        }
    }

    public HashMap<String, HashSet<String>> lerFichObjetosHashmap() {
        HashMap<String, HashSet<String>> urlsHash = null;
        if (fClientesObjHashUrl.exists()) {
            try {
                FileInputStream fIS = new FileInputStream(fClientesObjHashUrl);
                ObjectInputStream oIS = new ObjectInputStream(fIS);
                urlsHash = (HashMap<String, HashSet<String>>) oIS.readObject();
                oIS.close();


            } catch (EOFException e) {
                System.out.print("");
            } catch (ClassNotFoundException | IOException e) {
                System.out.println("ERRO " + e);
            }
        } else {
            System.out.println("Ficheiro de Objetos não existe...");
        }
        return urlsHash;
    }

    public void escreverFichObjetos(String mensagem) {
        String[] lista = mensagem.split("\\|");
        String titulo = lista[3];
        String url = lista[2];
        String citacao;
        if (lista.length == 5) {
            citacao = lista[4];
        } else {
            citacao = " ";
        }


        try {
            FileOutputStream iOS = new FileOutputStream(fClientesObj);
            ObjectOutputStream oOS = new ObjectOutputStream(iOS);

            // Cria um array de valores
            String[] valores = {url, titulo, citacao};

            String palavra = lista[1];
            String p_ascii = Normalizer.normalize(palavra, Normalizer.Form.NFD);
            if (!palavra.equals("") && !stopwords.contains(palavra) && Character.toLowerCase(p_ascii.charAt(0)) >= Character.toLowerCase(gama_palavra.charAt(1)) && Character.toLowerCase(p_ascii.charAt(0)) <= Character.toLowerCase(gama_palavra.charAt(3))) {
                if (!index.containsKey(palavra)) {
                    // Se não existir, cria um conjunto de valores
                    HashSet<String[]> values = new HashSet<>();
                    values.add(valores);
                    index.put(palavra, values);
                } else {
                    HashSet<String[]> v = index.get(palavra);
                    int flag = 0;
                    for (String[] va : v) {
                        if (va[0].equals(valores[0])) {
                            flag = 1;
                            break;
                        }
                    }
                    if (flag == 0) {
                        index.get(palavra).add(valores);
                    }
                }
            }
            /*
            for (String key : index.keySet()) {
                System.out.println("Key: " + key);

                // Access the values associated with the current key
                HashSet<String[]> values = index.get(key);
                System.out.print("Values:[");
                for (String[] val : values) {
                    System.out.print("[");
                    for (String v : val) {
                        System.out.print(v + " ,");
                    }
                    System.out.println("]");
                }
            }

            */
            oOS.writeObject(index);
            oOS.close();
        } catch (IOException e) {
            System.out.println("ERRO " + e);
        }
    }

    public HashMap<String, HashSet<String[]>> lerFichObjetos() {
        HashMap<String, HashSet<String[]>> palavras = null;
        if (fClientesObj.exists()) {
            try {
                FileInputStream fIS = new FileInputStream(fClientesObj);
                ObjectInputStream oIS = new ObjectInputStream(fIS);
                Object obj = oIS.readObject();
                if (obj instanceof HashMap) {
                    palavras = (HashMap<String, HashSet<String[]>>) obj;
                } else {
                    System.out.println("Error: Invalid object or null returned from input stream.");
                }
                //System.out.println(palavras);
                oIS.close();
                /*
                for (String key : palavras.keySet()) {
                    System.out.println("Key: " + key);

                    // Access the values associated with the current key
                    HashSet<String[]> values = palavras.get(key);
                    System.out.print("Values:[");
                    for (String[] val : values) {
                        System.out.print("[");
                        for (String v : val) {
                            System.out.print(v + " ,");
                        }
                        System.out.println("]");
                    }
                }

                 */

            } catch (EOFException e) {
                System.out.print("");
            } catch (ClassNotFoundException | IOException e) {
                System.out.println("ERRO " + e);
            }
        } else {
            System.out.println("Ficheiro de Objetos não existe...");
        }
        return palavras;
    }

    public void adicionarPesquisa(String pesquisa) throws RemoteException {
        mapaPesquisas = lerMapaPesquisas();
        if (mapaPesquisas != null) {
            if (mapaPesquisas.containsKey(pesquisa)) {
                mapaPesquisas.put(pesquisa, mapaPesquisas.get(pesquisa) + 1);
            } else {
                mapaPesquisas.put(pesquisa, 1);
            }
        } else {
            mapaPesquisas = new HashMap<>();
            mapaPesquisas.put(pesquisa, 1);
        }

        System.out.println(mapaPesquisas);
        escreverMapaPesquisas(mapaPesquisas);
    }

    public HashSet<String[]> obterInfoBarrel(String palavra) throws RemoteException {
        index = lerFichObjetos();
        return index.get(palavra);
    }

    public HashSet<String> obterLinks(String url) throws RemoteException {
        urlHashmap = lerFichObjetosHashmap();
        return urlHashmap.get(url);
    }


    public void LerArquivoTexto() {
        String nomeArquivo = "stopwords.txt";

        try (BufferedReader leitor = new BufferedReader(new FileReader(nomeArquivo))) {
            String linha;
            while ((linha = leitor.readLine()) != null) {
                linha = linha.replace(" ", "");
                stopwords.add(new String(linha.getBytes(), StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Erro: Executar --> StorageBarrel <NOME DO FICHEIRO PALAVRAS> <NOME DO FICHEIRO URL> <GAMA DA PALAVRAS [a-z]> <PORTO>");
            return;
        }

        StorageBarrel s1 = new StorageBarrel(args[0], args[1], 0, args[2], args[3]);
        StorageBarrel s2 = new StorageBarrel(args[0], args[1], 1, args[2], args[3]);//"fich_url1",0,"[a-z]","1000"
        StorageBarrel s3 = new StorageBarrel(args[0], args[1], 2, args[2], args[3]);
        try {
            Registry r = LocateRegistry.createRegistry(Integer.parseInt(args[3]));
            r.rebind("Storage_Barrel", s2);
            s1.t.join();
            s2.t.join();
            s3.t.join();

        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        } catch (RemoteException e) {
            System.out.println("error: " + e);
        }
    }
}
