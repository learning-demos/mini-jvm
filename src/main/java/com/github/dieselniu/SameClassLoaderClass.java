package com.github.dieselniu;

public class SameClassLoaderClass {
	public static void main(String[] args) {
		MyClassLoader myClassLoader = new MyClassLoader();
		MiniJVMClass aClass = myClassLoader.load("com.github.dieselniu.SimpleClass");


		SimpleClass simpleClass = (SimpleClass) aClass.newInstance();

	}
}
