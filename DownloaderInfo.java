import java.io.Serializable;

public class DownloaderInfo implements Serializable {
    private String id, porto;

    public DownloaderInfo(){}

    public DownloaderInfo(String id, String porto){
        this.id = id; this.porto = porto;
    }

    public String toString() {
        return "Downloader:" + id + " --> Porto: "+ porto;
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
}

