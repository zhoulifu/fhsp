package pers.zlf.fhsp.splitter;

public class NoOpChunkSizeEmitter implements ChunkSizeEmitter {
    @Override
    public int nextChunkSize(int current, int writable) {
        return writable;
    }
}
