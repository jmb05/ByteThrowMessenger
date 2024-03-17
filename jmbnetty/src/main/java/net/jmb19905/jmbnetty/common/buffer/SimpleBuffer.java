package net.jmb19905.jmbnetty.common.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import net.jmb19905.util.Logger;
import net.jmb19905.util.crypto.Encryption;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class SimpleBuffer {

    private static final PooledByteBufAllocator allocator = new PooledByteBufAllocator();

    private final ByteBuf buffer;

    public static SimpleBuffer allocate(int initialCap) {
        return new SimpleBuffer(allocator.buffer(initialCap));
    }

    public static SimpleBuffer allocate(int initialCap, ByteBufAllocator alloc) {
        return new SimpleBuffer(alloc.buffer(initialCap));
    }

    public SimpleBuffer(ByteBuf buffer) {
        this.buffer = buffer;
    }

    public void putInt(int i) {
        buffer.writeInt(i);
    }

    public int getInt() {
        return buffer.readInt();
    }

    public void putLong(long i) {
        buffer.writeLong(i);
    }

    public long getLong() {
        return buffer.readLong();
    }

    public void putString(String s) {
        if (s == null) {
            putBytes(new byte[]{});
            return;
        }
        putBytes(s.getBytes(StandardCharsets.UTF_8));
    }

    public String getString() {
        byte[] bytes = getBytes();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public void putBoolean(boolean b) {
        buffer.writeBoolean(b);
    }

    public boolean getBoolean() {
        return buffer.readBoolean();
    }

    public void putUUID(UUID id) {
        putLong(id.getMostSignificantBits());
        putLong(id.getLeastSignificantBits());
    }

    public UUID getUUID() {
        long mostSigBits = getLong();
        long leastSigBits = getLong();
        return new UUID(mostSigBits, leastSigBits);
    }

    public void putBytes(byte[] bytes) {
        putInt(bytes.length);
        buffer.writeBytes(bytes);
    }

    public byte[] getBytes() {
        int length = getInt();
        byte[] data = new byte[length];
        buffer.readBytes(data);
        return data;
    }

    public void put(BufferSerializable obj) {
        obj.deconstruct(this);
    }

    public <T extends BufferSerializable> T get(Class<T> objClass) {
        T obj = null;
        try {
            Constructor<T> constructor = objClass.getConstructor();
            obj = constructor.newInstance();
            obj.construct(this);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            Logger.error(e);
        }
        return obj;
    }

    public void putArray(BufferSerializable[] array) {
        BufferSerializable[] putArray = Arrays.stream(array).filter(Objects::nonNull).toArray(BufferSerializable[]::new);
        buffer.writeByte(putArray.length);
        Arrays.stream(putArray).forEach(this::put);
    }

    @SuppressWarnings("unchecked")
    public <T extends BufferSerializable> T[] getArray(Class<T> objClass) {
        byte length = buffer.readByte();

        T[] objs = (T[]) Array.newInstance(objClass, length);
        for(int i=0;i<length;i++) {
            objs[i] = get(objClass);
        }
        return objs;
    }

    public byte[] toByteArray() {
        byte[] rawData = new byte[buffer.readableBytes()];
        buffer.readBytes(rawData);
        return rawData;
    }

    public void encrypt(Encryption encryption) {
        if(encryption == null || !encryption.isUsable()) return;
        byte[] data = toByteArray();
        data = encryption.encrypt(data);
        buffer.clear();
        buffer.writeBytes(data);
    }

    public void decrypt(Encryption encryption) {
        if(encryption == null || !encryption.isUsable()) return;
        byte[] data = toByteArray();
        try {
            data = encryption.decrypt(data);
        } catch (IllegalArgumentException ex) {
            Logger.warn("IllegalArgumentException while decrypting: " + ex.getMessage());
        }
        buffer.clear();
        buffer.writeBytes(data);
    }

    public ByteBuf getBuffer() {
        return buffer;
    }

    public int getSize() {
        return buffer.capacity();
    }

}
