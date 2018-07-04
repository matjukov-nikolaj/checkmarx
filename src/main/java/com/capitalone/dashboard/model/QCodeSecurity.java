package com.capitalone.dashboard.model;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.PathInits;

@Generated("com.mysema.query.codegen.EntitySerializer")
public class QCodeSecurity extends EntityPathBase<CodeSecurity> {

    private static final long serialVersionUID = -910994128L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCodeSecurity codeSecurity = new QCodeSecurity("codeSecurity");

    public final QBaseModel _super;

    public final org.bson.types.QObjectId buildId;

    public final org.bson.types.QObjectId collectorItemId;

    // inherited
    public final org.bson.types.QObjectId id;

    public final SetPath<CodeQualityMetric, QCodeQualityMetric> metrics = this.<CodeQualityMetric, QCodeQualityMetric>createSet("metrics", CodeQualityMetric.class, QCodeQualityMetric.class, PathInits.DIRECT2);

    public final StringPath name = createString("name");

    public final NumberPath<Long> timestamp = createNumber("timestamp", Long.class);

    //public final EnumPath<CodeSecurityType> type = createEnum("type", CodeSecurityType.class);

    public final StringPath url = createString("url");

    public final StringPath version = createString("version");

    public QCodeSecurity(String variable) {
        this(CodeSecurity.class, forVariable(variable), INITS);
    }

    public QCodeSecurity(Path<? extends CodeSecurity> path) {
        this(path.getType(), path.getMetadata(), path.getMetadata().isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QCodeSecurity(PathMetadata<?> metadata) {
        this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QCodeSecurity(PathMetadata<?> metadata, PathInits inits) {
        this(CodeSecurity.class, metadata, inits);
    }

    public QCodeSecurity(Class<? extends CodeSecurity> type, PathMetadata<?> metadata, PathInits inits) {
        super(type, metadata, inits);
        this._super = new QBaseModel(type, metadata, inits);
        this.buildId = inits.isInitialized("buildId") ? new org.bson.types.QObjectId(forProperty("buildId")) : null;
        this.collectorItemId = inits.isInitialized("collectorItemId") ? new org.bson.types.QObjectId(forProperty("collectorItemId")) : null;
        this.id = _super.id;
    }

}

