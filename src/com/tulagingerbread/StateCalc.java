package com.tulagingerbread;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class StateCalc implements Runnable {
    private final MappedByteBuffer ssm;

    public static void main(String[] args) {
        try {
            new Thread(new StateCalc("D:\\state.bin", State.getRandomState(30, 20))).start();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public StateCalc(String filename, State initial) throws IOException {
        RandomAccessFile memoryFile = new RandomAccessFile(filename, "rw");
        ssm = memoryFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, initial.toBytes().length);
        State.setCurrentState(initial, ssm);
    }

    @Override
    public void run() {
        while (true) {
            State current = State.getCurrentState(ssm);
            int w = current.getWidth();
            int h = current.getHeight();
            State next = new State(w, h);
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    int c = current.getAdjacentAlive(i, j);
                    boolean a = current.isCellAlive(i, j);
                    if (a && c == 2 || c == 3) next.spawn(i, j);
                }
            }
            State.setCurrentState(next, ssm);
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
