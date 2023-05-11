import java.io.Serializable;
import java.time.LocalTime;

public class DownloaderInfo implements Serializable {
    private String id, ip, porto;
    private LocalTime tempo;

    public DownloaderInfo(){}

    public DownloaderInfo(String id, String ip, String porto){
        this.id = id; this.porto = porto; this.ip = ip; this.tempo = LocalTime.now();
    }

    public String toString() {
        return "Downloader: " + id + " --> IP: "+ ip +" --> Porto: "+ porto;
    }

    public void setid(String id) {
        this.id = id;
    }

    public String getid() {
        return id;
    }

    public String getPorto() {
        return porto;
    }

    public void setPorto(String porto) {
        this.porto = porto;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public LocalTime getTempo() {
        return tempo;
    }

    public void setTempo(LocalTime tempo) {
        this.tempo = tempo;
    }
}

