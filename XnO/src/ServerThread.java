import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;

import java.util.Map;

public class ServerThread extends Thread {
    public String[][] xno;
    private Socket[] socket;

    String[] arr;
    String line = "";
    int switcher = 0;
    byte[] respBuff = new byte[508];
    BufferedReader FirstPlayerIn = null;
    PrintWriter FirstPlayerOut = null;
    BufferedReader SecondPlayerIn = null;
    PrintWriter SecondPlayerOut = null;

    public ServerThread(Socket[] socket) {
        // super();
        this.socket = socket;
    }

    String win = "";

    public void chekWin() {
        if (!xno[0][0].equals(" ") && xno[0][0].equals(xno[0][1]) && xno[0][1].equals(xno[0][2])) {
            win = xno[0][0];
            return;
        }
        if (!xno[1][0].equals(" ") && xno[1][0].equals(xno[1][1]) && xno[1][1].equals(xno[1][2])) {
            win = xno[1][0];
            return;
        }
        if (!xno[2][0].equals(" ") && xno[2][0].equals(xno[2][1]) && xno[2][1].equals(xno[2][2])) {
            win = xno[2][0];
            return;
        }

        if (!xno[0][0].equals(" ") && xno[0][0].equals(xno[1][0]) && xno[1][0].equals(xno[2][0])) {
            win = xno[0][0];
            return;
        }
        if (!xno[0][1].equals(" ") && xno[0][1].equals(xno[1][1]) && xno[1][1].equals(xno[2][1])) {
            win = xno[0][1];
            return;
        }
        if (!xno[0][2].equals(" ") && xno[0][2].equals(xno[1][2]) && xno[1][2].equals(xno[2][2])) {
            win = xno[0][2];
            return;
        }

        if (!xno[0][0].equals(" ") && xno[0][0].equals(xno[1][1]) && xno[1][1].equals(xno[2][2])) {
            win = xno[0][0];
            return;
        }
        if (!xno[0][2].equals(" ") && xno[0][2].equals(xno[1][1]) && xno[1][1].equals(xno[2][0])) {
            win = xno[0][2];
            return;
        }
    }


    public void run() {
        xno = new String[][]{{" ", " ", " "}, {" ", " ", " "}, {" ", " ", " "}};
        try {
            FirstPlayerIn = new BufferedReader(new InputStreamReader(socket[0].getInputStream()));
            FirstPlayerOut = new PrintWriter(socket[0].getOutputStream(), true);
            SecondPlayerIn = new BufferedReader(new InputStreamReader(socket[1].getInputStream()));
            SecondPlayerOut = new PrintWriter(socket[1].getOutputStream(), true);
            try {
                FirstPlayerOut.println("go");
                while (true) {
                    if (switcher == 0 && (line = FirstPlayerIn.readLine()) != null && !line.isEmpty()) {
                        switcher = 1;
                        SecondPlayerOut.println("go");

                        arr = line.split(" ");
                    } else if (switcher == 1 && (line = SecondPlayerIn.readLine()) != null && !line.isEmpty()) {
                        switcher = 0;
                        FirstPlayerOut.println("go");

                        arr = line.split(" ");
                    }

                    if (xno[Integer.valueOf(arr[1])][Integer.valueOf(arr[2])] == " ") {
                        xno[Integer.valueOf(arr[1])][Integer.valueOf(arr[2])] = arr[0];
                    }

                    for (int i = 0; i < xno.length; i++) {
                        FirstPlayerOut.print("|");
                        SecondPlayerOut.print("|");
                        respBuff = ("|").getBytes();
                        for (Map.Entry<Integer, InetAddress> entry : Server.viewers.entrySet()) {
                            DatagramPacket resp = new DatagramPacket(respBuff, respBuff.length, entry.getValue(), entry.getKey());
                            Server.datagramSocket.send(resp);
                        }

                        System.out.print("|");
                        for (int j = 0; j < xno[i].length; j++) {
                            FirstPlayerOut.print(xno[i][j] + "|");
                            SecondPlayerOut.print(xno[i][j] + "|");
                            respBuff = (xno[i][j] + "|").getBytes();
                            for (Map.Entry<Integer, InetAddress> entry : Server.viewers.entrySet()) {
                                DatagramPacket resp = new DatagramPacket(respBuff, respBuff.length, entry.getValue(), entry.getKey());
                                Server.datagramSocket.send(resp);
                            }
                            System.out.print(xno[i][j] + "|");
                        }
                        FirstPlayerOut.println(" ");
                        SecondPlayerOut.println(" ");
                        respBuff = (" " + "\n").getBytes();
                        for (Map.Entry<Integer, InetAddress> entry : Server.viewers.entrySet()) {
                            DatagramPacket resp = new DatagramPacket(respBuff, respBuff.length, entry.getValue(), entry.getKey());
                            Server.datagramSocket.send(resp);
                        }
                        System.out.println();
                    }
                    chekWin();
                    if (!win.equals("")) {
                        FirstPlayerOut.println(win + " wins!!!");
                        SecondPlayerOut.println(win + " wins!!!");
                        respBuff = (win + " wins!!!").getBytes();
                        for (Map.Entry<Integer, InetAddress> entry : Server.viewers.entrySet()) {
                            DatagramPacket resp = new DatagramPacket(respBuff, respBuff.length, entry.getValue(), entry.getKey());
                            Server.datagramSocket.send(resp);
                        }
                        switcher = 0;
                        xno = new String[][]{{" ", " ", " "}, {" ", " ", " "}, {" ", " ", " "}};

                        FirstPlayerOut.println("reconnect");
                        SecondPlayerOut.println("reconnect");
                    }
                }
            } catch (IOException e) {
                System.out.println("Error during communication");
            }

        } catch (Exception e1) {

        }

        try {
            FirstPlayerOut.print("reconnect");
            SecondPlayerOut.print("reconnect");
        } catch (Exception e) {

        }
    }

}
