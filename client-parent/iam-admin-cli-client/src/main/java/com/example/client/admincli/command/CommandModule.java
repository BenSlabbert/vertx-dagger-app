/* Licensed under Apache-2.0 2024. */
package com.example.client.admincli.command;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;

@Module
public interface CommandModule {

  @Binds
  @IntoMap
  @ClassKey(PingCommand.class)
  Object pingCommand(PingCommand pingCommand);
}
