package com.tulagingerbread;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.BitSet;

public class State {
    private int w;
    private int h;
    private BitSet cells;
    private final int[] nei = new int[] {-1, 0, 1};

    public State(int w, int h) {
        this.w = w;
        this.h = h;
        this.cells = new BitSet(w*h);
    }

    public State(int w, int h, BitSet cells) {
        this(w, h);
        this.cells = cells;
    }

    public static State getRandomState(int w, int h) {
        State s = new State(w, h);
        for (int i = 0; i < w; i++ ) {
            for (int j = 0; j < h; j++) {
                if (Math.random() < 0.4) s.spawn(i, j);
            }
        }
        return s;
    }

    public boolean isCellAlive(int i, int j) {
        return cells.get(i * w + j);
    }

    public int getAdjacentAlive(int i, int j) {
        int count = isCellAlive(i, j) ? -1 : 0;
        for (int m : nei) for (int n : nei) {
            int k = (i + m) % w;
            int l = (j + n) % h;
            if (k < 0) k += w;
            if (l < 0) l += h;
            if (isCellAlive(k, l)) count++;
        }
        return count;
    }

    public void spawn(int i, int j) {
        cells.set(i * w + j);
    }

    public static State getCurrentState(MappedByteBuffer ssm) {
        Locker locker = new Locker();
        locker.lock();
        byte[] ar = new byte[ssm.capacity()];
        ssm.rewind();
        for (int i = 0; i < ssm.capacity(); i++) ar[i] = ssm.get();
        locker.unlock();
        return State.fromBytes(ar);
    }

    public static void setCurrentState(State currentState, MappedByteBuffer ssm) {
        Locker locker = new Locker();
        locker.lock();
        ssm.rewind();
        ssm.put(currentState.toBytes());
        locker.unlock();
    }

    public static void drawState(OutputStream out, MappedByteBuffer ssm) throws IOException {
        State current = getCurrentState(ssm);
        drawState(current, out);
    }

    public static void drawState(State current, OutputStream out) throws IOException {
        for (int i = 0; i < current.getWidth(); i++) out.write('=');
        out.write(0xd);
        out.write(0xa);
        for (int i = 0; i < current.getHeight(); i++) {
            for (int j = 0; j < current.getWidth(); j++) {
                out.write(current.isCellAlive(j, i) ? '#' : ' ');
            }
            out.write(0xd);
            out.write(0xa);
        }
    }

    public static int maximumLength(int w, int h) {
        State s = new State(w, h);
        for (int i = 0; i < w; i++) for (int j = 0; j < h; j++) s.spawn(i, j);
        return s.toBytes().length;
    }

    public byte[] toBytes() {
        byte[] ba = cells.toByteArray();
        ByteBuffer bb = ByteBuffer.allocate(8 + ba.length);
        bb.putInt(w);
        bb.putInt(h);
        bb.put(ba);
        return bb.array();
    }

    public static State fromBytes(byte[] a) {
        ByteBuffer bb = ByteBuffer.wrap(a);
        int width = bb.getInt();
        int height = bb.getInt();
        BitSet bs = BitSet.valueOf(bb);
        return new State(width, height, bs);
    }

    public int getWidth() {
        return w;
    }

    public int getHeight() {
        return h;
    }
}
