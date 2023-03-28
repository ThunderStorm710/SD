import java.io.Serializable;

public class Storage implements Serializable {
    private String gama, porto, ip;

    public Storage(){}

    public Storage(String gama, String ip, String porto){
        this.gama = gama;
        this.ip = ip;
        this.porto = porto;
    }

    public String toString() {
        return "Storage:" + " --> Gama: " + gama + " --> IP: " + ip + " --> Porto: " + porto;
    }

    public String getGama() {
        return gama;
    }

    public void setGama(String gama) {
        this.gama = gama;
    }

    public String getPorto() {
        return porto;
    }

    public void setPorto(String porto) {
        this.porto = porto;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
