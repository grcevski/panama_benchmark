package org.elasticsearch.panama;

import jdk.incubator.foreign.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;

import static jdk.incubator.foreign.CLinker.*;

public class Panama {
    private final static CLinker LINKER = CLinker.getInstance();
    private final static ClassLoader LOADER = Panama.class.getClassLoader();
    static final SymbolLookup LIBRARIES = lookup();    /* package-private */ Panama() {}

    static SymbolLookup lookup() {
        SymbolLookup loaderLookup = SymbolLookup.loaderLookup();
        SymbolLookup systemLookup = CLinker.systemLookup();
        return name -> loaderLookup.lookup(name).or(() -> systemLookup.lookup(name));
    }

    static final MethodHandle downcallHandle(SymbolLookup LOOKUP, String name, String desc, FunctionDescriptor fdesc) {
        return LOOKUP.lookup(name).map(
                addr -> {
                    MethodType mt = MethodType.fromMethodDescriptorString(desc, LOADER);
                    return LINKER.downcallHandle(addr, mt, fdesc);
                }).orElse(null);
    }

    static final FunctionDescriptor strlen$FUNC = FunctionDescriptor.of(C_LONG,
            C_POINTER
    );

    static final MethodHandle strlen$MH = downcallHandle(
            LIBRARIES, "strlen",
            "(Ljdk/incubator/foreign/MemoryAddress;)J",
            strlen$FUNC
    );

    public static MethodHandle strlen$MH() {
        return strlen$MH;
    }

    public static long strlen ( Addressable __s) {
        var mh$ = strlen$MH;
        try {
            return (long)mh$.invokeExact(__s.address());
        } catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }

    public static long strlen_wrapped(String testString) {
        try (ResourceScope scope = ResourceScope.newConfinedScope()) {
            Addressable testAddress = CLinker.toCString(testString, scope).address();
            return strlen(testAddress);
        }
    }

    public static long strlen_plain(Object panamaSegment) {
        return strlen(((MemorySegment)panamaSegment).address());
    }

    public static void main(String[] args) {
        System.out.println(strlen_wrapped("Nikola @ Elastic"));
    }

    public static Object getMemorySegment(ByteBuffer bb) {
        return MemorySegment.ofByteBuffer(bb);
    }
}
