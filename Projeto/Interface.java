import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.net.*;
import java.io.*;

public class Interface extends UnicastRemoteObject implements Hello_C_I {

    Interface() throws RemoteException {
        super();
    }

    
    public void print_on_client(String s) throws RemoteException {
        System.out.println("> " + s);
    }

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

    public static void main(String args[]) {

        try(Scanner sc = new Scanner(System.in)) {

            Hello_S_I h = (Hello_S_I) LocateRegistry.getRegistry(7000).lookup("XPTO");;
            Interface c = new Interface();
            h.subscribe(args[0], (Hello_C_I) c);

            while(true)
            {
                System.out.println("O que deseja realizar?\n1 - Login\n2 - Indexar novo URL\n3 - Realizar pesquisa\n4 - Consultar informacoes gerais do sistema\n");
                
                int num = read_int();
                
                if(num != -1){

                    int choice = num;
                    
                    switch(choice)
                    {
                        case(1):
    
                                System.out.println("\nUsername:");
                                String username = read_text();

                                if (!verify_value(username))
                                {
                                    System.out.println("Nao pode conter os carateres '|' , ';' e '\\n'");
                                }

                                if(username == null)
                                    break;
                                else{
    
                                    if(verify_value(username))
                                    {
                                        System.out.println("\nPassword:");
                                        String password = read_text();

                                        if (!verify_value(password))
                                        {
                                            System.out.println("Nao pode conter os carateres '|' , ';' e '\\n'");
                                        }

                                        if(password == null)
                                            break;
                                        
                                        else{
                                            if(verify_value(password))
                                            {
                                                String msg = "type | login; username | " + username + "; password | " + password;
                                                System.out.println(msg);
                                                h.print_on_server(msg);
                                            }
                                            else
                                                break;
                                        }
                                    }
                                    else
                                        break;
                                }
                            break;
                        case(2):
                            break;
                        case(3):
                            break;
                        case(4):
                            break;
                    }
                }

                System.out.println("\n\nDeseja realizar mais alguma coisa?\n1 - Sim\n2 - Nao\n");

                num = read_int();

                if(num != 1)
                    break;

                System.out.println("\n\n");
            }
        } catch (Exception e) {
            System.out.println("Exception in main: " + e);
        }

    }
}
