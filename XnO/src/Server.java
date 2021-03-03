import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.HashMap;

public class Server {
    PrintWriter out = null;
    BufferedReader in = null;
    static HashMap<Integer, InetAddress> viewers = new HashMap<Integer,InetAddress>();
    Socket client;
    int index = 0;
    String key = "";
    byte[] buf = new byte[508];
    public static DatagramSocket datagramSocket;



    public static Socket[] pair = new Socket[2];

    public void listenSocket() {
            try {
                datagramSocket = new DatagramSocket();
            } catch (SocketException e) {
                e.printStackTrace();
            }
        ServerSocket server = null;
        try {
            server = new ServerSocket(1111);
        } catch (IOException e) {
            System.out.println("Could not listen");
            System.exit(-1);
        }

        System.out.println("Server listens on port: " + server.getLocalPort());

        while (true) {
            try {
                client = server.accept();
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("menu");
                System.out.println(key = in.readLine());

            } catch (IOException e) {
                e.printStackTrace();
            }

            switch (key) {

                case "PLAY":
                    if (freePorts()) {
                        pair[index] = client;
                        client = null;

                        if (!freePorts()) {
                            System.out.println("start");
                                (new ServerThread(pair)).start();
                                pair = new Socket[2];
                        }
                    }
                    break;

                case "LOGOUT":
                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "LIST":
                    System.out.println(datagramSocket.getLocalPort());
                    out.println(datagramSocket.getLocalPort());

                        try {
                                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                            System.out.println("waiting for receive");
                                datagramSocket.receive(packet);
                            System.out.println("udp received");
                                viewers.put(packet.getPort(),packet.getAddress());
                        }catch(Exception e){

                        }
                    break;

            }

        }

    }

    boolean freePorts() {
        for (int i = 0; i < Server.pair.length; i++) {
            if (pair[i] == null) {
                index = i;
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.listenSocket();
    }
}
