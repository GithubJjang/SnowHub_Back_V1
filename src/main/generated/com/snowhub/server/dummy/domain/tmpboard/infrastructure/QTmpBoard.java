package com.snowhub.server.dummy.domain.tmpboard.infrastructure;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTmpBoard is a Querydsl query type for TmpBoard
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTmpBoard extends EntityPathBase<TmpBoard> {

    private static final long serialVersionUID = -466704963L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTmpBoard tmpBoard = new QTmpBoard("tmpBoard");

    public final StringPath category = createString("category");

    public final StringPath content = createString("content");

    public final DateTimePath<java.sql.Timestamp> createDate = createDateTime("createDate", java.sql.Timestamp.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final EnumPath<com.snowhub.server.dummy.common.condition.State> state = createEnum("state", com.snowhub.server.dummy.common.condition.State.class);

    public final StringPath title = createString("title");

    public final com.snowhub.server.dummy.domain.user.infrastructure.QUser user;

    public QTmpBoard(String variable) {
        this(TmpBoard.class, forVariable(variable), INITS);
    }

    public QTmpBoard(Path<? extends TmpBoard> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTmpBoard(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTmpBoard(PathMetadata metadata, PathInits inits) {
        this(TmpBoard.class, metadata, inits);
    }

    public QTmpBoard(Class<? extends TmpBoard> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.snowhub.server.dummy.domain.user.infrastructure.QUser(forProperty("user")) : null;
    }

}

