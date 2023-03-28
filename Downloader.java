import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.*;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.util.*;

public class Downloader implements Runnable {

    HashMap<String, HashSet<String>> urlsLig;
    private static final int MAX_CHUNK_SIZE = 1024;
    private final String MULTICAST_ADDRESS = "224.3.2.1";
    private final int PORT = 4321;
    private final String MULTICAST_ADDRESS_2 = "224.3.2.2";
    private final int PORT_2 = 4322;

    Thread t;
    int type_t;
    String id;

    public Downloader(int type_t, String id) {
        this.urlsLig = new HashMap<>();
        t = new Thread(this);
        t.start();
        this.type_t = type_t;
        this.id = id;
    }

    public void run() {
        if (type_t == 1) {

            MulticastSocket socket = null;
            try {
                FilaURL_I h = (FilaURL_I) LocateRegistry.getRegistry(1099).lookup("fila_url");
                socket = new MulticastSocket();
                while (true) {

                    String url = h.sendUrl();
                    URL url_test = new URL(url);

                    HttpURLConnection connection = (HttpURLConnection) url_test.openConnection();
                    connection.setRequestMethod("HEAD");
                    int responseCode = connection.getResponseCode();
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        System.out.println("O URL não pode ser alcançado. Código de resposta HTTP: " + responseCode);

                    } else {

                        ArrayList<String> lista = new ArrayList<>();

                        if (url != null) {
                            System.out.println(url);

                            Document doc = Jsoup.connect(url).ignoreHttpErrors(true).get();
                            String title = doc.title();

                            Element firstParagraph = doc.selectFirst("p:first-of-type");

                            lista.add(url);
                            lista.add(title);

                            if (firstParagraph != null) {
                                lista.add(firstParagraph.text());
                            }


                            StringTokenizer tokens = new StringTokenizer(doc.text());
                            int countTokens = 0;
                            while (tokens.hasMoreElements() && countTokens++ < 1000) {
                                //System.out.println(tokens.nextToken().toLowerCase());
                                String tok = tokens.nextToken().toLowerCase().replaceAll("[,.\\[\\]{}!?:;()<>+*/%]", "");
                                lista.add(tok);
                            }
                            System.out.println("++++++++++++");
                            for (String numero : lista) {
                                System.out.println(numero);
                            }

                            System.out.println("++++++++++++");
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ObjectOutputStream oos = new ObjectOutputStream(baos);
                            oos.writeObject(lista);
                            byte[] serializedData = baos.toByteArray();

                            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                            DatagramPacket packet = new DatagramPacket(serializedData, serializedData.length, group, PORT);

                            socket.send(packet);


                            if (urlsLig != null) {
                                try {
                                    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                                    ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
                                    objectStream.writeObject(urlsLig);
                                    byte[] data = byteStream.toByteArray();

                                    int offset = 0;
                                    while (offset < data.length) {
                                        int chunkSize = Math.min(MAX_CHUNK_SIZE, data.length - offset);
                                        byte[] chunkData = new byte[chunkSize];
                                        System.arraycopy(data, offset, chunkData, 0, chunkSize);

                                        DatagramPacket packet2 = new DatagramPacket(chunkData, chunkSize, group, PORT);

                                        socket.send(packet2);

                                        offset += chunkSize;
                                    }
                                } catch (UTFDataFormatException e) {
                                    System.out.println("String em codificacao invalida...");
                                } catch (StreamCorruptedException e) {
                                    System.out.println("Dados de objeto inválidos...");
                                } catch (IOException | ClassCastException e) {
                                    System.out.println("Erro: " + e);
                                }
                            }


                            Elements links = doc.select("a[href]");
                            for (Element link : links) {
                                if (!urlsLig.containsKey(link.attr("abs:href"))) {
                                    HashSet<String> values = new HashSet<>();
                                    values.add(url);
                                    urlsLig.put(link.attr("abs:href"), values);
                                } else {
                                    HashSet<String> values = urlsLig.get(link.attr("abs:href"));
                                    if (values != null) {
                                        values.add(url);
                                        urlsLig.put(link.attr("abs:href"), values);
                                    }
                                }
                                // PRINT DO HASHMAP
                                //System.out.println("LLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL");
                                //for (Map.Entry<String, HashSet<String>> entry : urlsLig.entrySet()) {
                                //    String key = entry.getKey();
                                //    HashSet<String> values = entry.getValue();
                                //    System.out.println("Chave: " + key);
                                //    System.out.println("Valores: " + values);
                                //}
                                //System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
                                //System.out.println( link.attr("abs:href") );
                                h.recUrl(link.attr("abs:href"));

                            }
                        }
                    }

                }
            } catch (IOException | NotBoundException e) {
                e.printStackTrace();
            } finally {
                socket.close();
            }
        }
        if (type_t == 2) {
            MulticastSocket socket2 = null;
            while (true) {
                try {
                    String di = "1|" + id + "|" + PORT_2;

                    byte[] buffer2 = di.getBytes();
                    socket2 = new MulticastSocket(PORT);
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

    public static void main(String[] args) {
        Downloader d1 = new Downloader(1, args[0]);
        Downloader d2 = new Downloader(2, args[0]);
        try {
            d1.t.join();
            d2.t.join();

        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        }
    }

}