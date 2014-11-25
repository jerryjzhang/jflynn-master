package com.tencent.jflynn.service;

import java.util.List;

import com.tencent.jflynn.domain.Formation;

public interface FormationService {
	public List<Formation> getAllFormations();
	public Formation getAppFormation(String appId);
}
