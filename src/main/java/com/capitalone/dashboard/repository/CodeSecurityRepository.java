package com.capitalone.dashboard.repository;

import com.capitalone.dashboard.model.CodeSecurity;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for {@link CodeSecurity} data.
 */

public interface CodeSecurityRepository extends CrudRepository<CodeSecurity, ObjectId>, QueryDslPredicateExecutor<CodeSecurity> {

    /**
     * Finds the {@link CodeSecurity} data point at the given timestamp for a specific
     * {@link com.capitalone.dashboard.model.CollectorItem}.
     *
     * @param collectorItemId collector item id
     * @param timestamp timestamp
     * @return a {@link CodeSecurity}
     */
    CodeSecurity findByCollectorItemIdAndTimestamp(ObjectId collectorItemId, long timestamp);

    List<CodeSecurity> findByCollectorItemIdAndVersionOrderByTimestampDesc (ObjectId collectorItemId,String version);

    List<CodeSecurity> findByCollectorItemIdAndNameAndVersionOrderByTimestampDesc (ObjectId collectorItemId,String name,String version);

    List<CodeSecurity> findByCollectorItemIdOrderByTimestampDesc (ObjectId collectorItemId);

    List<CodeSecurity> findByNameAndVersion(String name,String version);

    List<CodeSecurity> findByNameAndVersionOrderByTimestampDesc(String name,String version);

    List<CodeSecurity> findByCollectorItemIdAndTimestampIsBetweenOrderByTimestampDesc(ObjectId collectorItemId, long beginDate, long endDate);
}
