/**
 * this package contains three packages to deal with eventbus message patters:
 *
 * <ul>
 *   <li>p2p (point to point) send a message to a single consumer without response
 *   <li>rpc (remote procedure call) send a message to a single consumer with response
 *   <li>pubsub (publish subscribe) send a message to many consumers without response
 * </ul>
 *
 * <br>
 * inside the package there may be implementations that support persistent messages unless stated,
 * these messages will not be persistent and will rely on the vertx "best effort delivery" {@link
 * https://vertx.io/docs/vertx-core/java/#_best_effort_delivery}
 */
package com.example.commons.eventbus;
