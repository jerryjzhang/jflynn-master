package com.tencent.jflynn.dao.mem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.tencent.jflynn.dao.ReleaseDao;
import com.tencent.jflynn.domain.Program;
import com.tencent.jflynn.domain.Release;

@Repository
public class ReleaseDaoMem implements ReleaseDao {
	private final Map<String, Release> idToRelease = 
			new HashMap<String, Release>();
	
	public void insert(Release release){
		idToRelease.put(release.getId(), release);
	}
	
	public Release queryById(String id){
		return idToRelease.get(id);
	}
	
	public List<Release> queryByAppId(String appId){
		List<Release> releases = new ArrayList<Release>();
		
		for(Release release : idToRelease.values()){
			if(release.getAppID().equals(appId)){
				releases.add(release);
			}
		}
		
		return releases;
	}
	
	public Release queryByAppIdAndVersion(String appId, int version) {
		Release release = null;
		
		for(Release r : idToRelease.values()){
			if(r.getAppID().equals(appId)
					&& r.getVersion() == version){
				release = r;
				break;
			}
		}
		
		return release;
	}
	
	public List<Program> queryPrograms(String releaseId){
		Release release = idToRelease.get(releaseId);
		
		return new ArrayList<Program>(release.getPrograms().values());
	}
}
