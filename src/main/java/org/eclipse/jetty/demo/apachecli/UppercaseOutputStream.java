package org.eclipse.jetty.demo.apachecli;

import java.io.IOException;
import java.io.OutputStream;

public class UppercaseOutputStream extends OutputStream
{
    private final OutputStream out;
    int offset = 'A' - 'a';

    public UppercaseOutputStream(OutputStream out)
    {
        this.out = out;
    }

    @Override
    public void write(int b) throws IOException
    {
        if ((b >= 'a') && (b <= 'z'))
            out.write((byte) (b + offset));
        else
            out.write(b);
    }
}
