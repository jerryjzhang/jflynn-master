package com.tencent.jflynn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tencent.jflynn.dao.ReleaseDao;
import com.tencent.jflynn.domain.Release;
import com.tencent.jflynn.service.ReleaseService;

@Service
public class ReleaseServiceImpl implements ReleaseService {
	@Autowired
	private ReleaseDao releaseDao;
	
	public Release getReleaseById(String releaseID){
		return releaseDao.queryById(releaseID);
	}
}
