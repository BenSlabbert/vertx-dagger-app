/* Licensed under Apache-2.0 2023. */
package com.example.commons.redis;

import dagger.Module;

@Module(includes = {RedisAPIProvider.class, RedisModuleBindings.class})
public interface RedisModule {}
