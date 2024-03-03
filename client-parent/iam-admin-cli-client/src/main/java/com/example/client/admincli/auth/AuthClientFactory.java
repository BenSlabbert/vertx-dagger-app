/* Licensed under Apache-2.0 2024. */
package com.example.client.admincli.auth;

import com.example.starter.iam.auth.client.IamAuthClient;

public interface AuthClientFactory {

  IamAuthClient provide();
}
