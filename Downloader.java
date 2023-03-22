import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.rmi.registry.LocateRegistry;
import java.io.IOException;
import java.util.StringTokenizer;

public class Downloader implements Runnable{
    Thread t;
    public Downloader() {
        t = new Thread(this);
        t.start();
    }

    public void run() {
        try {
            FilaURL_I h = (FilaURL_I) LocateRegistry.getRegistry(1099).lookup("fila_url");
            while(true){
                String url = h.sendUrl();
                if (url != null) {
                    System.out.println(url);
                    Document doc = Jsoup.connect(url).get();
                    StringTokenizer tokens = new StringTokenizer(doc.text());
                    int countTokens = 0;
                    while (tokens.hasMoreElements() && countTokens++ < 100)
                        System.out.println(tokens.nextToken().toLowerCase());
                    Elements links = doc.select("a[href]");
                    for (Element link : links) {
                        //System.out.println( link.attr("abs:href") );
                        h.recUrl(link.attr("abs:href"));
                    }
                }

            }
        } catch (Exception e ) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        Downloader d1 = new Downloader();
        Downloader d2 = new Downloader();
        try {
            d1.t.join();
            d2.t.join();

        } catch(InterruptedException e) {
            System.out.println("Interrupted");
        }
    }

}
