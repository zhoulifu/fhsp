package pers.zlf.fhsp.splitter;

public interface ChunkSizeEmitter {
    int nextChunkSize(int current, int writable);
}
