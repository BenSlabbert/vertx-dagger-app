/* Licensed under Apache-2.0 2024. */
package com.example.starter.jdbc.pool;

import dagger.Module;

@Module(includes = {BlockingJdbcPoolConfig.class, JdbcPoolModuleBindings.class})
public interface JdbcPoolModule {}
