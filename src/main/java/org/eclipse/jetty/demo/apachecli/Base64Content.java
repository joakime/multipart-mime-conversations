package org.eclipse.jetty.demo.apachecli;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.codec.binary.Base64.MIME_CHUNK_SIZE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.AbstractContentBody;
import org.eclipse.jetty.toolchain.test.IO;

public class Base64Content extends AbstractContentBody
{
    public static final byte[] CHUNK_SEP_LN = {'\n'};
    private final String filename;
    private byte rawBase64[];

    public Base64Content(String content, ContentType contentType)
    {
        super(contentType);
        this.rawBase64 = Base64.getEncoder().encode(content.getBytes(UTF_8));
        this.filename = null;
    }

    public Base64Content(Path file, ContentType contentType) throws IOException
    {
        this(file, contentType, MIME_CHUNK_SIZE, CHUNK_SEP_LN);
    }

    public Base64Content(Path file, ContentType contentType, int lineLength, final byte[] lineSeparator) throws IOException
    {
        super(contentType);

        try(InputStream in = Files.newInputStream(file);
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            Base64OutputStream b64out = new Base64OutputStream(buf, true, lineLength, lineSeparator))
        {
            IO.copy(in, b64out);
            b64out.flush();
            rawBase64 = buf.toByteArray();
        }

        this.filename = file.getFileName().toString();
    }

    @Override
    public String getFilename()
    {
        return this.filename;
    }

    @Override
    public String getTransferEncoding()
    {
        return "base64";
    }

    @Override
    public long getContentLength()
    {
        return this.rawBase64.length;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException
    {
        out.write(rawBase64);
    }
}
