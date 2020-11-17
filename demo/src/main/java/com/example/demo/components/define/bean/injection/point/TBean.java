package com.example.demo.components.define.bean.injection.point;

import java.util.List;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component(value = "tBean")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TBean {

	private List<String> list;

	public TBean(List<String> list) {
		this.list = list;
	}

	@Override
	public String toString() {
		return "TBean{" + "list=" + list + '}';
	}

	public List<String> getList() {
		return list;
	}
}
