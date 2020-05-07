package com.tenchael.cords.netty;

import com.tenchael.cords.CordsServer;
import com.tenchael.cords.protocol.Command;
import com.tenchael.cords.protocol.ErrorReply;
import com.tenchael.cords.protocol.InlineReply;
import com.tenchael.cords.protocol.Reply;
import com.tenchael.cords.utils.BytesKey;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.tenchael.cords.protocol.ErrorReply.NYI_REPLY;
import static com.tenchael.cords.protocol.StatusReply.QUIT;

public class CordsHandler extends SimpleChannelInboundHandler<Command> {


    private static final byte LOWER_DIFF = 'a' - 'A';
    private Map<BytesKey, Wrapper> methods = new HashMap<BytesKey, Wrapper>();


    public CordsHandler(final CordsServer cs) {
        Class<? extends CordsServer> aClass = cs.getClass();
        for (final Method method : aClass.getMethods()) {
            final Class<?>[] types = method.getParameterTypes();
            methods.put(new BytesKey(method.getName().getBytes()), new Wrapper() {
                @Override
                public Reply execute(Command command) throws CordsException {
                    Object[] objects = new Object[types.length];
                    try {
                        command.toArguments(objects, types);
                        return (Reply) method.invoke(cs, objects);
                    } catch (IllegalAccessException e) {
                        throw new CordsException("Invalid server implementation");
                    } catch (InvocationTargetException e) {
                        Throwable te = e.getTargetException();
                        if (!(te instanceof CordsException)) {
                            te.printStackTrace();
                        }
                        return new ErrorReply("ERR " + te.getMessage());
                    } catch (Exception e) {
                        return new ErrorReply("ERR " + e.getMessage());
                    }
                }
            });
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command msg)
            throws Exception {
        byte[] name = msg.getName();
        for (int i = 0; i < name.length; i++) {
            byte b = name[i];
            if (b >= 'A' && b <= 'Z') {
                name[i] = (byte) (b + LOWER_DIFF);
            }
        }
        Wrapper wrapper = methods.get(new BytesKey(name));

        Reply reply;
        if (wrapper == null) {
            reply = new ErrorReply("unknown command '" + new String(name, StandardCharsets.US_ASCII) + "'");
        } else {
            reply = wrapper.execute(msg);
        }
        if (reply == QUIT) {
            ctx.close();
        } else {
            if (msg.isInline()) {
                if (reply == null) {
                    reply = new InlineReply(null);
                } else {
                    reply = new InlineReply(reply.data());
                }
            }
            if (reply == null) {
                reply = NYI_REPLY;
            }
            ctx.write(reply);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx)
            throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    interface Wrapper {
        Reply execute(Command command) throws CordsException;
    }
}
