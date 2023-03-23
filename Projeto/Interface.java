import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.net.*;
import java.io.*;

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

        //podemos meter variavel para depois verificar se estamos logados ou nao
        //tendo em conta a mensagem recevida podemos ver se e um login ou um logout.
        if(msg_received[3].equals("logged")){

            if(msg_received[5].equals("on;"))
                logged_in = true;
            else
                logged_in = false;
            
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
    }
    /**
     * Le inteiro inserido na consola
     * @return
     */
    private static int read_int(){
        
        int n;
        System.out.printf("Digite o numero:");
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
     * @return
     */
    private static String read_text(){ 
         
        String str;  
         
        try{
                
        Scanner sc = new Scanner(System.in);
        str = sc.nextLine();
            
        }
        //Se o valor for um valor causar um erro, ira ser avisado ao usuario que o valor nao e valido.
        catch (java.util.InputMismatchException e){
            System.out.printf("Valor Introduzido nao e valido.");
            return null;
        }
        
    return str;
    }

    private static Boolean verify_value(String username)
    {
        if (username.contains("|") || username.contains(";") || username.contains("\\n"))
            return false;
        else
            return true;

    }
    
    /**
     * Inicia conexao com servidor e pergunta ao cliente o que ele deseja fazer, realiza diferentes operacoes tendo em conta a escolha do cliente
     * @param args
     */
    public static void main(String args[]) {

        try{
            
            Boolean finish = false;
            Hello_S_I h = (Hello_S_I) LocateRegistry.getRegistry(7000).lookup("XPTO");

            Interface c = new Interface();

            h.subscribe("cliente", (Hello_C_I) c);

            while(true)
            {
                System.out.println("O que deseja realizar?\n\n1 - Login\n2 - Registar\n3 - Logout\n4 - Indexar novo URL\n5 - Realizar pesquisa\n6 - Consultar informacoes gerais do sistema\n7 - Sair do programa\n");
                
                int num = read_int();
                
                if(num != -1){

                    int choice = num;
                    
                    switch(choice)
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
                            break;
                        case(5):
                            h.print_on_server("type | search;" , (Hello_C_I) c);
                            break;
                        case(6):
                            break;
                        case(7):
                            h.unsubscribe("cliente", (Hello_C_I) c);
                            finish = true;
                            break;
                    }
                }
                
                if(finish == true){
                    System.exit(0);
                }
            }
        
        
        } catch (Exception e) {
            System.out.println("Exception in main: " + e);
        }
    }
}
