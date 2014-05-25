package com.tulagingerbread;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class StateCalc {
    private final MappedByteBuffer ssm;

    public static void main(String[] args) {
        try {
            int w = args.length > 0 ? Integer.parseInt(args[0]) : 30;
            int h = args.length > 1 ? Integer.parseInt(args[1]) : 20;
            String filename = args.length > 2 ? args[2] : "state.bin";
            new StateCalc(filename, State.getRandomState(w, h)).start();
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

    public void start() {
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
            catch (InterruptedException ignored) {}
        }
    }
}
