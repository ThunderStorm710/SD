import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Downloader implements Runnable{
    private final String MULTICAST_ADDRESS = "224.3.2.1";
    private final int PORT = 4321;

    Thread t;
    public Downloader() {
        t = new Thread(this);
        t.start();
    }

    public void run() {
        MulticastSocket socket = null;
        try {
            FilaURL_I h = (FilaURL_I) LocateRegistry.getRegistry(1099).lookup("fila_url");
            socket = new MulticastSocket();
            while(true){

                String url = h.sendUrl();
                URL url_test = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) url_test.openConnection();
                connection.setRequestMethod("HEAD");
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    System.out.println("O URL não pode ser alcançado. Código de resposta HTTP: " + responseCode);

                }
                else {

                    ArrayList<String> lista = new ArrayList<>();

                    if (url != null) {
                        System.out.println(url);
                        Document doc = Jsoup.connect(url).ignoreHttpErrors(true).get();
                        String title = doc.title();

                        Element firstParagraph = doc.selectFirst("p:first-of-type");

                        lista.add(url);
                        lista.add(title);

                        if (firstParagraph != null){
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


                        Elements links = doc.select("a[href]");
                        for (Element link : links) {
                            //System.out.println( link.attr("abs:href") );
                            h.recUrl(link.attr("abs:href"));
                        }
                    }
                }

            }
        } catch (IOException | NotBoundException e ) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

    public static void main(String args[]) {
        Downloader d1 = new Downloader();
        //Downloader d2 = new Downloader();
        try {
            d1.t.join();
            //d2.t.join();

        } catch(InterruptedException e) {
            System.out.println("Interrupted");
        }
    }

}