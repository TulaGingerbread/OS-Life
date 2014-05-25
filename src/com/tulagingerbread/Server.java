package com.tulagingerbread;

import java.io.*;
import java.lang.reflect.Constructor;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class Server implements Runnable {
    private final ServerSocket socket;
    private final MappedByteBuffer ssm;
    private static String clientClassName = "Client";

    public static void main(String[] args) {
        try {
            int i = 0;
            if (args.length > 0 && args[0].equals("-t")) {
                clientClassName = "TelnetClient";
                i++;
            }
            int w = args.length > i ? Integer.parseInt(args[i++]) : 30;
            int h = args.length > i ? Integer.parseInt(args[i++]) : 20;
            String filename = args.length > i ? args[i] : "state.bin";
            State random = State.getRandomState(w, h);
            String java = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java.exe";
            new Thread(new Server(2550, filename, random)).start();
            new ProcessBuilder(java, "-cp", "ServerLife.jar", StateCalc.class.getCanonicalName(),
                               Integer.toString(w), Integer.toString(h), filename
            ).start();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Server(int port, String filename, State initial) throws IOException {
        RandomAccessFile memoryFile = new RandomAccessFile(filename, "rw");
        ssm = memoryFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, initial.toBytes().length);
        socket = new ServerSocket(port);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        while (true) {
            try {
                Socket client = socket.accept();
                System.out.println("Client connected from "
                                   + client.getInetAddress().getHostAddress()
                                   + ":" + client.getPort()
                );
                Class<Runnable> clazz = (Class<Runnable>) Class.forName("com.tulagingerbread.Server$" + clientClassName);
                Constructor<Runnable> constructor = clazz.getDeclaredConstructor(Server.class, Socket.class);
                new Thread(constructor.newInstance(this, client)).start();
            }
            catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public class TelnetClient implements Runnable {
        OutputStream os;

        public TelnetClient(Socket s) throws IOException {
            this.os = s.getOutputStream();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    State.drawState(os, ssm);
                    Thread.sleep(1000);
                }
                catch (SocketException ignored) {
                    System.out.println("Client disconnected");
                    break;
                }
                catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    public class Client implements Runnable {
        InputStream is;
        OutputStream os;

        public Client(Socket s) throws IOException {
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
            catch (SocketException ignored) {
                System.out.println("Client disconnected");
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
