package com.tenchael.cords.netty;

import com.tenchael.cords.protocol.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Decode commands.
 */
public class CordsCommandDecoder extends ReplayingDecoder<Void> {

    public static final char CR = '\r';
    public static final char LF = '\n';
    private static final char ZERO = '0';

    private byte[][] bytes;
    private int arguments = 0;


    private MultiBulkReply reply;

    private boolean checkpointEnabled;


    public CordsCommandDecoder() {
        this(false);
    }

    public CordsCommandDecoder(boolean checkpointEnabled) {
        this.checkpointEnabled = checkpointEnabled;
    }

    public static long readLong(ByteBuf is) throws IOException {
        long size = 0;
        int sign = 1;
        int read = is.readByte();
        if (read == '-') {
            read = is.readByte();
            sign = -1;
        }
        do {
            if (read == CR) {
                if (is.readByte() == LF) {
                    break;
                }
            }
            int value = read - ZERO;
            if (value >= 0 && value < 10) {
                size *= 10;
                size += value;
            } else {
                throw new IOException("Invalid character in integer");
            }
            read = is.readByte();
        } while (true);
        return size * sign;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (bytes != null) {
            int numArgs = bytes.length;
            for (int i = arguments; i < numArgs; i++) {
                if (in.readByte() == '$') {
                    long l = readLong(in);
                    if (l > Integer.MAX_VALUE) {
                        throw new IllegalArgumentException("Java only supports arrays up to " + Integer.MAX_VALUE + " in size");
                    }
                    int size = (int) l;
                    bytes[i] = new byte[size];
                    in.readBytes(bytes[i]);
                    if (in.bytesBefore((byte) '\r') != 0) {
                        throw new CordsException("Argument doesn't end in CRLF");
                    }
                    in.skipBytes(2);
                    arguments++;
                    checkpoint();
                } else {
                    throw new IOException("Unexpected character");
                }
            }
            try {
                out.add(new Command(bytes));
            } finally {
                bytes = null;
                arguments = 0;
            }
        } else if (in.readByte() == '*') {
            long l = readLong(in);
            if (l > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Java only supports arrays up to " + Integer.MAX_VALUE + " in size");
            }
            int numArgs = (int) l;
            if (numArgs < 0) {
                throw new CordsException("Invalid size: " + numArgs);
            }
            bytes = new byte[numArgs][];
            checkpoint();
            decode(ctx, in, out);
        } else {
            // Go backwards one
            in.readerIndex(in.readerIndex() - 1);
            // Read command -- can't be interupted
            byte[][] b = new byte[1][];
            b[0] = in.readBytes(in.bytesBefore((byte) '\r')).array();
            in.skipBytes(2);
            out.add(new Command(b, true));
        }
    }

    public ByteBuf readBytes(ByteBuf is) throws IOException {
        long l = readLong(is);
        if (l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Java only supports arrays up to " + Integer.MAX_VALUE + " in size");
        }
        int size = (int) l;
        if (size == -1) {
            return null;
        }
        ByteBuf buffer = is.readSlice(size);
        int cr = is.readByte();
        int lf = is.readByte();
        if (cr != CR || lf != LF) {
            throw new IOException("Improper line ending: " + cr + ", " + lf);
        }
        return buffer;
    }

    public Reply receive(final ByteBuf is) throws IOException {
        if (reply != null) {
            return decodeMultiBulkReply(is);
        }
        return readReply(is);
    }

    public Reply readReply(ByteBuf is) throws IOException {
        int code = is.readByte();
        switch (code) {
            case StatusReply.MARKER: {
                String status = is.readBytes(is.bytesBefore((byte) '\r')).toString(StandardCharsets.UTF_8);
                is.skipBytes(2);
                return new StatusReply(status);
            }
            case ErrorReply.MARKER: {
                String error = is.readBytes(is.bytesBefore((byte) '\r')).toString(StandardCharsets.UTF_8);
                is.skipBytes(2);
                return new ErrorReply(error);
            }
            case IntegerReply.MARKER: {
                return new IntegerReply(readLong(is));
            }
            case BulkReply.MARKER: {
                return new BulkReply(readBytes(is));
            }
            case MultiBulkReply.MARKER: {
                if (reply == null) {
                    return decodeMultiBulkReply(is);
                } else {
                    return new CordsCommandDecoder(false).decodeMultiBulkReply(is);
                }
            }
            default: {
                throw new IOException("Unexpected character in stream: " + code);
            }
        }
    }

    @Override
    public void checkpoint() {
        if (checkpointEnabled) {
            super.checkpoint();
        }
    }

    public MultiBulkReply decodeMultiBulkReply(ByteBuf is) throws IOException {
        try {
            if (reply == null) {
                reply = new MultiBulkReply();
                checkpoint();
            }
            reply.read(this, is);
            return reply;
        } finally {
            reply = null;
        }
    }

}
