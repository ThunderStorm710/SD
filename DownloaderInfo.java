import java.io.Serializable;

public class DownloaderInfo implements Serializable {
    private String porto;

    public DownloaderInfo(){}

    public DownloaderInfo(String porto){
        this.porto = porto;
    }

    public String toString() {
        return "Downloader:" + porto;
    }

    public void setPorto(String porto) {
        this.porto = porto;
    }

    public String getPorto() {
        return porto;
    }
}
