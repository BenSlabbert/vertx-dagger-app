package com.example.commons.networking;

import io.netty.resolver.dns.DefaultDnsServerAddressStreamProvider;
import java.io.IOException;
import java.util.List;

public class ReachableNameServers {

  private ReachableNameServers() {}

  public static List<String> getReachableNameServers() {
    // not sure if this is still needed
    // root cause might be in the custom jlink runtime
    return DefaultDnsServerAddressStreamProvider.defaultAddressList().stream()
        .filter(
            ns -> {
              try {
                return ns.getAddress().isReachable(1000);
              } catch (IOException e) {
                // do nothing
              }
              return false;
            })
        .map(ns -> ns.getAddress().getHostAddress())
        .toList();
  }
}
