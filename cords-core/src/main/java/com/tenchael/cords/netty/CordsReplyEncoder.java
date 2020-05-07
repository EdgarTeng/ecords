package com.tenchael.cords.netty;

import com.tenchael.cords.protocol.Reply;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Write a reply.
 */
public class CordsReplyEncoder extends MessageToByteEncoder<Reply> {
    @Override
    public void encode(ChannelHandlerContext ctx, Reply msg, ByteBuf out) throws Exception {
        msg.write(out);
    }
}
