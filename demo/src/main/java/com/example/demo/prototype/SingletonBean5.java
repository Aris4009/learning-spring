package com.example.demo.prototype;

public class SingletonBean5 {

	private PrototypeBeanScopedProxy prototypeBeanScopedProxy;

	public PrototypeBeanScopedProxy getPrototypeBeanScopedProxy() {
		return prototypeBeanScopedProxy;
	}

	public void setPrototypeBeanScopedProxy(PrototypeBeanScopedProxy prototypeBeanScopedProxy) {
		this.prototypeBeanScopedProxy = prototypeBeanScopedProxy;
	}

	@Override
	public String toString() {
		return "SingletonBean5{" + "prototypeBeanScopedProxy=" + prototypeBeanScopedProxy + '}';
	}
}
