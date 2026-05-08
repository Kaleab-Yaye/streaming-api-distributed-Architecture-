package com.adnakiwoch.platform.streaming_api.service.security;

import com.adnakiwoch.platform.streaming_api.dto.internal.UserForUserDetails;
import com.adnakiwoch.platform.streaming_api.service.user.UserService;
import java.util.ArrayList;
import java.util.UUID;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class FindUserDetails implements UserDetailsService {

  private final UserService userService;

  FindUserDetails(UserService userService) {

    this.userService = userService;
  }

  public UserDetails loadUserById(UUID uuid) throws UsernameNotFoundException {

    UserForUserDetails user = userService.getInfoForUserWithId(uuid);
    ArrayList<String> rolesList = new ArrayList<>();
    if (user.status()) {
      rolesList.add("ACTIVE");
    }
    if (user.isStudio()) {
      rolesList.add("CAN_UPLOAD");
    }

    String[] roles = new String[rolesList.size()];
    roles = rolesList.toArray(roles);

    return org.springframework.security.core.userdetails.User.builder()
        .username(user.id().toString())
        .password(user.password())
        .authorities(roles)
        .build();
  }

  @Override
  public UserDetails loadUserByUsername(String email)
      throws UsernameNotFoundException { // this is the only methode in this inteface
    // now we need to acces the User object
    // you can change this to the methode for this we will make it so that it will search for user
    // bassed on name

    UserForUserDetails user = userService.getCatchableInfoForUser(email);

    ArrayList<String> rolesList = new ArrayList<>();
    if (user.status()) {
      rolesList.add("ACTIVE");
    }
    if (user.isStudio()) {
      rolesList.add("CAN_UPLOAD");
    }

    String[] roles = new String[rolesList.size()];
    roles = rolesList.toArray(roles);

    return org.springframework.security.core.userdetails.User.builder()
        .username(user.id().toString())
        .password(user.password())
        .authorities(roles)
        .build();
  }
}
