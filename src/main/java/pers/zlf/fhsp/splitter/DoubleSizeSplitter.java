package pers.zlf.fhsp.splitter;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;

@Sharable
public class DoubleSizeSplitter extends ByteBufSplitter {

    @Override
    protected int nextChunkSize(int lastChunkSize, ByteBuf remaining) {
        return lastChunkSize << 1;
    }
}
