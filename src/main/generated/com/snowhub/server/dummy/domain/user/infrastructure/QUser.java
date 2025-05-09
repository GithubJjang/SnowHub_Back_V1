package com.snowhub.server.dummy.domain.user.infrastructure;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = 373557013L;

    public static final QUser user = new QUser("user");

    public final StringPath displayName = createString("displayName");

    public final StringPath email = createString("email");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath password = createString("password");

    public final StringPath refreshToken = createString("refreshToken");

    public final EnumPath<com.snowhub.server.dummy.domain.role.RoleType> roleType = createEnum("roleType", com.snowhub.server.dummy.domain.role.RoleType.class);

    public final StringPath uid = createString("uid");

    public QUser(String variable) {
        super(User.class, forVariable(variable));
    }

    public QUser(Path<? extends User> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUser(PathMetadata metadata) {
        super(User.class, metadata);
    }

}

