package pers.zlf.fhsp.splitter;

import io.netty.buffer.ByteBuf;

public class NoopSplitter extends ByteBufSplitter {

    @Override
    protected int nextChunkSize(int lastChunkSize, ByteBuf remaining) {
        return remaining.readableBytes();
    }
}
