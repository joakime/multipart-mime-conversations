package org.eclipse.jetty.demo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ThreadLocalRandom;

import org.eclipse.jetty.toolchain.test.MavenTestingUtils;
import org.eclipse.jetty.toolchain.test.Sha1Sum;

public class Util
{
    public static final Path pngPath;

    static
    {
        pngPath = MavenTestingUtils.getProjectFilePath("src/main/resources/base-browser-capture/jetty-avatar-256.png");
    }

    public static String sha1sum(byte buf[]) throws IOException
    {
        try
        {
            return Sha1Sum.calculate(buf);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IOException(e);
        }
    }

    /**
     * Create a new Boundary String allowing only valid (per spec) characters
     */
    public static String newSpecBoundaryString()
    {
        int size = ThreadLocalRandom.current().nextInt(30, 40);
        char cbuf[] = new char[size];
        for (int i = 0; i < size; i++)
        {
            cbuf[i] = SPEC_CHARS[ThreadLocalRandom.current().nextInt(SPEC_CHARS.length)];
        }
        return new String(cbuf);
    }

    private final static char[] SPEC_CHARS =
            "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
                    .toCharArray();

    public static String urlEncode(String raw, Charset charset) throws UnsupportedEncodingException
    {
        return URLEncoder.encode(raw, charset.toString());
    }
}
