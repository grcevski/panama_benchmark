package org.elasticsearch.panama;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;

public class JNARunner {
    public interface CLibrary extends Library {
        CLibrary INSTANCE = (CLibrary)
                Native.load("c", CLibrary.class);

        int strlen(String testString);
    }

    static class JNACLibrary {
        public static native int strlen(Pointer p);

        static {
            Native.register(Platform.C_LIBRARY_NAME);
        }
    }

    public static int strlen_wrapped(String testString) {
        return CLibrary.INSTANCE.strlen(testString);
    }

    public static int strlen_plain(Pointer jnaPointer) {
        return JNACLibrary.strlen(jnaPointer);
    }

    public static void main(String[] args) {
        System.out.println(CLibrary.INSTANCE.strlen("Nikola @ Elastic"));
    }
}
