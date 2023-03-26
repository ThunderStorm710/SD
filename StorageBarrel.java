import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.rmi.AccessException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.rmi.RemoteException;
import java.util.Map;

public class StorageBarrel implements Runnable, StorageBarrel_I, Serializable {
    File fClientesObj;
    File fClientesObjHashUrl;
    HashMap<String, HashSet<String[]>> index;
    HashMap<String, HashSet<String>> urlHashmap;
    private static final int MAX_PACKET_SIZE = 1024;
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
                    byte[] buffer = new byte[50000];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    System.out.println("Received packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " with message:");
                    ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData());
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    ArrayList<String> receivedList = (ArrayList<String>) ois.readObject();


                    /*
                    byte[] buffer2 = new byte[50000];
                    DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length);
                    socket.receive(packet2);

                    System.out.println("Received packet from " + packet2.getAddress().getHostAddress() + ":" + packet2.getPort() + " with message:");
                    ByteArrayInputStream bais2 = new ByteArrayInputStream(packet2.getData());
                    ObjectInputStream ois2 = new ObjectInputStream(bais2);

                    HashMap<String, HashSet<String>> urlsLigacoes = (HashMap<String, HashSet<String>>) ois2.readObject();

                     */


                    byte[] data = new byte[0];
                    while (true) {
                        byte[] buffer2 = new byte[MAX_PACKET_SIZE];
                        DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length);
                        socket.receive(packet2);

                        byte[] receivedData = packet2.getData();
                        int receivedSize = packet2.getLength();
                        byte[] newData = new byte[data.length + receivedSize];
                        System.arraycopy(data, 0, newData, 0, data.length);
                        System.arraycopy(receivedData, 0, newData, data.length, receivedSize);
                        data = newData;

                        if (receivedSize < MAX_PACKET_SIZE) {
                            break;
                        }
                    }

                    try {
                        ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
                        ObjectInputStream objectStream = new ObjectInputStream(byteStream);
                        HashMap<String, HashSet<String>> urlsLigacoes = (HashMap<String, HashSet<String>>) objectStream.readObject();

                        escreverFichObjetosHashmap(urlsLigacoes);
                        urlHashmap = lerFichObjetosHashmap();
                    } catch (UTFDataFormatException e) {
                        System.out.println("String em codificacao invalida...");
                    }

                    for (String l : receivedList) {
                        System.out.println(l);
                    }

                    escreverFichObjetos(receivedList);
                    index = lerFichObjetos();

                    //escreverFichObjetosHashmap(urlsLigacoes);
                    //urlHashmap = lerFichObjetosHashmap();


                    if (urlHashmap != null) {
                        System.out.println("LLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL");
                        for (Map.Entry<String, HashSet<String>> entry : urlHashmap.entrySet()) {
                            String key = entry.getKey();
                            HashSet<String> values = entry.getValue();
                            System.out.println("Chave: " + key);
                            System.out.println("Valores: " + values);
                        }
                        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
                    }






                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                socket.close();
            }
        } else if (type_t == 1) {

            try {
                SearchModule_I h = (SearchModule_I) LocateRegistry.getRegistry(1100).lookup("Search_Module");
                if (!h.adicionarInfoInicialBarrel(gama_palavra, porto)) {
                    System.out.println("Nao foi possivel ligar ao Search Module...");
                    return;
                }
            } catch (RemoteException | java.rmi.NotBoundException e) {
                System.out.println("Interrupted");
            }
        }
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

    public void escreverFichObjetos(ArrayList<String> receivedList) {
        String titulo = receivedList.get(1);
        String url = receivedList.get(0);
        String citacao = receivedList.get(2);
        receivedList.remove(2);
        receivedList.remove(1);
        receivedList.remove(0);
        try {
            FileOutputStream iOS = new FileOutputStream(fClientesObj);
            ObjectOutputStream oOS = new ObjectOutputStream(iOS);
            for (String palavra : receivedList) {
                // Cria um array de valores
                String[] valores = {url, titulo, citacao};
                String p_ascii = Normalizer.normalize(palavra, Normalizer.Form.NFD);

                if (!palavra.equals("") && !stopwords.contains(palavra) && Character.toLowerCase(p_ascii.charAt(0)) >= Character.toLowerCase(gama_palavra.charAt(1)) && Character.toLowerCase(p_ascii.charAt(0)) <= Character.toLowerCase(gama_palavra.charAt(3))) {
                    if (!index.containsKey(palavra)) {
                        // Se não existir, cria um novo conjunto de valores
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
            }
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
                palavras = (HashMap<String, HashSet<String[]>>) oIS.readObject();
                oIS.close();
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




    public HashSet<String[]> obterInfoBarrel(String palavra) throws RemoteException {
        index = lerFichObjetos();
        return index.get(palavra);
    }

    public HashSet<String> obterLinks(String url) throws RemoteException{
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
            System.out.println("StorageBarrel <NOME DO FICHEIRO> <GAMA DA PALAVRAS [a-z]> <PORTO>");
            return;
        }
        System.out.println("ARGS" + args[0] + " --- " + args[3]);

        StorageBarrel s1 = new StorageBarrel(args[0], args[1], 0, args[2], args[3]);
        StorageBarrel s2 = new StorageBarrel(args[0], args[1],1, args[2], args[3]);//"fich_url1",0,"[a-z]","1000"
        try {
            Registry r = LocateRegistry.createRegistry(Integer.parseInt(args[3]));
            r.rebind("Storage_Barrel", s2);
            s1.t.join();
            s2.t.join();

        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        } catch (RemoteException e) {
            System.out.println("error: " + e);
        }
    }
}
