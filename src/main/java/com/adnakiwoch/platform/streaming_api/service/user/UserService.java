package com.adnakiwoch.platform.streaming_api.service.user;

import com.adnakiwoch.platform.streaming_api.domain.Subscription;
import com.adnakiwoch.platform.streaming_api.domain.User;
import com.adnakiwoch.platform.streaming_api.domain.Vid;
import com.adnakiwoch.platform.streaming_api.dto.internal.UserForUserDetails;
import com.adnakiwoch.platform.streaming_api.dto.request.upload.UploadVidRequest;
import com.adnakiwoch.platform.streaming_api.dto.response.user.UploadResponse;
import com.adnakiwoch.platform.streaming_api.exception.crypto.InternalCryptoException;
import com.adnakiwoch.platform.streaming_api.exception.resource.ResourceNotFoundException;
import com.adnakiwoch.platform.streaming_api.exception.user.DuplicateUserException;
import com.adnakiwoch.platform.streaming_api.repository.SubscriptionRepo;
import com.adnakiwoch.platform.streaming_api.repository.UserRepository;
import com.adnakiwoch.platform.streaming_api.repository.VidRepo;
import com.adnakiwoch.platform.streaming_api.service.internal.EmailService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final SubscriptionRepo subscriptionRepo;
  private final MeterRegistry meterRegistry;
  private final Counter successRegistrationCounter;
  private final EmailService emailService;
  private final VidRepo vidRepo;

  public UserService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      SubscriptionRepo subscriptionRepo,
      MeterRegistry meterRegistry,
      EmailService emailService,
      VidRepo vidRepo) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.subscriptionRepo = subscriptionRepo;
    this.meterRegistry = meterRegistry;
    this.successRegistrationCounter =
        Counter.builder("successful_Regist")
            .tag("New_User", "User_Service")
            .description("counts the number of successful registrations")
            .register(meterRegistry);
    this.emailService = emailService;
    this.vidRepo = vidRepo;
  }

  @Transactional
  public void register(String name, String email, String password) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] mdB = md.digest(email.getBytes(StandardCharsets.UTF_8));
      MDC.put("Attempted_Email", HexFormat.of().formatHex(mdB));

    } catch (NoSuchAlgorithmException ex) {
      throw new InternalCryptoException("Digest failed at registration", ex);
    }
    if (userRepository.checkUserExistByEmail(email)) {
      MDC.put("cause", "duplicate value");
      log.warn("NEW_USER_REGIST_FAILED");
      throw new DuplicateUserException("email already taken");
    }
    log.info("NEW_USER_REGIST_SUCCESS");
    User user = new User();
    user.setUserName(name);
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(password));
    userRepository.save(user);

    Subscription subscription = new Subscription();
    subscription.setUser(user);

    subscriptionRepo.save(subscription);
    successRegistrationCounter.increment();

    emailService.sendEmailToUser(
        email,
        "noreplay@Adnakiwoch",
        "Registration success full",
        "your account has been created welcome");
  }

  public UserForUserDetails getCatchableInfoForUser(String email) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

    return new UserForUserDetails(
        user.getId(), user.getPassword(), user.getSubscription().getStatus(), user.getIsStudio());
  }

  @Cacheable(value = "privilege", key = "#uuid")
  public UserForUserDetails getInfoForUserWithId(UUID uuid) {
    User user =
        userRepository
            .findById(uuid)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + uuid.toString()));
    return new UserForUserDetails(
        user.getId(), user.getPassword(), user.getSubscription().getStatus(), user.getIsStudio());
  }

  public UploadResponse uploadServ(UserDetails userDetails, UploadVidRequest uploadVidRequest) {
    UUID userId = UUID.fromString(userDetails.getUsername());
    Vid newVid = new Vid();
    newVid.setName(uploadVidRequest.name());
    newVid.setAbout(uploadVidRequest.about());
    newVid.setUser(userRepository.getUserById((userId)));
    vidRepo.save(newVid);
    return new UploadResponse(newVid.getId());
  }
}
