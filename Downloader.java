import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.*;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

public class Downloader implements Runnable {


    private static final int MAX_CHUNK_SIZE = 1024;
    private final String MULTICAST_ADDRESS = "224.3.2.1";
    private final int PORT = 4321;
    private final String MULTICAST_ADDRESS_2 = "224.3.2.2";
    private final int PORT_2 = 4322;
    private HashMap<String, Integer> listaMensagens;
    Thread t;
    int type_t;
    String id;

    public Downloader(int type_t, String id) {
        t = new Thread(this);
        t.start();
        this.type_t = type_t;
        this.id = id;
        this.listaMensagens = new HashMap<>();
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

                            String citacao;
                            System.out.println(url);

                            Document doc = Jsoup.connect(url).ignoreHttpErrors(true).get();
                            String title = doc.title();

                            Element firstParagraph = doc.selectFirst("p:first-of-type");

                            lista.add(url);
                            lista.add(title);

                            if (firstParagraph != null) {
                                citacao = firstParagraph.text();
                                lista.add(firstParagraph.text());
                            }
                            else{
                                citacao = " ";
                                lista.add(" ");
                            }


                            StringTokenizer tokens = new StringTokenizer(doc.text());
                            int countTokens = 0;
                            while (tokens.hasMoreElements() && countTokens++ < 1000) {
                                //System.out.println(tokens.nextToken().toLowerCase());
                                String tok = tokens.nextToken().toLowerCase().replaceAll("[,.\\[\\]{}!?:;()<>+*/%]", "");
                                lista.add(tok);
                            }
                            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                            for(int i = 3; i<lista.size();i++) {
                                try {
                                    String frase = "1|" + lista.get(i) + "|" + url + "|" + title + "|" + citacao;
                                    listaMensagens.put(frase, LocalTime.now().getSecond());
                                    byte[] buffer = frase.getBytes();
                                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, PORT);
                                    socket.send(packet);
                                }catch (UTFDataFormatException e) {
                                    System.out.println("String em codificacao invalida...");
                                } catch (StreamCorruptedException e) {
                                    System.out.println("Dados de objeto inválidos...");
                                } catch (IOException e) {
                                    System.out.println("Erro: " + e);
                                }
                            }



                            Elements links = doc.select("a[href]");
                            for (Element link : links) {
                                String frase2 = "2|" + link.attr("abs:href") + "|" + url;
                                listaMensagens.put(frase2, LocalTime.now().getSecond());
                                byte[] buffer2 = frase2.getBytes();
                                DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length, group, PORT);
                                socket.send(packet2);


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
                    InetAddress enderecoIP = InetAddress.getLocalHost();
                    String ip = enderecoIP.getHostAddress();
                    String di = "1|" + id + "|" + ip + "|" + PORT_2;

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
    public void repetirMulticast() {
        Runnable runnable = this::repetirMulticastFuncao;
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void repetirMulticastFuncao() {
        int i;
        Duration diff;
        try {
            while (true) {
                synchronized (listaMensagens) {
                    i = 0;

                    Thread.sleep(3000);
                }

            }

        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        }
    }

    public void ouvirACK() {
        Runnable runnable = this::ouvirACKFuncao;
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void ouvirACKFuncao() {
        int i;
        Duration diff;
        try {
            while (true) {
                synchronized (listaMensagens) {
                    for (String mensagem: listaMensagens.keySet()) {
                        if (listaMensagens.get(mensagem) - LocalTime.now().getSecond() > 5){

                        }
                    }

                    Thread.sleep(3000);
                }

            }

        } catch (InterruptedException e) {
            System.out.println("Interrupted");
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