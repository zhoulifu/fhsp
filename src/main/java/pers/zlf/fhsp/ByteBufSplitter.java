package pers.zlf.fhsp;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import pers.zlf.fhsp.config.Configuration;
import pers.zlf.fhsp.splitter.ChunkSizeEmitter;

@ChannelHandler.Sharable
public class ByteBufSplitter extends MessageToMessageDecoder<ByteBuf> {
    private static final ByteBufSplitter INSTANCE = new ByteBufSplitter();

    private ChunkSizeEmitter splitter;

    private ByteBufSplitter() {
        try {
            this.splitter = (ChunkSizeEmitter) Class.forName(Configuration.splitter())
                                                    .newInstance();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static ByteBufSplitter getInstance() {
        return INSTANCE;
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
