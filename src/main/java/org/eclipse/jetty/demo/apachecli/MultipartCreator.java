package org.eclipse.jetty.demo.apachecli;

import static java.nio.charset.StandardCharsets.UTF_16;
import static java.nio.charset.StandardCharsets.UTF_16BE;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.jetty.toolchain.test.MavenTestingUtils;
import org.eclipse.jetty.toolchain.test.Sha1Sum;

public class MultipartCreator
{
    static
    {
        pngPath = MavenTestingUtils.getProjectFilePath("src/main/resources/base-browser-capture/jetty-avatar-256.png");
    }

    public static final Path pngPath;

    public static HttpEntity createTextFiles() throws IOException
    {
        String text = "text default\r\n";
        byte[] aTextFile = "Content of a.txt\n".getBytes(UTF_8);
        byte[] aHtmlFile = "<!DOCTYPE html><title>Content of a.html.</title>\r".getBytes(UTF_8);

        FormBodyPart aTextBody = FormBodyPartBuilder
                .create("file1", new ByteArrayBody(aTextFile, "a.txt"))
                .setField("Content-Type", ContentType.TEXT_PLAIN.withCharset(UTF_8).toString())
                .addField("X-SHA1", sha1sum(aTextFile))
                .build();

        FormBodyPart aHtmlBody = FormBodyPartBuilder
                .create("file2", new ByteArrayBody(aHtmlFile, "a.html"))
                .setField("Content-Type", ContentType.TEXT_HTML.withCharset(UTF_8).toString())
                .addField("X-SHA1", sha1sum(aHtmlFile))
                .build();

        HttpEntity entity = MultipartEntityBuilder
                .create()
                .setMode(HttpMultipartMode.RFC6532)
                .addTextBody("text", text, ContentType.TEXT_PLAIN.withCharset(UTF_8))
                .addPart(aTextBody)
                .addPart(aHtmlBody)
                .build();

        return entity;
    }

    public static HttpEntity createNested() throws IOException
    {
        String exampleBoundary = newSpecBoundaryString();

        HttpEntity entity = MultipartEntityBuilder
                .create()
                .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                .addTextBody("fruit", "banana")
                .addTextBody("color", "yellow")
                .addTextBody("cost", "$0.12 USG")
                .addTextBody("comments", "--" + exampleBoundary)
                .build();

        byte firstBuf[];
        try (ByteArrayOutputStream capture = new ByteArrayOutputStream())
        {
            entity.writeTo(capture);
            firstBuf = capture.toByteArray();
        }

        StringBody attachmentContent = new StringBody(new String(firstBuf, UTF_8), ContentType.TEXT_PLAIN);
        FormBodyPart attachment = FormBodyPartBuilder.create("attachment", attachmentContent)
                .build();

        HttpEntity entity2 = MultipartEntityBuilder
                .create()
                .addTextBody("reporter", "<user@company.com>")
                .addTextBody("timestamp", "2018-03-21T18:52:18+00:00")
                .addTextBody("comments", "this couldn't be parsed")
                .addPart(attachment) // as string
                .build();

        return entity2;
    }

    public static HttpEntity createNestedBinary() throws IOException
    {
        String exampleBoundary = newSpecBoundaryString();

        HttpEntity entity = MultipartEntityBuilder
                .create()
                .setMode(HttpMultipartMode.RFC6532)
                .addTextBody("fruit", "cherry")
                .addTextBody("color", "red")
                .addTextBody("cost", "$1.20 USG")
                .addTextBody("comments", "--" + exampleBoundary)
                .build();

        byte firstBuf[];
        try (ByteArrayOutputStream capture = new ByteArrayOutputStream())
        {
            entity.writeTo(capture);
            firstBuf = capture.toByteArray();
        }

        ContentBody attachmentContent = new ByteArrayBody(firstBuf, null);
        FormBodyPart attachment = FormBodyPartBuilder.create("attachment", attachmentContent)
                .build();

        HttpEntity entity2 = MultipartEntityBuilder
                .create()
                .addTextBody("reporter", "<user@company.com>")
                .addTextBody("timestamp", "2018-03-21T19:00:18+00:00")
                .addTextBody("comments", "this also couldn't be parsed")
                .addPart(attachment) // as binary
                .build();

        return entity2;
    }

