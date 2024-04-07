/* Licensed under Apache-2.0 2024. */
package com.example.client.admincli.command;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;

@Module
interface CommandModuleBindings {

  @Binds
  @IntoMap
  @ClassKey(UpdatePermissionsCommand.class)
  Object updatePermissionsCommand(UpdatePermissionsCommand updatePermissionsCommand);

  @Binds
  @IntoMap
  @ClassKey(RefreshCommand.class)
  Object refreshCommand(RefreshCommand refreshCommand);

  @Binds
  @IntoMap
  @ClassKey(RegisterCommand.class)
  Object registerCommand(RegisterCommand registerCommand);

  @Binds
  @IntoMap
  @ClassKey(LoginCommand.class)
  Object loginCommand(LoginCommand loginCommand);
}
