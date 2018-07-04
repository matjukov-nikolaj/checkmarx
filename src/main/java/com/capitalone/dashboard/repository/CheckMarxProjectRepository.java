package com.capitalone.dashboard.repository;

import com.capitalone.dashboard.model.CheckMarxProject;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface CheckMarxProjectRepository extends BaseCollectorItemRepository<CheckMarxProject> {

    @Query(value="{ 'collectorId' : ?0, options.instanceUrl : ?1, options.projectId : ?2}")
    CheckMarxProject findCheckMarxProject(ObjectId collectorId, String instanceUrl, String projectId);

    @Query(value="{ 'collectorId' : ?0, options.instanceUrl : ?1, enabled: true}")
    List<CheckMarxProject> findEnabledProjects(ObjectId collectorId, String instanceUrl);
}
