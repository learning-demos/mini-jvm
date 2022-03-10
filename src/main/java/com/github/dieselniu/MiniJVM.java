package com.github.dieselniu;

import com.github.zxh.classpy.classfile.ClassFile;
import com.github.zxh.classpy.classfile.ClassFileParser;
import com.github.zxh.classpy.classfile.MethodInfo;
import com.github.zxh.classpy.classfile.bytecode.Instruction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Stack;
import java.util.stream.Stream;

/**
 * 这是一个用来学习的JVM
 */
public class MiniJVM {
	private String mainClass;
	private String[] classPathEntries;

	/**
	 * 创建一个miniJVM，使用指定的classpath，和main class
	 *
	 * @param classPath 启动时的classpath，使用{@Link java.io.File.pathSeparator},只支持文件夹
	 * @param mainClass
	 */
	public MiniJVM(String classPath, String mainClass) {
		this.mainClass = mainClass;
		this.classPathEntries = classPath.split(File.pathSeparator);
	}

	public static void main(String[] args) {
		new MiniJVM("target/classes", "com.github.dieselniu.SimpleClass").start();
	}


	/**
	 * 启动并且运行虚拟机
	 */
	public void start() {
		ClassFile mainClassFile = loadClassFromClassPath(mainClass);
		MethodInfo methodInfo = mainClassFile.getMethod("main").get(0);
		Stack<StackFrame> methodStack = new Stack<>();
		Object[] localVariablesForMainStackFrame = new Object[methodInfo.getMaxStack()];
		localVariablesForMainStackFrame[0] = null;

		methodStack.push(new StackFrame(localVariablesForMainStackFrame, methodInfo));


		PCRegister pcRegister = new PCRegister(methodStack);
		while (true) {
			Instruction instruction = pcRegister.getNextInstruction();
			if (instruction == null) {
				break;
			}
			switch (instruction.getOpcode()) {
				case getstatic:
				case invokestatic:
				case invokevirtual:
				case _return:
				default:
					throw new IllegalStateException("Opcode" + instruction + " not implemented yet!");
			}
		}

	}

	// fqcn is fullQualifiedClassName
	private ClassFile loadClassFromClassPath(String fqcn) {
		return Stream.of(classPathEntries).map(entry -> tryLoad(entry, fqcn))
			.filter(Objects::nonNull)
			.findFirst()
			.orElseThrow(() -> new RuntimeException(new ClassNotFoundException(fqcn)));
	}

	private ClassFile tryLoad(String entry, String fqcn) {
		try {
			byte[] bytes = Files.readAllBytes(new File(entry, fqcn.replace(".", "/") + ".class").toPath());
			return new ClassFileParser().parse(bytes);
		} catch (IOException exception) {
			return null;
		}
	}

	static class PCRegister {
		Stack<StackFrame> methodStack;

		public PCRegister(Stack<StackFrame> methodStack) {
			this.methodStack = methodStack;
		}

		public Instruction getNextInstruction() {
			if (methodStack.isEmpty()) {
				return null;
			} else {
				StackFrame frameAtTop = methodStack.peek();
				return frameAtTop.getInstruction();
			}
		}
	}

	static class StackFrame {
		Object[] localVariables;
		Stack<Objects> operandStack;
		MethodInfo methodInfo;
		int currentInstructionIndex;

		public Instruction getInstruction() {
			return methodInfo.getCode().get(currentInstructionIndex++);
		}

		public StackFrame(Object[] localVariables, MethodInfo methodInfo) {
			this.localVariables = localVariables;
			this.methodInfo = methodInfo;
		}
	}
}
