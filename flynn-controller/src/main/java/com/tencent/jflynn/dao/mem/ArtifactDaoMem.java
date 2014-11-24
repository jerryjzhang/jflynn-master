package com.tencent.jflynn.dao.mem;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.tencent.jflynn.dao.ArtifactDao;
import com.tencent.jflynn.domain.Artifact;

@Repository
public class ArtifactDaoMem implements ArtifactDao {
	private Map<String, Artifact> idToArtifact = new HashMap<String, Artifact>();
	
	public void insert(Artifact artifact){
		idToArtifact.put(artifact.getId(), artifact);
	}
}
