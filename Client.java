import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.HashSet;
import java.util.Scanner;

public class Client implements Runnable {
    Thread t;

    public Client() {
        t = new Thread(this);
        t.start();
    }

    public void run() {
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ClienteInfo login(SearchModule_I h) {
        boolean flag = false;
        ClienteInfo c1 = null;
        Scanner sc = new Scanner(System.in);
        String username, password;
        int contador = 0;
        try {
            while (!flag) {
                System.out.print("Insira o seu username: ");
                username = sc.nextLine();
                System.out.print("Insira a sua password: ");
                password = sc.nextLine();
                c1 = h.verificarLogin(username, password);

                if (contador == 3) {
                    System.out.println("Numero tentativas excedidas...");
                    return null;
                }
                if (c1 == null) {
                    contador++;
                    System.out.println("Nome ou identificacao não encontrados na base de dados...por favor volte a inserir as suas credenciais...");

                } else {
                    flag = true;
                    System.out.println("Inscricaoo validada com sucesso!");
                    System.out.println("Seja bem-vindo, " + c1.getNome() + "!!\n");
                }
            }
        } catch (RemoteException e) {
            System.out.println("Erro " + e);
        }
        return c1;
    }


    public static ClienteInfo registar(SearchModule_I h) {
        boolean flag = false;
        String nome, email, password, username;
        Scanner sc = new Scanner(System.in);
        ClienteInfo c1 = null;

        try {
            while (!flag) {
                System.out.print("Insira o seu nome: ");
                nome = sc.nextLine();
                System.out.print("Insira o seu username: ");
                username = sc.nextLine();
                System.out.print("Insira o seu email: ");
                email = sc.nextLine();
                System.out.print("Insira a sua password: ");
                password = sc.nextLine();


                c1 = h.verificarRegisto(nome, email, username, password);
                if (c1 == null) {
                    System.out.println("Nome, username ou email  ja se encontram associados a um utilizador ja existente na base de dados...por favor volte a inserir as suas credencias...");
                } else {
                    flag = true;
                    System.out.println("Inscricao validada com sucesso!");
                    System.out.println("Seja bem-vindo, " + nome + "!!\n");
                }
            }

        } catch (RemoteException e) {
            System.out.println("Erro " + e);
        }
        return c1;
    }


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String opcao, linha;
        boolean entrada = false;
        ClienteInfo cliente = null;
        try {
            SearchModule_I h = (SearchModule_I) LocateRegistry.getRegistry(1100).lookup("Search_Module");

            while (!entrada) {
                System.out.println("""
                        --- BEM-VINDO ---
                        1 - Registar
                        2 - Login
                        3 - Sair
                        """);
                opcao = sc.nextLine();
                if (opcao.length() != 1) {
                    System.out.println("Opcao invalida...");
                } else if (opcao.equals("1")) {
                    if ((cliente = registar(h)) != null) {
                        entrada = true;
                    }

                } else if (opcao.equals("2")) {
                    if ((cliente = login(h)) != null) {
                        entrada = true;
                    }
                } else if (opcao.equals("3")) {
                    break;

                } else {
                    System.out.println("Opcao invalida...");
                }

            }
            if (entrada){
                while (true) {
                    System.out.println("""
                        --- BEM-VINDO ---
                        Que operacao deseja realizar?
                        1 - Indexar novo URL
                        2 - Pesquisar
                        3 - Sair
                        """);
                    opcao = sc.nextLine();
                    if (opcao.length() != 1) {
                        System.out.println("Opcao invalida...");
                    } else if (opcao.equals("1")) {
                        System.out.print("Insira um URL: ");
                        opcao = sc.nextLine();
                        System.out.println("Vou indexar!");
                        h.indexarURL(cliente, opcao);

                    } else if (opcao.equals("2")) {
                        System.out.print("Pesquisa: ");
                        linha = sc.nextLine();
                        System.out.println("Linha lida --> " + linha);
                        HashSet<String[]> paginas = h.pesquisarPaginas(cliente, linha);
                        if (paginas != null){
                            for (String[] pagina : paginas) {
                                System.out.println(pagina[0] + " ---> " + pagina[1]);

                            }
                        }

                    } else if (opcao.equals("3")) {
                        break;

                    } else {
                        System.out.println("Opcao invalida...");
                    }

                }
            }


        } catch (java.rmi.RemoteException | java.rmi.NotBoundException e) {
            System.out.println("RemoteException");
        }
    }
}
