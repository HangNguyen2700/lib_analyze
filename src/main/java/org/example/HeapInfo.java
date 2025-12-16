package org.example;

public class HeapInfo {
    public static void printHeapStats() {
        Runtime rt = Runtime.getRuntime();

        long maxMemory = rt.maxMemory();        // max heap (≈ -Xmx)
        long totalMemory = rt.totalMemory();      // currently allocated heap
        long freeMemory = rt.freeMemory();       // free in the allocated heap
        long usedMemory = totalMemory - freeMemory;

        System.out.println("Max heap      : " + bytesToMB(maxMemory) + " MB");
        System.out.println("Total (commit): " + bytesToMB(totalMemory) + " MB");
        System.out.println("Used          : " + bytesToMB(usedMemory) + " MB");
        System.out.println("Free          : " + bytesToMB(freeMemory) + " MB");
    }

    private static long bytesToMB(long bytes) {
        return bytes / (1024 * 1024);
    }

    public static void main(String[] args) {
        printHeapStats();
    }
}


