import java.util.ArrayList;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;



public class FilaURL extends UnicastRemoteObject implements FilaURL_I{


    private ArrayList<String> urlList;
    public FilaURL() throws RemoteException {
        super();
        this.urlList = new ArrayList<String>();
    }

    public void setList(ArrayList<String> urlList) {
        this.urlList = urlList;
    }

    public ArrayList<String> getList(){
        return this.urlList;
    }

    public void meteNaLista(String nome){
        this.urlList.add(nome);
    }

    synchronized public String sendUrl() throws RemoteException {
        if (!urlList.isEmpty()) {
            String url = urlList.get(0);
            urlList.remove(0);
            return url;
        }
        return null;
    }

    synchronized public void recUrl(String url) throws RemoteException{
        urlList.add(url);
        System.out.println(urlList.get(0));
        System.out.println(url);
    }
    public static void main(String[] args) {

        //fila.setList(fila.getList());
        try{
            FilaURL fila = new FilaURL();
            Registry r = LocateRegistry.createRegistry(1099);
            r.rebind("fila_url", fila);
            fila.meteNaLista("https://www.uc.pt");
            System.out.println(fila);


        } catch(Exception e){
            System.out.println("Error" + e);
        }
    }


}
