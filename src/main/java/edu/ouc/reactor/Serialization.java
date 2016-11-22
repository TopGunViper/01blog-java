package edu.ouc.reactor;

import java.io.IOException;

public interface Serialization {
	
	public byte[] serialize(Object src) throws IOException;

	public Object deserialize(byte[] src, Class<?> cls) throws IOException, ClassNotFoundException;
}
