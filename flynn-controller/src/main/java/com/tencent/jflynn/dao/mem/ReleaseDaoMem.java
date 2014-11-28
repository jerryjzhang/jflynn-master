package com.tencent.jflynn.dao.mem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.tencent.jflynn.dao.ReleaseDao;
import com.tencent.jflynn.domain.Release;

@Repository
public class ReleaseDaoMem implements ReleaseDao {
	private final Map<String, Release> idToRelease = new HashMap<String, Release>();
	private final Map<String, Map<Integer, Release>> appVersionReleases = 
		new HashMap<String, Map<Integer, Release>>();
	
	public void insert(Release release){
		idToRelease.put(release.getId(), release);
		
		Map<Integer, Release> versionReleases = appVersionReleases.get(release.getAppID());
		if (versionReleases == null) {
			versionReleases = new HashMap<Integer, Release>();
			appVersionReleases.put(release.getAppID(), versionReleases);
		}
		
		versionReleases.put(release.getVersion(), release);		
	}
	
	public Release queryById(String id){
		return idToRelease.get(id);
	}
	
	public List<Release> queryByAppId(String appID){
		List<Release> releases = new ArrayList<Release>();
		
		for(Release release : idToRelease.values()){
			if(release.getAppID().equals(appID)){
				releases.add(release);
			}
		}
		
		return releases;
	}
	
	/* Return release by a version of an application. */
	public Release queryByAppIdAndVersion(String appId, int version) {
		if (!appVersionReleases.containsKey(appId)) {
			return null;
		}
		
		return appVersionReleases.get(appId).get(version);
	}
}
