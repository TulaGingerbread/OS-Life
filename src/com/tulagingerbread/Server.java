package com.tulagingerbread;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class Server implements Runnable {
    private final ServerSocket socket;
    private final MappedByteBuffer ssm;

    public static void main(String[] args) {
        try {
            int w = args.length > 0 ? Integer.parseInt(args[0]) : 30;
            int h = args.length > 1 ? Integer.parseInt(args[1]) : 20;
            String filename = args.length > 2 ? args[2] : "D:\\state.bin";
            State random = State.getRandomState(w, h);
            new Thread(new Server(2550, filename, random)).start();
            new ProcessBuilder("\"C:\\Program Files\\Java\\jdk1.8.0_05\\bin\\java.exe\"",
                               "-cp", "out\\production\\ServerLife",
                               StateCalc.class.getCanonicalName()
            ).start();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Server(int port, String filename, State initial) throws IOException {
        RandomAccessFile memoryFile = new RandomAccessFile(filename, "r");
        ssm = memoryFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, initial.toBytes().length);
        socket = new ServerSocket(port);
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket client = socket.accept();
                System.out.println("Client connected from "
                                   + client.getInetAddress().getHostAddress()
                                   + ":" + client.getPort()
                );
                new Thread(new Client(client)).start();
            }
            catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private class TelnetClient implements Runnable {
        OutputStream os;

        TelnetClient(Socket s) throws IOException {
            this.os = s.getOutputStream();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    State.drawState(os, ssm);
                    Thread.sleep(1000);
                }
                catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    private class Client implements Runnable {
        InputStream is;
        OutputStream os;

        Client(Socket s) throws IOException {
            this.is = s.getInputStream();
            this.os = s.getOutputStream();
        }

        @Override
        public void run() {
            int a;
            try {
                while ((a = is.read()) == 0xa) {
                    byte[] st = State.getCurrentState(ssm).toBytes();
                    try {
                        ByteBuffer bb = ByteBuffer.allocate(4);
                        bb.putInt(st.length);
                        os.write(bb.array());
                        os.write(st);
                        os.flush();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            catch (SocketException ignored) {}
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
