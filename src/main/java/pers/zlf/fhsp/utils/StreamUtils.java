package pers.zlf.fhsp.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class StreamUtils {
    public static final int BUFFER_SIZE = 4096;

    private StreamUtils() {
    }

    public static int copy(InputStream in, OutputStream out) throws IOException {
        Objects.requireNonNull(in, "No InputStream specified");
        Objects.requireNonNull(out, "No OutputStream specified");
        int byteCount = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
            byteCount += bytesRead;
        }
        out.flush();
        return byteCount;
    }

    public static InputStream markSupport(InputStream in) {
        return in.markSupported() ? in : new BufferedInputStream(in);
    }
}
