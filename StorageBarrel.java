import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.rmi.registry.LocateRegistry;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.rmi.RemoteException;

public class StorageBarrel implements Runnable {
    File fClientesObj;
    HashMap<String, HashSet<String[]>> index;
    ArrayList<String> stopwords;
    int type_t;
    String porto;
    String gama_palavra;
    Thread t;

    public StorageBarrel(String nome_fich, int type_t, String gama_palavra, String porto) {
        t = new Thread(this);
        this.index = new HashMap<>();
        this.stopwords = new ArrayList<>();
        this.fClientesObj = new File(nome_fich);
        this.type_t = type_t;
        this.porto = porto;
        this.gama_palavra = gama_palavra;
        t.start();
    }

    public void run() {
        LerArquivoTexto();
        if(type_t == 0) {
            MulticastSocket socket = null;
            String MULTICAST_ADDRESS = "224.3.2.1";
            int PORT = 4321;
            try {
                socket = new MulticastSocket(PORT);  // create socket and bind it
                InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                socket.joinGroup(group);
                while (true) {
                    byte[] buffer = new byte[5000];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    System.out.println("Received packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " with message:");
                    ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData());
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    ArrayList<String> receivedList = (ArrayList<String>) ois.readObject();
                    for (String cona : receivedList) {
                        System.out.println(cona);
                    }
                    escreverFichObjetos(receivedList);
                    lerFichObjetos();

                }
            } catch (
                    IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                socket.close();
            }
        }
        else if(type_t == 1){
            try {
                SearchModule_I h = (SearchModule_I) LocateRegistry.getRegistry(1100).lookup("Search_Module");
                if (!h.adicionarInfoInicialBarrel(gama_palavra, porto)){
                    return;
                }
            }catch ( RemoteException | java.rmi.NotBoundException e) {
                System.out.println("Interrupted");
            }
        }
    }

    public void escreverFichObjetos(ArrayList<String> receivedList) {
        String titulo = receivedList.get(1);
        String url = receivedList.get(0);
        String citacao = receivedList.get(2);
        receivedList.remove(0);
        receivedList.remove(1);
        receivedList.remove(2);
        try {
            FileOutputStream iOS = new FileOutputStream(fClientesObj);
            ObjectOutputStream oOS = new ObjectOutputStream(iOS);
            for (String palavra : receivedList) {
                // Cria um array de valores
                String[] valores = {url, titulo, citacao};
                String p_ascii = Normalizer.normalize(palavra, Normalizer.Form.NFD);

                if(!palavra.equals("") && !stopwords.contains(palavra) && Character.toLowerCase(p_ascii.charAt(0)) >= Character.toLowerCase(gama_palavra.charAt(1)) && Character.toLowerCase(p_ascii.charAt(0)) <= Character.toLowerCase(gama_palavra.charAt(3)) ) {
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

    public void lerFichObjetos() {

        if (fClientesObj.exists()) {
            HashMap<String, HashSet<String[]>> palavras;
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
    }

    public HashSet<String[]> obterInfoBarrel(String palavra) throws RemoteException {
        return index.get(palavra);
    }


    public  void LerArquivoTexto() {
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
        if (args.length != 3) {
            System.out.println("StorageBarrel <NOME DO FICHEIRO> <GAMA DA PALAVRAS [a-z]> <PORTO>");
            return;
        }
        StorageBarrel s1 = new StorageBarrel(args[0],0,args[1],args[2] );
        //StorageBarrel s2 = new StorageBarrel("fich_url1",0,"[a-z]","1000");
        try {
            s1.t.join();
            //s2.t.join();

        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        }
    }
}
