package net.jmb19905.jmbnetty.common.buffer;

public interface BufferSerializable {
    void construct(SimpleBuffer buffer);
    void deconstruct(SimpleBuffer buffer);
}
