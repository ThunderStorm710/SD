import java.io.Serializable;

public class Storage implements Serializable {
    private String gama, porto;

    public Storage(){}

    public Storage(String gama, String porto){
        this.gama = gama;
        this.porto = porto;
    }

    public String toString() {
        return "Storage:" + " --> Gama: " + gama;
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
}
