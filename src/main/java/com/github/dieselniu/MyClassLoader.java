package com.github.dieselniu;

public class MyClassLoader extends MiniJVMClassLoader {
	public MyClassLoader() {
		super();
	}

	public MyClassLoader(String[] classPath, MiniJVMClassLoader parent) {
		super(classPath, parent);
	}

	public MiniJVMClass load(String s) {
		return null;
	}
}
