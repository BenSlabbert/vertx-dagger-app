/* Licensed under Apache-2.0 2023. */
package com.example.payment.scope;

import org.jooq.DSLContext;

public interface DslProvider {

  DSLContext getContext();
}
