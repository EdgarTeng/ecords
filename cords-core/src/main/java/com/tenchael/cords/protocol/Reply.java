package com.tenchael.cords.protocol;

import com.tenchael.cords.netty.CordsCommandDecoder;
import io.netty.buffer.ByteBuf;

import java.io.IOException;

public interface Reply<T> {
    byte[] CRLF = new byte[]{CordsCommandDecoder.CR, CordsCommandDecoder.LF};

    T data();

    void write(ByteBuf os) throws IOException;
}
