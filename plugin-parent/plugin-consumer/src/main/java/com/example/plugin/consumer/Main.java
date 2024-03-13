/* Licensed under Apache-2.0 2024. */
package com.example.plugin.consumer;

import com.example.plugin.consumer.gen.api.model.Post;

public class Main {

  public static void main(String[] args) {
    Post post = Post.builder().id(1).title("title").body("body").registered(true).build();
    System.err.println("post: " + post);
  }
}
