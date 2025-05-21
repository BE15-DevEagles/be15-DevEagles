package com.deveagles.be15_deveagles_be.features.auth.command.application.model;

import com.deveagles.be15_deveagles_be.features.user.command.domain.aggregate.UserStatus;
import java.util.Collection;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Builder
public class CustomUser implements UserDetails {

  private final Long userId;
  private final String username;
  private final String password;
  private final UserStatus userStatus;
  private final Collection<? extends GrantedAuthority> authorities;

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
