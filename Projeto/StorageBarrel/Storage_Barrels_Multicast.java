package StorageBarrel;

import java.io.*;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;


public class Storage_Barrels_Multicast extends Thread implements Runnable {
    private final String MULTICAST_ADDRESS = "224.3.2.1";
    private final int PORT = 4321;

    public Storage_Barrels_Multicast() throws RemoteException {
        super();
    }

    @Override
    public void run() {
        try (MulticastSocket socket = new MulticastSocket(PORT)) {
            // create socket and bind it
            InetSocketAddress group = new InetSocketAddress(MULTICAST_ADDRESS, PORT);
            NetworkInterface netIf = NetworkInterface.getByName("bge0");
            socket.joinGroup(group, netIf);

            FileOutputStream fileOutWords = new FileOutputStream("words.obj");
            ObjectOutputStream outWords = new ObjectOutputStream(fileOutWords);
            FileOutputStream fileOutUrls = new FileOutputStream("urls.obj");
            ObjectOutputStream outUrls = new ObjectOutputStream(fileOutUrls);
            ObjectInputStream inWords = new ObjectInputStream(new FileInputStream("words.obj"));
            ObjectInputStream inUrls = new ObjectInputStream(new FileInputStream("urls.obj"));
            HashMap<String, HashSet<String>> index = new HashMap<>();
            HashMap<String, HashSet<String>> urls = new HashMap<>();
            urls.put("visited", new HashSet<>());

            while (true) {
                byte[] buffer = new byte[5000];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                System.out.println("Waiting");
                socket.receive(packet);
                System.out.println("Received packet from " + packet.getAddress().getHostAddress() + ":" + packet.getPort() + " with message:");
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println(message);
                String[] split_message = message.split("[|;]+");
                if (!urls.get("visited").contains(split_message[3])) {
                    if (split_message[1].equals("word_list")) {
                        for (int i = 5; i < split_message.length; i += 2) {
                            if (index.containsKey(split_message[i])) {
                                HashSet<String> set = index.get(split_message[i]);
                                set.add(split_message[3]);
                                index.put(split_message[i], set);
                            } else {
                                HashSet<String> set = new HashSet<>();
                                set.add(split_message[3]);
                                index.put(split_message[i], set);
                            }
                        }
                        outWords.writeObject(index);
                        HashMap<String, HashSet<String>> new_index = (HashMap<String, HashSet<String>>) inWords.readObject();
                        System.out.println(new_index);
                    }

                    if (split_message[1].equals("url_list")) {
                        HashSet<String> visited = urls.get("visited");
                        visited.add(split_message[3]);
                        urls.put("visited", visited);
                        for (int i = 5; i < split_message.length; i += 2) {
                            if (urls.containsKey(split_message[i])) {
                                HashSet<String> set = urls.get(split_message[i]);
                                set.add(split_message[3]);
                                urls.put(split_message[i], set);
                            } else {
                                HashSet<String> set = new HashSet<>();
                                set.add(split_message[3]);
                                urls.put(split_message[i], set);
                            }
                        }
                        outUrls.writeObject(urls);
                        HashMap<String, HashSet<String>> new_index = (HashMap<String, HashSet<String>>) inUrls.readObject();
                        System.out.println(new_index);
                    }

                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
