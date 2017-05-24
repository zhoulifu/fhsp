package pers.zlf.fhsp;

import java.lang.reflect.Constructor;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import pers.zlf.fhsp.splitter.ChunkSizeEmitter;

@ChannelHandler.Sharable
public class ByteBufSplitter extends MessageToMessageDecoder<ByteBuf> {

    private ChunkSizeEmitter splitter;

    public ByteBufSplitter() {
        String clazz = System.getProperty("splitter",
                                          "pers.zlf.fhsp.splitter.DoubleChunkSizeEmitter");
        try {
            Constructor<?> constructor = Class.forName(clazz).getConstructor();
            this.splitter = (ChunkSizeEmitter) constructor.newInstance();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    protected void decode(
            ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        int size = 1;
        ByteBufAllocator allocator = ctx.alloc();

        for (;;) {
            if (size >= msg.readableBytes()) {
                out.add(msg.retain());
                break;
            } else {
                ByteBuf buf = allocator.buffer(size);
                buf.writeBytes(msg, size);
                out.add(buf);
                size = splitter.nextChunkSize(size, msg.readableBytes());
            }
        }
    }
}
