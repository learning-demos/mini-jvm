package com.github.dieselniu;

public class BranchClass {
	public static void main(String[] args) {
		System.out.println(foo(111));
	}

	private static int foo(int i) {
		if (i % 2 == 0) {
			return 100;
		} else {
			return 200;
		}
	}
}
