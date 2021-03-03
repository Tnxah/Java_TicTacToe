import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public class Client {
    static Scanner entered = new Scanner(System.in);
    static String string = "";
    static Socket socket = null;
    static PrintWriter out = null;
    static BufferedReader in = null;
    static String address = "localhost";
    static volatile String regex = "";
    static int port = 1111;
    static DatagramSocket udpSocket = null;
    static int udpPort = 0;
    static InetAddress inetAddress;

    static {
        try {
            udpSocket = new DatagramSocket();
            inetAddress = InetAddress.getByName("localhost");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static volatile boolean myTurn = true;

    static Runnable udp = () -> {
        //myTurn = true;
        while (true) {
            byte[] buff = new byte[508];
            DatagramPacket packet = new DatagramPacket(buff, buff.length);

            try {
                udpSocket.receive(packet);
                System.out.print(new String(
                        packet.getData(), 0, packet.getLength()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    static Runnable r = () -> {
        String line;
        try {
            while ((line = in.readLine()) != null) {

                switch (line) {
                    case "menu":
                        System.out.println("Enter one of these commands");
                        System.out.println("PLAY - to start the game or enter the queue");
                        System.out.println("LIST - to view all games");
                        System.out.println("LOGOUT - to logout");
                        regex = "^[LIST,PLAY,LOGOUT]+$";
                        myTurn = true;
                        break;

                    case "go":
                        myTurn = true;
                        regex = "[X,O] \\d \\d";
                        System.out.println("Now is your turn");
                        break;

                    case "reconnect":
                        socket.close();
                        connection();
                        break;
                }
                if (line.matches("^[\\d]+$")) {
                    System.out.println(line);
                    udpPort = Integer.valueOf(line);
                    udpSocket = new DatagramSocket();
                    udp.run();
                }

                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Error during communication");
            System.exit(-1);
        }
    };

    public static void main(String[] args) {

        connection();
        while (!string.equals("LOGOUT")) {
            if (myTurn) {
                string = entered.nextLine();
                if (!string.matches(regex)) {
                    System.out.println("Wrong format");
                    System.out.println("Try again");
                    continue;
                }
                if(string.equals("LIST")) {
                    out.println(string);
                    try {
                        sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    byte[] buff = new byte[508];
                    DatagramPacket packet = new DatagramPacket(buff, buff.length, inetAddress, udpPort);
                    try {
                        if(udpSocket != null){
                            System.out.println("not null");
                            udpSocket.send(packet);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(!string.equals("LIST")) {
                    out.println(string);
                    System.out.println(socket.getLocalPort());
                    myTurn = false;
                }
            }
        }
        out.println();
        try {

            socket.close();
            System.out.println("Session is finished");
            sleep(2000);
            System.exit(0);
        } catch (Exception e) {
            System.out.println("Cannot close the socket");
            System.exit(-1);
        }
    }

    static void connection() {
        try {
            socket = new Socket(address, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Thread myThread = new Thread(r, "Game");
            myThread.start();
        } catch (UnknownHostException e) {
            System.out.println("Unknown host ");
            System.exit(-1);
        } catch (IOException e) {
            System.out.println("No I/O");
            System.exit(-1);
        }
    }

}
