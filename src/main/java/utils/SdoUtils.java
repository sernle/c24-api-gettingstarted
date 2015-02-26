package utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import biz.c24.io.api.data.SimpleDataObject;

public class SdoUtils {
    
    public static long sizeOf(SimpleDataObject sdo) {
        return sdo.getSdoData().length - sdo.getBufferOffset();
    }
    
    public static long sizeOf(File file) throws IOException {
        long length = file.length();
        if(length == 0) {
            // See if it was really in the classpath
            // Filter out a search for '.', '..' & ''
            String path = file.getCanonicalPath();
            if(!path.matches("[\\.]*")) {
                boolean isAbsolute = file.isAbsolute();
                InputStream stream = SdoUtils.class.getResourceAsStream(isAbsolute? path : "/" + path);
                if(stream == null && !isAbsolute) {
                    // See if they really did mean to find it beneath biz/c24/io/api in the classpath
                    stream = SdoUtils.class.getResourceAsStream(path);
                }
                if(stream != null) {
                    length = stream.available();
                }
            }
        }
        return length;
    }

    public static void visualise(SimpleDataObject sdo, PrintStream stream) {

        byte[] bytes = sdo.getSdoData();
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
