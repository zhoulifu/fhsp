package pers.zlf.fhsp.splitter;

public class DoubleChunkSizeEmitter implements ChunkSizeEmitter {

    @Override
    public int nextChunkSize(int current, int writable) {
        return current << 1;
    }
}
