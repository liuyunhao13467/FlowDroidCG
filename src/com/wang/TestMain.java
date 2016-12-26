package com.wang;

public class TestMain {

	public static void main(String[] args) {

		C(1);
	}

	public static void A(){
		System.out.println("inside A");
	}
	public static void B(){
		System.out.println("inside B");
	}
	public static void C(int i ){
		if(i > 1){
			A();
		}else{
			B();
		}
		int data =0;
		System.out.println("hello world!!");
	}
}
