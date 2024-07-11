package interpreter.monkey.objects;

public class FNVHash {
    // FNV-1a 32-bit hash parameters
    private static final int FNV_32_PRIME = 0x01000193;
    private static final int FNV_32_INIT = 0x811c9dc5;

    // FNV-1a 64-bit hash parameters
    private static final long FNV_64_PRIME = 0x100000001b3L;
    private static final long FNV_64_INIT = 0xcbf29ce484222325L;

    // 32-bit FNV-1a hash function
    public static int fnv1a32(byte[] data) {
        int hash = FNV_32_INIT;
        for (byte b : data) {
            hash ^= b;
            hash *= FNV_32_PRIME;
        }
        return hash;
    }

    // 64-bit FNV-1a hash function
    public static long fnv1a64(byte[] data) {
        long hash = FNV_64_INIT;
        for (byte b : data) {
            hash ^= b;
            hash *= FNV_64_PRIME;
        }
        return hash;
    }

}
