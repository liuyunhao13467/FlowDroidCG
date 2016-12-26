package com.wang.flowdroid.entities;

public class MyEdge {

	protected Integer ip;
	protected Integer src;
	protected Integer tgt;
	protected StringBuilder conditions;

	public MyEdge(Integer ip, Integer src, Integer tgt) {
		this.ip = ip;
		this.src = src;
		this.tgt = tgt;
	}
	
	public void setConditions(StringBuilder conditions){
		this.conditions = conditions;
	}

	public Integer getSrc() {
		return src;
	}

	public Integer getTgt() {
		return tgt;
	}

	public Integer getIp() {
		return ip;
	}
	
	public StringBuilder getConditions(){
		return this.conditions;
	}

}
