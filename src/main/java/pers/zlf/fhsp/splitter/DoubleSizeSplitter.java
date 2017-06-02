package pers.zlf.fhsp.splitter;

import io.netty.buffer.ByteBuf;

public class DoubleSizeSplitter extends ByteBufSplitter {

    @Override
    protected int nextChunkSize(int lastChunkSize, ByteBuf remaining) {
        return lastChunkSize << 1;
    }
}
