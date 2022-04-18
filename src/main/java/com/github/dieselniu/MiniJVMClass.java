package com.github.dieselniu;

import com.github.zxh.classpy.classfile.ClassFile;

import javax.print.DocFlavor;

class MiniJVMClass {
	private MiniJVMClassLoader classLoader;
	private ClassFile classFile;


	public MiniJVMClass(MiniJVMClassLoader classLoader, ClassFile classFile) {
		this.classLoader = classLoader;
		this.classFile = classFile;
	}

	public MiniJVMClassLoader getClassLoader() {
		return classLoader;
	}

	public Object newInstance() {
		return null;
	}
}
