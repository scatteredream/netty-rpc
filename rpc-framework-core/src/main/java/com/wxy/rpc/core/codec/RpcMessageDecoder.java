package com.wxy.rpc.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author Wuxy
 * @version 1.0
 * {@code ClassName} RpcMessageDecoder
 * {@code Date} 2025/1/5 12:41
 */
public class RpcMessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // todo: implement this method
    }
}
