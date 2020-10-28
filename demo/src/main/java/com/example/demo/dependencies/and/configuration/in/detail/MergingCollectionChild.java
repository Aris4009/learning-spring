package com.example.demo.dependencies.and.configuration.in.detail;

public class MergingCollectionChild extends MergingCollection {

	public MergingCollection init() {
		return new MergingCollectionChild();
	}
}
