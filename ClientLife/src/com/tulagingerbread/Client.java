package com.tulagingerbread;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Client {

    public static void main(String[] args) {
        try {
            Socket s = new Socket(InetAddress.getByName("127.0.0.1"), 2550);
            InputStream is = s.getInputStream();
            while (true) {
                s.getOutputStream().write(0xa);
                byte[] size = new byte[4];
                ByteBuffer bb = ByteBuffer.wrap(size);
                if (is.read(size) < 4) {
                    System.err.println("Some error with size");
                }
                int l = bb.getInt();
                byte[] data = new byte[l];
                if (is.read(data) < l) {
                    System.err.println("Some error with data");
                }
                State current = State.fromBytes(data);
                State.drawState(current, System.out);
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
