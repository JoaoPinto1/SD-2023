package RMIClient;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.LocateRegistry;
import java.util.Arrays;
import java.util.Scanner;


public class Interface extends UnicastRemoteObject implements Hello_C_I {

    static boolean logged_in = false;

    Interface() throws RemoteException {
        super();
    }

    /**
     * Realiza diferentes operacoes tendo em conta mensagem recevida pelo servidor
     */
    public void print_on_client(String s) throws RemoteException {

        //"type | status; logged | on; msg | Welcome to the app"
        String[] msg_received = s.split(" " , 0);

        if(msg_received[3].equals("logged")){

            logged_in = msg_received[5].equals("on;");

            if(msg_received.length > 6)
            {
                for(int i = 8 ; i < msg_received.length ; i++){
                    System.out.print(msg_received[i] + " ");
                }

                System.out.println("\n");
            }

        }
        else if(msg_received[3].equals("register")){
            System.out.println();
            if(msg_received.length > 6)
            {
                for(int i = 8 ; i < msg_received.length ; i++){
                    System.out.print(msg_received[i] + " ");
                }

                System.out.println("\n");
            }
        }
        //type | status; search | result; " + received_string[5]
        else if(msg_received[3].equals("search")){
            String search_results = msg_received[6];
            String[] separate_results = search_results.split(";");

            int counter = 0;

            for(int i = 0 ; i < separate_results.length ; i += 3){

                counter ++;
                System.out.println(separate_results[i] + "\n" + separate_results[i+ 1] + "\n" + separate_results[i+2] + "\n");

                if(counter == 10){
                    System.out.println("Deseja ir para a proxima pagina?\n");
                    System.out.println("1 - sim\n2 - nao");
                    int escolha = read_int();

                    if(escolha != 1)
                    {
                        break;
                    }
                    else
                        counter = 0;
                }
            }

        }
    }
    /**
     * Le inteiro inserido na consola
     *
     */
    private static int read_int(){

        int n;
        System.out.print("Digite o numero:");
        try{

            Scanner sc = new Scanner(System.in);
            n = sc.nextInt();

        }
        //Se o valor for um valor causar um erro, ira ser avisado ao usuario que o valor nao e valido.
        catch (java.util.InputMismatchException e){
            return -1;
        }

        return n;
    }
    /**
     * Le texto inserido na consola
     *
     */
    private static String read_text(){

        String str;

        try{

            Scanner sc = new Scanner(System.in);
            str = sc.nextLine();

        }
        //Se o valor for um valor causar um erro, ira ser avisado ao usuario que o valor nao e valido.
        catch (java.util.InputMismatchException e){
            System.out.print("Valor Introduzido nao e valido.");
            return null;
        }

        return str;
    }

    private static Boolean verify_value(String username)
    {
        return !username.contains("|") && !username.contains(";") && !username.contains("\\n");

    }

    /**
     * Inicia conexao com servidor e pergunta ao cliente o que ele deseja fazer, realiza diferentes operacoes tendo em conta a escolha do cliente
     *
     */
    public static void main(String[] args) {

        try{

            boolean finish = false;
            Hello_S_I h = (Hello_S_I) LocateRegistry.getRegistry(7000).lookup("XPTO");

            Interface c = new Interface();

            h.subscribe("cliente", (Hello_C_I) c);

            while(true)
            {
                System.out.println("O que deseja realizar?\n\n1 - Login\n2 - Registar\n3 - Logout\n4 - Indexar novo URL\n5 - Realizar pesquisa\n6 - Consultar informacoes gerais do sistema\n7 - Sair do programa\n");

                int num = read_int();

                if(num != -1){

                    switch(num)
                    {
                        case(1):

                            System.out.println("\nUsername:");
                            String username = read_text();

                            if(username == null)
                                break;


                            if (!verify_value(username))
                            {
                                System.out.println("Nao pode conter os carateres '|' , ';' e '\\n'\n");
                                break;
                            }

                            else{
                                System.out.println("\nPassword:");
                                String password = read_text();

                                if(password == null)
                                    break;

                                if (!verify_value(password))
                                {
                                    System.out.println("Nao pode conter os carateres '|' , ';' e '\\n'\n");
                                    break;
                                }

                                else{
                                    String msg = "type | login; username | " + username + "; password | " + password;
                                    h.print_on_server(msg , (Hello_C_I) c);

                                }
                            }
                            break;
                        case(2):
                            System.out.println("\nInsira o username desejado:");

                            String username_regist = read_text();

                            if(username_regist == null)
                                break;

                            if (!verify_value(username_regist))
                            {
                                System.out.println("Nao pode conter os carateres '|' , ';' e '\\n'\n");
                                break;
                            }
                            else{
                                System.out.println("\nInsira a password desejada:");
                                String password_regist = read_text();

                                if(password_regist == null)
                                    break;

                                if (!verify_value(password_regist))
                                {
                                    System.out.println("Nao pode conter os carateres '|' , ';' e '\\n'\n");
                                    break;
                                }

                                else{
                                    String msg = "type | regist; username | " + username_regist + "; password | " + password_regist;
                                    h.print_on_server(msg , (Hello_C_I) c);
                                }
                            }

                            break;

                        case(3):
                            if(logged_in){
                                String msg = "type | logout;";
                                h.print_on_server(msg , (Hello_C_I) c);
                            }
                            else
                                System.out.println("Para realizar logout necessita primeiro realizar login.\n");
                            break;
                        case(4):
                            System.out.println("Qual o URL que deseja anexar?");

                            String URL = read_text();

                            String msg = "type | url; url | " + URL;
                            h.print_on_server(msg, (Hello_C_I) c);
                            break;
                        case(5):
                            System.out.println("Quantos termos deseja pesquisar?");
                            int termos = read_int();

                            if(termos == -1){
                                System.out.println("Erro nos termos!");
                            }
                            String[] pesquisa = new String[termos];

                            for(int i = 0 ; i < termos ; i++){

                                pesquisa[i] = read_text();
                                pesquisa[i] = pesquisa[i];

                                if(pesquisa[i] == null) {
                                    System.out.println("Erro na pesquisa!");
                                    break;
                                }
                            }

                            String str = String.join(",", pesquisa);

                            System.out.println(str);

                            h.print_on_server("type | search; pesquisa | " + str , (Hello_C_I) c);

                            break;
                        case(6):
                            h.print_on_server("type | information;" , (Hello_C_I) c);
                            break;
                        case(7):
                            h.unsubscribe("cliente", (Hello_C_I) c);
                            finish = true;
                            break;
                    }
                }

                if(finish){
                    System.exit(0);
                }
            }


        } catch (Exception e) {
            System.out.println("Exception in main: " + e);
        }
    }
}
