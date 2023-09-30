/* Licensed under Apache-2.0 2023. */
package com.example.catalog.mapper;

import dagger.Module;
import dagger.Provides;

@Module
public class MapperModule {

  private MapperModule() {}

  @Provides
  static FindOneResponseDtoMapper findOneResponseDtoMapper() {
    return new FindOneResponseDtoMapperImpl();
  }
}
