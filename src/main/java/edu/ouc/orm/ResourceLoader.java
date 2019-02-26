package edu.ouc.orm;

import java.io.InputStream;

/**
 *
 * @author wqx
 *
 */
public final class ResourceLoader {
	
	public static InputStream getResourceAsStream(String location){
		InputStream ins = null;
		ClassLoader cl = getClassLoader();
		if(cl != null){
			ins = cl.getResourceAsStream(location);
		}
		return ins;
	}
	
	public static ClassLoader getClassLoader() {
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		}
		catch (Throwable ex) {}
		if (cl == null) {
			cl = ResourceLoader.class.getClassLoader();
			if (cl == null) {
				try {
					cl = ClassLoader.getSystemClassLoader();
				}
				catch (Throwable ex) {}
			}
		}
		return cl;
	}
}
