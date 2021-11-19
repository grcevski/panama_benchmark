package co.elasticsearch.panama.benchmark;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import org.elasticsearch.panama.JNARunner;
import org.elasticsearch.panama.Panama;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Random;

@State(Scope.Benchmark)
public class PanamaBenchmark {
    static Random random = new Random(123L);

    static String randomStringWithLength(int len) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(len)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    @Param({"10", "100", "1000", "10000"})
    public int testStringLength;

    public String testString;
    public ByteBuffer bb;
    Pointer jnaPointer;
    Object panamaSegment;

    @Setup(Level.Trial)
    public void setUp() {
        testString = randomStringWithLength(testStringLength);
        bb = ByteBuffer.allocateDirect(testStringLength);
        bb.order(ByteOrder.nativeOrder());
        bb.put(testString.getBytes(StandardCharsets.UTF_8));
        bb.clear();
        jnaPointer = Native.getDirectBufferPointer(bb);
        panamaSegment = Panama.getMemorySegment(bb);
    }

    @Benchmark
    public void panama(Blackhole bh) {
        Panama.strlen_wrapped(testString);
    }

    @Benchmark
    public void jna(Blackhole bh) {
        JNARunner.strlen_wrapped(testString);
    }

    @Benchmark
    public void panamaNoAlloc(Blackhole bh) {
        Panama.strlen_plain(panamaSegment);
    }

    @Benchmark
    public void jna_plain(Blackhole bh) {
        JNARunner.strlen_plain(jnaPointer);
    }
}
