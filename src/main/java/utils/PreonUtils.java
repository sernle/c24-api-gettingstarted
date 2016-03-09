package utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import biz.c24.io.api.data.preon.PreonDataObject;

public class PreonUtils {
    
    public static long sizeOf(PreonDataObject preon) {
        return preon.getPreonData().length - preon.getBufferOffset();
    }
    
    public static long sizeOf(File file) throws IOException {
        long length = file.length();
        if(length == 0) {
            // See if it was really in the classpath
            // Filter out a search for '.', '..' & ''
            String path = file.getCanonicalPath();
            if(!path.matches("[\\.]*")) {
                boolean isAbsolute = file.isAbsolute();
                InputStream stream = PreonUtils.class.getResourceAsStream(isAbsolute? path : "/" + path);
                if(stream == null && !isAbsolute) {
                    // See if they really did mean to find it beneath biz/c24/io/api in the classpath
                    stream = PreonUtils.class.getResourceAsStream(path);
                }
                if(stream != null) {
                    length = stream.available();
                }
            }
        }
        return length;
    }

    public static void visualise(PreonDataObject preon, PrintStream stream) {

        byte[] bytes = preon.getPreonData();
        for(int i=0; i < bytes.length; i++) {
            if(i % 80 == 0) {
                stream.println();
            }
            char c = (char) bytes[i];
            stream.print((c <= 'z' && c >= ' '? c : '.'));
        }
        stream.println();
    }
    
}
