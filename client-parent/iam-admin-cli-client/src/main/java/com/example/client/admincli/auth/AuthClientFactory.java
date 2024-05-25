/* Licensed under Apache-2.0 2024. */
package com.example.client.admincli.auth;

import github.benslabbert.vertxdaggerstarter.iamauthclient.IamAuthClient;

public interface AuthClientFactory {

  IamAuthClient provide();
}
