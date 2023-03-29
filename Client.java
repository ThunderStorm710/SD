import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.*;

public class Client {

    public Client() {
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
                    System.out.println("Inscricao validada com sucesso!");
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

    public static void indexarURL(SearchModule_I h, ClienteInfo cliente) throws RemoteException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Insira um URL: ");
        String opcao = sc.nextLine();
        System.out.println("Vou indexar!");
        h.indexarURL(cliente, opcao);
    }

    public static void consultarListaPaginas(SearchModule_I h, ClienteInfo cliente) throws RemoteException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Insira um link url: ");
        String linha = sc.nextLine();
        System.out.println();

        ArrayList<HashSet<String>> lista = h.obterLinks(cliente, linha);
        if (lista.size() == 0) {
            System.out.println("Ligacao URL nao encontrada...");
        } else {
            for (HashSet<String> cadeia : lista) {
                for (String s : cadeia) {
                    System.out.println(s);
                }
            }
        }
    }

    public static void obterInfoGerais(SearchModule_I h) throws RemoteException {

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
    }

    public static void realizarPesquisa(SearchModule_I h, ClienteInfo cliente) throws RemoteException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Pesquisa: ");
        String linha = sc.nextLine();
        HashSet<String[]> paginas = h.pesquisarPaginas(cliente, linha);
        if (paginas != null) {
            System.out.println("------ Resultados da pesquisa ------");

            if (paginas.size() > 10) {
                int i, contador;
                ArrayList<String[]> lista = new ArrayList<>(paginas);
                for (i = 0; i < lista.size() && i < 10; i++) {
                    System.out.println(Arrays.toString(lista.get(i)));
                }
                label:
                while (true) {
                    if (i >= 10 && i < lista.size()) {
                        System.out.println("""
                                ---------------
                                \t1 - Anterior
                                \t2 - Proximo
                                \t3 - Sair
                                ---------------
                                """);
                    } else if (i >= lista.size()) {
                        System.out.println("""
                                ---------------
                                \t1 - Anterior
                                \t3 - Sair
                                ---------------
                                """);
                    } else {
                        System.out.println("""
                                ---------------
                                \t2 - Proximo
                                \t3 - Sair
                                ---------------
                                """);
                    }

                    String opcao = sc.nextLine();

                    switch (opcao) {
                        case "1":
                            if (i >= 10) {
                                if (i >= lista.size()) {
                                    i = lista.size() - 1;
                                }
                                contador = i - 10;

                                for (; contador < i; contador++) {
                                    System.out.println(Arrays.toString(lista.get(contador)));
                                }
                                i -= 10;
                            } else {
                                contador = 0;
                                for (; contador < i; contador++) {
                                    System.out.println(Arrays.toString(lista.get(contador)));
                                }
                                i = 0;
                            }
                            break;
                        case "2":
                            contador = i;
                            if (i < lista.size()) {
                                for (; contador < lista.size() && contador < i + 10; contador++) {
                                    System.out.println(Arrays.toString(lista.get(contador)));
                                }
                                i = contador;
                            }
                            break;
                        case "3":
                            break label;
                        default:
                            System.out.println("Opcao invalida...");
                            break;
                    }
                }
            } else {
                for (String[] pagina : paginas) {
                    System.out.println(Arrays.toString(pagina));
                }
            }

        } else {
            System.out.println("Pedimos desculpa mas nao foram encontradas paginas relevantes");
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String opcao;
        boolean entrada = false;
        ClienteInfo cliente = null;
        try {
            SearchModule_I h = (SearchModule_I) LocateRegistry.getRegistry(1100).lookup("Search_Module");

            label1:
            while (!entrada) {
                System.out.println("""
                        --- BEM-VINDO ---
                        \t1 - Registar
                        \t2 - Login
                        \t3 - Sair
                        -----------------
                        """);
                opcao = sc.nextLine();

                switch (opcao) {
                    case "1":
                        if ((cliente = registar(h)) != null) {
                            entrada = true;
                        }

                        break;
                    case "2":
                        if ((cliente = login(h)) != null) {
                            entrada = true;
                        }
                        break;
                    case "3":
                        break label1;

                    default:
                        System.out.println("Opcao invalida...");
                        break;
                }
            }
            if (entrada) {
                label:
                while (true) {

                    System.out.println("""
                            ------------------- BEM-VINDO -------------------
                            Que operacao deseja realizar?
                            \t1 - Indexar novo URL
                            \t2 - Pesquisar
                            \t3 - Consultar lista de paginas
                            \t4 - Obter Informacoes gerais sobre o sistema
                            \t5 - Sair
                            -------------------------------------------------
                            """);

                    opcao = sc.nextLine();

                    switch (opcao) {
                        case "1":
                            indexarURL(h, cliente);

                            break;
                        case "2":
                            realizarPesquisa(h, cliente);

                            break;
                        case "3":
                            consultarListaPaginas(h, cliente);

                            break;
                        case "4":
                            obterInfoGerais(h);
                            break;
                        case "5":
                            break label;

                        default:
                            System.out.println("Opcao invalida...");
                            break;
                    }

                }
            }

        } catch (java.rmi.RemoteException |
                java.rmi.NotBoundException e) {
            System.out.println("RemoteException");
        }
    }
}
