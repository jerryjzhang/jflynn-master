package com.tencent.jflynn.domain;

import java.sql.Timestamp;

public class Artifact {
	private String id;
	private String uri;
	private Timestamp createTime;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	@Override
	public String toString() {
		return "Artifact [id=" + id + ", uri=" + uri + ", createTime="
				+ createTime + "]";
	}
}
