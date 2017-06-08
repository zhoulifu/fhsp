package pers.zlf.fhsp.splitter;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public abstract class ByteBufSplitter extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected void decode(
            ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        int chunkSize = 2;
        ByteBuf chunk;

        for (;;) {
            if (chunkSize >= msg.readableBytes()) {
                out.add(msg.retain());
                break;
            }

            chunk = msg.readRetainedSlice(chunkSize);
            out.add(chunk);
            chunkSize = nextChunkSize(chunkSize, msg);
        }
    }

    protected abstract int nextChunkSize(int lastChunkSize, ByteBuf remaining);
}
