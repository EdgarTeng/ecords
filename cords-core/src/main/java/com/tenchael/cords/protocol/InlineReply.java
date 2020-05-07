package com.tenchael.cords.protocol;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.tenchael.cords.utils.Encoding.numToBytes;

/**
 * Return the reply inline when you get an inline message.
 */
public class InlineReply implements Reply<Object> {

    private final Object obj;

    public InlineReply(Object obj) {
        this.obj = obj;
    }

    @Override
    public Object data() {
        return obj;
    }

    @Override
    public void write(ByteBuf os) throws IOException {
        if (obj == null) {
            os.writeBytes(CRLF);
        } else if (obj instanceof String) {
            os.writeByte('+');
            os.writeBytes(((String) obj).getBytes(StandardCharsets.US_ASCII));
            os.writeBytes(CRLF);
        } else if (obj instanceof ByteBuf) {
            os.writeByte('+');
            os.writeBytes(((ByteBuf) obj).array());
            os.writeBytes(CRLF);
        } else if (obj instanceof byte[]) {
            os.writeByte('+');
            os.writeBytes((byte[]) obj);
            os.writeBytes(CRLF);
        } else if (obj instanceof Long) {
            os.writeByte(':');
            os.writeBytes(numToBytes((Long) obj, true));
        } else {
            os.writeBytes("ERR invalid inline response".getBytes(StandardCharsets.US_ASCII));
            os.writeBytes(CRLF);
        }
    }
}
