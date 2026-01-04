package vn.edu.smd.core.config;

import org.hibernate.dialect.DatabaseVersion;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.spi.JdbcTypeRegistry;

/**
 * Custom PostgreSQL dialect that disables enum type handling
 * This forces Hibernate to use AttributeConverter for enum fields
 */
public class CustomPostgreSQLDialect extends PostgreSQLDialect {
    
    public CustomPostgreSQLDialect() {
        super(DatabaseVersion.make(15, 0));
    }
    
    @Override
    public void contributeTypes(org.hibernate.boot.model.TypeContributions typeContributions, org.hibernate.service.ServiceRegistry serviceRegistry) {
        super.contributeTypes(typeContributions, serviceRegistry);
        
        // Force all enums to use VARCHAR instead of PostgreSQL enum type
        JdbcTypeRegistry jdbcTypeRegistry = typeContributions.getTypeConfiguration().getJdbcTypeRegistry();
        JdbcType varcharType = jdbcTypeRegistry.getDescriptor(SqlTypes.VARCHAR);
        jdbcTypeRegistry.addDescriptor(SqlTypes.NAMED_ENUM, varcharType);
    }
}
