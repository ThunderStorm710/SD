import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class StorageBarrel implements Runnable{
    File fClientesObj;
    HashMap< String, HashSet<String[]>> index ;
    Thread t;
    public StorageBarrel() {
        t = new Thread( this);
        this.index = new HashMap<>();
        this.fClientesObj = new File("fich_urls");
        t.start();
    }

    public void run() {
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
                for(String cona: receivedList){
                    System.out.println(cona);
                }
                escreverFichObjetos(receivedList);
                lerFichObjetos();
                //System.out.println(message);

            }
        } catch (
                IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

    public void escreverFichObjetos(ArrayList<String> receivedList) {
        String titulo = receivedList.get(1);
        String url = receivedList.get(0);
        receivedList.remove(0);
        receivedList.remove(1);
        try {
            FileOutputStream iOS = new FileOutputStream(fClientesObj);
            ObjectOutputStream oOS = new ObjectOutputStream(iOS);
            for (String palavra : receivedList) {
                // Cria um array de valores
                String[] valores = {url, titulo};

                if (!index.containsKey(palavra)) {
                    // Se não existir, cria um novo conjunto de valores
                    HashSet<String[]> values = new HashSet<>();
                    values.add(valores);
                    index.put(palavra, values);
                }
                else{
                    HashSet<String[]> v = index.get(palavra);
                    int flag = 0;
                    for(String[] va : v){
                        //System.out.println(va[0] + " " + va[1]);
                        if (va[0].equals(valores[0])){
                            flag = 1;
                            break;
                        }
                    }
                    if (flag == 0){
                        index.get(palavra).add(valores);
                    }
                    //System.out.println("++++");
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
            HashMap< String, HashSet<String[]>> palavras ;
            try {
                FileInputStream fIS = new FileInputStream(fClientesObj);
                ObjectInputStream oIS = new ObjectInputStream(fIS);
                palavras = (HashMap< String, HashSet<String[]>>)oIS.readObject();
                oIS.close();
                for (String key : palavras.keySet()) {
                    System.out.println("Key: " + key);

                    // Access the values associated with the current key
                    HashSet<String[]> values = palavras.get(key);
                    System.out.print("Values:[");
                    for(String[] val :values) {
                        System.out.print ("[");
                        for( String v: val) {
                            System.out.print (v+" ,");
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

    public static void main(String args[]) {
        StorageBarrel s1 = new StorageBarrel();
        //StorageBarrel s2 = new StorageBarrel();

        try {

            s1.t.join();
            //s2.t.join();

        } catch(InterruptedException e) {
            System.out.println("Interrupted");
        }
    }
}
