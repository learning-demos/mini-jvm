package com.github.dieselniu;

import com.github.zxh.classpy.classfile.ClassFileParser;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MiniJVMClassLoader {
	public static final MiniJVMClassLoader BOOTSTRAP_CLASSLOADER = new MiniJVMClassLoader(new String[]{System.getProperty("java.home") + "/lib/rt.jar"}, null);
	public static final MiniJVMClassLoader EXT_CLASSLOADER = new MiniJVMClassLoader(
		Stream.of(Objects.requireNonNull(new File(System.getProperty("java.home") + "/lib/ext").listFiles()))
			.filter(File::isFile)
			.map(File::getName)
			.filter(name -> name.endsWith(".jar"))
			.toArray(String[]::new), BOOTSTRAP_CLASSLOADER);
	// Bootstrap类加载器  rt.jar
	// Ext类加载器  ext/
	// 应用类加载器  -classpath
	private Map<String, MiniJVMClass> loadedClasses = new ConcurrentHashMap<>();
	//null为启动类加载器
	private MiniJVMClassLoader parent;

	// 对于bootstrap加载器来说它是rt.jar
	// 对于Ext类加载器来说它是 ext/ 目录下的所有jarbao
	// 对于AppClassLoader 它是 -classPath传入的东西

	private String[] classPath;

	public MiniJVMClassLoader(String[] classPath, MiniJVMClassLoader parent) {
		this.parent = parent;
		this.classPath = classPath;
	}

	public MiniJVMClassLoader() {
	}

	public MiniJVMClass loadClass(String className) throws ClassNotFoundException {
		if (loadedClasses.containsKey(className)) {
			return loadedClasses.get(className);
		}

		MiniJVMClass result = null;

		if (parent == null) {
			result = findAndDefineClass(className);
		} else {
			result = parent.loadClass(className);
		}

		if (result == null && this.parent != null) {
			result = findAndDefineClass(className);
		}

		loadedClasses.put(className, result);
		return result;
	}

	private MiniJVMClass findAndDefineClass(String className) throws ClassNotFoundException {
		byte[] bytes = findClassBytes(className);
		return defineClass(bytes);
	}

	private MiniJVMClass defineClass(byte[] bytes) {
		return new MiniJVMClass(this, new ClassFileParser().parse(bytes));
	}

	private byte[] findClassBytes(String className) throws ClassNotFoundException {
		String path = className.replace(".", "/").concat(".java");
		for (String entry : classPath) {
			if (new File(entry).isDirectory()) {
				try {
					return Files.readAllBytes(new File(entry, path).toPath());
				} catch (IOException ignored) {
				}
			} else if (entry.endsWith(".jar")) {
				try {
					return readBytesFromJar(entry, path);
				} catch (IOException ignored) {

				}
			}
		}
		throw new ClassNotFoundException();
	}

	private byte[] readBytesFromJar(String jar, String path) throws IOException {
		ZipFile zipFile = new ZipFile(jar);
		ZipEntry entry = zipFile.getEntry(path);
		if (entry == null) {
			throw new IOException("Not found" + path);
		}

		InputStream inputStream = zipFile.getInputStream(entry);
		return IOUtils.toByteArray(inputStream);

	}

}

