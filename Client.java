import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.*;

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
            if (entrada) {
                while (true) {
                    System.out.println("""
                            --- BEM-VINDO ---
                            Que operacao deseja realizar?
                            1 - Indexar novo URL
                            2 - Pesquisar
                            3 - Consultar lista de paginas
                            4 - Sair
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
                        HashSet<String[]> paginas = h.pesquisarPaginas(cliente, linha);
                        if (paginas != null) {
                            System.out.println("------ Resultados da pesquisa ------");

                            if (paginas.size() > 10) {
                                int i, contador;
                                ArrayList<String[]> lista = new ArrayList<>(paginas);
                                for (i = 0; i < lista.size() && i < 10; i++) {
                                    System.out.println(Arrays.toString(lista.get(i)));
                                }
                                opcao = "";
                                label:
                                while (true) {
                                    System.out.println("""
                                            1 - Anterior
                                            2 - Proximo
                                            3 - Sair
                                            """);
                                    opcao = sc.nextLine();

                                    switch (opcao) {
                                        case "1":
                                            if (i > 10) {
                                                contador = i;
                                                for (; i > 0 && contador - i < 10; i--) {
                                                    System.out.println(Arrays.toString(lista.get(i)));
                                                }
                                            }
                                            break;
                                        case "2":
                                            contador = i;
                                            for (; i < lista.size() && i - contador < 10; i++) {
                                                System.out.println(Arrays.toString(lista.get(i)));
                                            }
                                            break;
                                        case "3":
                                            break label;
                                        default:
                                            System.out.println("Opcao invalida...");
                                            break;
                                    }
                                }
                            }
                            for (String[] pagina : paginas) {
                                System.out.println(Arrays.toString(pagina));
                            }
                        } else {
                            System.out.println("Pedimos desculpa mas nao foram encontradas paginas relevantes");
                        }

                    } else if (opcao.equals("3")) {

                        System.out.print("Insira um link url: ");
                        linha = sc.nextLine();
                        ArrayList<HashSet<String>> lista = h.obterLinks(cliente, linha);
                        if (lista.size() != 0) {
                            for (HashSet<String> s : lista) {
                                System.out.println(s);

                            }
                        } else {
                            System.out.println("Link não encontrado...");
                        }

                    } else if (opcao.equals("4")) {
                        System.out.println("--- Informacoes gerais do sistema ---");
                        ArrayList<Storage> barrels = h.obterInfoBarrels();
                        ArrayList<DownloaderInfo> downloaders = h.obterInfoDownloaders();
                        if (barrels.size() != 0) {
                            System.out.println("--- Storage Barrels ---");

                            for (Storage s : barrels) {
                                System.out.println(s);
                            }
                        }
                        if (downloaders.size() != 0) {
                            System.out.println("--- Downloaders ---");
                            for (DownloaderInfo d : downloaders) {
                                System.out.println(d);
                            }
                        }


                    } else if (opcao.equals("5")) {
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
