package com.adnakiwoch.platform.streaming_api.dto.req;

public class TestRegsiterNewUserReq {

  public String name;

  public String email;

  public String password;

  public TestRegsiterNewUserReq(String name, String email, String password) {
    this.email = email;
    this.name = name;
    this.password = password;
  }
}
