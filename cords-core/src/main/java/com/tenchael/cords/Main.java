package com.tenchael.cords;

import com.tenchael.cords.netty.CordsCommandDecoder;
import com.tenchael.cords.netty.CordsHandler;
import com.tenchael.cords.netty.CordsReplyEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Main {


    public static void main(String[] args) throws InterruptedException {
        int port = 8989;
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup(5);

        CordsServer cordsServer = new SimpleCordsServer();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new CordsCommandDecoder());
                        pipeline.addLast(new CordsReplyEncoder());
                        pipeline.addLast(new CordsHandler(cordsServer));
                    }
                }).option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        ChannelFuture f = bootstrap.bind(port).sync();
        System.out.println("server listen on port: " + port);
        f.channel().closeFuture().sync();

    }

}
