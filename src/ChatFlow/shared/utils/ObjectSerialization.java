package ChatFlow.shared.utils;

import java.io.*;

public class ObjectSerialization {

    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(obj);
        out.flush();
        return byteOut.toByteArray();
    }

    public static <T> T deserialize(byte[] data, Class<T> classType) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
        ObjectInputStream in = new ObjectInputStream(byteIn);
        return classType.cast(in.readObject());
    }
}
