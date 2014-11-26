package com.tencent.jflynn.dao;

import com.tencent.jflynn.domain.Artifact;

public interface ArtifactDao {
	public void insert(Artifact artifact);
	public Artifact queryById(String artifactId);
}
