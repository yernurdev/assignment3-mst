package org.example.mst.utils;

public class Counter {
    private long count = 0L;
    public void inc() { count++; }
    public void add(long k) { count += k; }
    public long get() { return count; }
}
