package com.tenchael.cords;

import com.tenchael.cords.netty.CordsException;
import com.tenchael.cords.protocol.BulkReply;
import com.tenchael.cords.protocol.IntegerReply;
import com.tenchael.cords.protocol.MultiBulkReply;
import com.tenchael.cords.protocol.StatusReply;
import com.tenchael.cords.utils.BytesKeyObjectMap;

import static com.tenchael.cords.protocol.BulkReply.NIL_REPLY;

public class SimpleCordsServer implements CordsServer {
    private static final StatusReply PONG = new StatusReply("PONG");

    private BytesKeyObjectMap<Object> data = new BytesKeyObjectMap<>();

    private static CordsException invalidValue() {
        return new CordsException("Operation against a key holding the wrong kind of value");
    }

    @Override
    public BulkReply get(byte[] key0) throws CordsException {
        Object o = data.get(key0);
        if (o instanceof byte[]) {
            return new BulkReply((byte[]) o);
        }
        if (o == null) {
            return NIL_REPLY;
        } else {
            throw invalidValue();
        }
    }

    @Override
    public StatusReply set(byte[] key0, byte[] value1) throws CordsException {
        data.put(key0, value1);
        return StatusReply.OK;
    }

    @Override
    public BulkReply echo(byte[] message0) throws CordsException {
        return new BulkReply(message0);
    }

    @Override
    public StatusReply ping() throws CordsException {
        return PONG;
    }

    @Override
    public StatusReply quit() throws CordsException {
        return StatusReply.QUIT;
    }

    @Override
    public IntegerReply del(byte[][] key0) throws CordsException {
        return null;
    }

    @Override
    public IntegerReply exists(byte[] key0) throws CordsException {
        return null;
    }

    @Override
    public MultiBulkReply keys(byte[] pattern0) throws CordsException {
        return null;
    }
}
