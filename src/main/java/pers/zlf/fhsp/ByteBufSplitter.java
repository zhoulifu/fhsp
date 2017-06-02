package pers.zlf.fhsp;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import pers.zlf.fhsp.config.Configuration;
import pers.zlf.fhsp.splitter.ChunkSizeEmitter;
import pers.zlf.fhsp.splitter.DoubleChunkSizeEmitter;

@ChannelHandler.Sharable
public class ByteBufSplitter extends ByteToMessageDecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(ByteBufSplitter.class);
    private static final ByteBufSplitter INSTANCE = new ByteBufSplitter();

    private ChunkSizeEmitter splitter;

    private ByteBufSplitter() {
        try {
            this.splitter = (ChunkSizeEmitter) Class.forName(Configuration.splitter())
                                                    .newInstance();
        } catch (Throwable t) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Failed to create instance of {}, using default splitter {}",
                            Configuration.splitter(),
                            DoubleChunkSizeEmitter.class.getSimpleName());
            }
            this.splitter = new DoubleChunkSizeEmitter();
        }
    }

    public static ByteBufSplitter getInstance() {
        return INSTANCE;
    }

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
            chunkSize = splitter.nextChunkSize(chunkSize, msg.readableBytes());
        }
    }
}
