import java.util.ArrayList;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;


public class FilaURL extends UnicastRemoteObject implements FilaURL_I {

    private ArrayList<String> urlList;
    private ArrayList<String> urlVisitados;

    public FilaURL() throws RemoteException {
        super();
        this.urlList = new ArrayList<>();
        this.urlVisitados = new ArrayList<>();
    }

    public void setList(ArrayList<String> urlList) {
        this.urlList = urlList;
    }

    public ArrayList<String> getList() {
        return this.urlList;
    }

    public void meteNaLista(String nome) {
        this.urlList.add(nome);
    }

    synchronized public String sendUrl() throws RemoteException {
        if (!urlList.isEmpty()) {
            String url = urlList.get(0);
            urlVisitados.add(urlList.get(0));
            urlList.remove(0);
            return url;
        }
        return null;
    }

    synchronized public void adicionarAForca(String url) throws RemoteException{
        if (urlList != null){
            urlList.add(url);
        }
    }

    synchronized public boolean recUrl(String url) throws RemoteException {
        boolean flag = true;
        for (String s : urlVisitados) {
            if (s.equals(url)) {
                flag = false;
                break;
            }
        }
        if (flag) {
            urlList.add(url);
            System.out.println(urlList.get(0));
            System.out.println(url);
        }
        return flag;
    }

    public static void main(String[] args) {

        //fila.setList(fila.getList());
        try {
            FilaURL fila = new FilaURL();
            Registry r = LocateRegistry.createRegistry(1099);
            r.rebind("fila_url", fila);
            fila.meteNaLista("https://www.uc.pt");
            System.out.println(fila);


        } catch (Exception e) {
            System.out.println("Error" + e);
        }
    }


}
