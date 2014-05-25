package com.tulagingerbread;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class Locker {
    private FileLock lock = null;
    private static File file = new File("life.lock");

    public void lock() {
        try {
            FileChannel fc = new RandomAccessFile(file, "rw").getChannel();
            while (lock == null) lock = fc.tryLock();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unlock() {
        if (lock == null) return;
        try {
            lock.release();
            lock = null;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
