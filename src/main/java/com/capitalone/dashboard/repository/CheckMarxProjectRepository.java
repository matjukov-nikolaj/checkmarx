package com.capitalone.dashboard.repository;

import com.capitalone.dashboard.model.CheckMarxProject;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface CheckMarxProjectRepository extends BaseCollectorItemRepository<CheckMarxProject> {

    @Query(value="{ 'collectorId' : ?0, options.projectId : ?1, options.projectName : ?2, options.projectTimestamp : ?3}")
    CheckMarxProject findCheckMarxProject(ObjectId collectorId, String projectId, String projectName, Long projectTimestamp);

    @Query(value="{ 'collectorId' : ?0, options.current : ?1}")
    CheckMarxProject findCurrentProject(ObjectId collectorId, Boolean current);

    @Query(value="{ 'collectorId' : ?0, options.instanceUrl : ?1, enabled: true}")
    List<CheckMarxProject> findEnabledProjects(ObjectId collectorId, String instanceUrl);
}
