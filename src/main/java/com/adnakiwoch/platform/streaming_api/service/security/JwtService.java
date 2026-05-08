package com.adnakiwoch.platform.streaming_api.service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  @Value("${jwt.secret-key}") // this is the secret key that will be used with the hashing of the
  // singiture
  private String SECRET_KEY;

  // this will generate a token from the user detials
  public String generateToken(UserDetails userDetails) {
    return Jwts.builder() // no extra claims with this one
        .subject(userDetails.getUsername())
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(
            new Date(
                System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24 Hours, you can change it
        .signWith(
            getSignInKey(),
            Jwts.SIG.HS256) // this is where it will mix our key and all the string information and
        // creat the hash
        .compact();
  }

  // this will generate the token from the user detials + any extra detials that was given to it
  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    String uuId = userDetails.getUsername(); // u
    return Jwts.builder()
        .claims(extraClaims) // with extra calim of our need
        .subject(uuId) // now the subject of our token is UUID ( better than the name one)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(
            new Date(
                System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24 Hours, you can change it
        .signWith(
            getSignInKey(),
            Jwts.SIG.HS256) // this is where it will mix our key and all the string information and
        // creat the hash
        .compact();
  }

  // to extractt user name same us extractClaim(token, Claim::getsubject);
  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  // validating token
  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String uuid = extractUsername(token);
    return (uuid.equals(userDetails.getUsername())) && !isTokenExpired(token);
  }

  // used by the validation logic too
  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  // to get the expriry claim that was set
  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  // to calim any of the other calims that i haven wrote the specific implmentaiton for
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  // the base logic of verfityin a token, and extracting claims(all)
  private Claims extractAllClaims(String token) {
    return Jwts.parser()
        .verifyWith(getSignInKey()) // here is where our key will be used for verficaiton
        .build()
        .parseSignedClaims(token) // here is where the verfication happens
        .getPayload();
  }

  // this is where the raw charachter become an array of bits so that all the hashin alcoirhms can
  // do
  // the job.
  private SecretKey getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
