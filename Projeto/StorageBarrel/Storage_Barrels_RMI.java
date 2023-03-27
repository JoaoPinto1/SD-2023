package StorageBarrel;
import RMIClient.Hello_S_I;
import RMIClient.Hello_C_I;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.LocateRegistry;
import java.sql.*;
import java.util.*;

public class Storage_Barrels_RMI extends UnicastRemoteObject implements Hello_C_I,Runnable {

    public static Hello_S_I h;
    public static Storage_Barrels_RMI c;

    public Storage_Barrels_RMI() throws RemoteException {
        super();
    }

    /**
     * Realiza diferentes operacoes tendo em conta mensagem recevida pelo servidor
     */
    public void print_on_client(String s) throws RemoteException {

        String[] pesquisa = s.split(",");
        System.out.println(Arrays.toString(pesquisa));
        //todos os URLS em que aparece o primeiro termo.
        ArrayList<String> rowValuesFinal = new ArrayList<String>();
        ArrayList<String> resultList = new ArrayList<String>();

        try {

            Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/googol", "test", "test");
            con.setAutoCommit(false);
            PreparedStatement pre_stmt;
            ResultSet rs;
            pre_stmt = con.prepareStatement("SELECT url_url from word_url where word_word=?;");
            pre_stmt.setString(1, pesquisa[0]);
            ResultSet URLS = pre_stmt.executeQuery();

            while (true){
                try {
                    if (!URLS.next()) break;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                try {
                    rowValuesFinal.add(URLS.getString(1));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }

        if(!rowValuesFinal.isEmpty()) {
            ArrayList<String> rowValues = new ArrayList<String>();
            for (int i = 1; i < pesquisa.length; i++) {
                try {
                    pre_stmt = con.prepareStatement("SELECT url_url from word_url where word_word=?;");
                    pre_stmt.setString((1), pesquisa[i]);
                    ResultSet Url = pre_stmt.executeQuery();
                    while (true){
                        try {
                            if (!Url.next()) break;
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            rowValues.add(Url.getString(1));
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                rowValuesFinal.retainAll(rowValues);

            }
        }

        for(int i = 0; i < rowValuesFinal.size(); i++) {

            pre_stmt = con.prepareStatement("SELECT url.url,title,citation from url where url.url=?;");
            pre_stmt.setString((1), rowValuesFinal.get(i));
            ResultSet Url = pre_stmt.executeQuery();

            while (Url.next()) {
                String url_obtained = Url.getString("url");
                String title = Url.getString("title");
                String citation = Url.getString("citation");
                String resultRow = "url:" + url_obtained +";title:" + title + ";citation:" + citation;
                resultList.add(resultRow);
            }
        }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        String str = String.join(";", resultList);
        System.out.println(str);
        h.print_on_server(str, c);

    }

    /**
     * Inicia conexao com servidor e pergunta ao cliente o que ele deseja fazer, realiza diferentes operacoes tendo em conta a escolha do cliente
     *
     */
    @Override
    public void run() {

        try{

            h = (Hello_S_I) LocateRegistry.getRegistry(7001).lookup("XPT");
            c = new Storage_Barrels_RMI();
            h.subscribe("Storage Barrel", (Hello_C_I) c);
            System.out.println("Storage Barrel Ready");

            while (true) {
                
            }
        
        } catch (Exception e) {
            System.out.println("Exception in main: " + e);
        }
    }
}