    public static HttpEntity createBase64() throws IOException
    {
        NameValuePair namePair = new BasicNameValuePair("name", pngPath.getFileName().toString());
        Base64Content pngBase64 = new Base64Content(pngPath, ContentType.IMAGE_PNG.withParameters(namePair));

        FormBodyPart basePart = FormBodyPartBuilder.create("png", pngBase64)
                .addField("Content-ID", "<junk@company.com>")
                .build();

        HttpEntity entity = MultipartEntityBuilder
                .create()
                .addPart(basePart)
                .build();

        return entity;
    }

    public static HttpEntity createBase64Long() throws IOException
    {
        NameValuePair namePair = new BasicNameValuePair("name", pngPath.getFileName().toString());
        Base64Content pngBase64 = new Base64Content(pngPath, ContentType.IMAGE_PNG.withParameters(namePair), 0, Base64Content.CHUNK_SEP_LN);

        FormBodyPart basePart = FormBodyPartBuilder.create("png", pngBase64)
                .addField("Content-ID", "<junk@company.com>")
                .build();

        HttpEntity entity = MultipartEntityBuilder
                .create()
                .addPart(basePart)
                .build();

        return entity;
    }

    public static HttpEntity createNumberOnly() throws IOException
    {
        HttpEntity entity = MultipartEntityBuilder
                .create()
                .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                .addTextBody("pi", "3.14159265358979323846264338327950288419716939937510", ContentType.create("text/plain"))
                .build();

        return entity;
    }

    public static HttpEntity createNumberOnly2() throws IOException
    {
        HttpEntity entity = MultipartEntityBuilder
                .create()
                .setMode(HttpMultipartMode.RFC6532)
                .addTextBody("pi", "3.14159265358979323846264338327950288419716939937510", ContentType.TEXT_PLAIN.withCharset(""))
                .build();

        return entity;
    }

    public static HttpEntity createUnicodeNames() throws IOException
    {
        ContentType contentType = ContentType.TEXT_PLAIN.withCharset(UTF_8);

        HttpEntity entity = MultipartEntityBuilder
                .create()
                .setMode(HttpMultipartMode.RFC6532)
                .addTextBody("こんにちは世界", "Greetings 1", contentType)
                .addTextBody(urlEncode("こんにちは世界", UTF_8), "Greetings 2", contentType)
                .build();

        return entity;
    }

    public static HttpEntity createStrangeQuoting() throws IOException
    {
        ContentType contentType = ContentType.TEXT_PLAIN.withCharset(UTF_8);

        HttpEntity entity = MultipartEntityBuilder
                .create()
                .setMode(HttpMultipartMode.RFC6532)
                .addTextBody("and \"I\" quote", "Value 1", contentType)
                .addTextBody(urlEncode("and \"I\" quote", UTF_8), "Value 2", contentType)
                .addTextBody("value\"; what=\"whoa\"", "Value 3", contentType)
                .addTextBody("other\";\t\nwhat=\"Something\"", "Value 4", contentType)
                .build();

        return entity;
    }

    public static HttpEntity createZalgoTextPlain() throws IOException
    {
        String zalgo = "y͔͕͍o̪̞͎̥͇̤̕u'̛̰̫̳̰v̧̘̪̠̟̟e̥͈̱ ̥̠͇͎͕̜s̤e̺e̙ͅņ̜ ̲̟͝za̴͖̱̲͈̘l͖̖͓̙̮͔g͕̞͖͘o͕̤͈̗ ̯̲̹̲͓b͙͟e̞͎̜̗͈͉̭͞f̸or̰̩e̡̝̺,̸͕̙̥̼͇̜ ̪͇̹r̘̪ͅị͔̥͈ͅg̠̟̯͖̦͇ht͖̪͍͚̖͡?͙̝͖̞";

        HttpEntity entity = MultipartEntityBuilder
                .create()
                .setMode(HttpMultipartMode.RFC6532)
                .addTextBody("zalgo-8", zalgo, ContentType.TEXT_PLAIN.withCharset(UTF_8))
                .addTextBody("zalgo-16", zalgo, ContentType.TEXT_PLAIN.withCharset(UTF_16))
                .addTextBody("zalgo-16-be", zalgo, ContentType.TEXT_PLAIN.withCharset(UTF_16BE))
                .addTextBody("zalgo-16-le", zalgo, ContentType.TEXT_PLAIN.withCharset(UTF_16LE))
                .build();

        return entity;
    }

