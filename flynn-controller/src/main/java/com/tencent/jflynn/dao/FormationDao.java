package com.tencent.jflynn.dao;

import java.util.List;

import com.tencent.jflynn.domain.Formation;

public interface FormationDao {
	public void save(Formation formation);
	public List<Formation> queryAll();
	public Formation queryByAppId(String appId);
}