    public static HttpEntity createEncodingMess() throws IOException
    {
        String persian = "برج بابل";

        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder
                .create()
                .setMode(HttpMultipartMode.RFC6532);

        int count = 0;
        for (Charset charset : Charset.availableCharsets().values())
        {
            try
            {
                String name = "persian-" + urlEncode(charset.name(), UTF_8);
                StringBody body = new StringBody(persian, ContentType.TEXT_PLAIN.withCharset(charset));
                entityBuilder.addPart(name, body);
                count++;
            }
            catch (UnsupportedOperationException e)
            {
                // skip
            }
        }

        entityBuilder.addTextBody("count", Integer.toString(count));
        HttpEntity entity = entityBuilder.build();
        return entity;
    }

    public static HttpEntity createDuplicateNames() throws IOException
    {
        ContentType contentType = ContentType.TEXT_PLAIN.withCharset(UTF_8);

        HttpEntity entity = MultipartEntityBuilder
                .create()
                .setMode(HttpMultipartMode.RFC6532)
                .addTextBody("pi", "3.14159265358979323846264338327950288419716939937510", contentType)
                .addTextBody("pi", "3.14159", contentType)
                .addTextBody("pi", "3", contentType)
                .addTextBody("pi", "π", contentType)
                .addTextBody("pi", "π", ContentType.DEFAULT_BINARY.withCharset(UTF_16))
                .addTextBody("pi", urlEncode("π", UTF_8), ContentType.APPLICATION_FORM_URLENCODED.withCharset(""))
                .addTextBody("pi", "π = C/d", contentType)
                .addTextBody("π", "3.14", contentType)
                .addTextBody(urlEncode("π", UTF_8), "Approximately 3.14", contentType.withCharset(""))
                .addTextBody(urlEncode("π", UTF_16), "Approximately 3.14", contentType.withCharset(UTF_16))
                .build();

        return entity;
    }

    public static HttpEntity createCompany() throws IOException
    {
        HttpEntity entity = MultipartEntityBuilder
                .create()
                .addTextBody("company", urlEncode("bob & frank's shoe repair", UTF_8), ContentType.APPLICATION_FORM_URLENCODED.withCharset(UTF_8))
                .build();

        return entity;
    }

    public static HttpEntity createSjis() throws IOException
    {
        ContentType SJIS = ContentType.TEXT_PLAIN.withCharset("sjis");

        HttpEntity entity = MultipartEntityBuilder
                .create()
                .addTextBody("japanese", "健治", SJIS)
                .addTextBody("hello", "ャユ戆タ", SJIS)
                .build();

        return entity;
    }

    public static HttpEntity createUppercase() throws IOException
    {
        ContentType UPPER = ContentType.TEXT_PLAIN.withCharset("WINDOWS-1252");

        HttpEntity entity = MultipartEntityBuilder
                .create()
                .addTextBody("STATE", "TEXAS", UPPER)
                .addTextBody("CITY", "AUSTIN", UPPER)
                .build();

        return entity;
    }

    public static HttpEntity createComplex() throws IOException
    {
        HttpEntity entity = MultipartEntityBuilder
                .create()
                .addTextBody("pi", "3.14159265358979323846264338327950288419716939937510")
                .addTextBody("company", "bob & frank's shoe repair", ContentType.create("application/x-www-form-urlencoded"))
                .addBinaryBody("upload_file", pngPath.toFile(), ContentType.create("application/octet-stream"), "filename")
                .addTextBody("power", "\uAB35о\uD835\uDDCBⲥ\uD835\uDDBE", ContentType.TEXT_PLAIN.withCharset("utf-8"))
                .addTextBody("japanese", "健治", ContentType.TEXT_PLAIN.withCharset("SJIS"))
                .addTextBody("hello", "ャユ戆タ", ContentType.TEXT_PLAIN.withCharset("SJIS"))
                .build();

        return entity;
    }

    private static String urlEncode(String raw, Charset charset) throws UnsupportedEncodingException
    {
        return URLEncoder.encode(raw, charset.toString());
    }

    /**
     * Create a new Boundary String allowing only valid (per spec) characters
     */
    private static String newSpecBoundaryString()
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

    private static String sha1sum(byte buf[]) throws IOException
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
}
