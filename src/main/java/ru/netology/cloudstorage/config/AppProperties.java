package ru.netology.cloudstorage.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

  private final Storage storage = new Storage();
  private final Security security = new Security();
  private final Cors cors = new Cors();
  private final Bootstrap bootstrap = new Bootstrap();

  public Storage getStorage() { return storage; }
  public Security getSecurity() { return security; }
  public Cors getCors() { return cors; }
  public Bootstrap getBootstrap() { return bootstrap; }

  public static class Storage {
    /** Root directory for storing binary file payloads. */
    private String rootDir;

    public String getRootDir() { return rootDir; }
    public void setRootDir(String rootDir) { this.rootDir = rootDir; }
  }

  public static class Security {
    /** TTL для токенов аутентификации. */
    private Duration tokenTtl = Duration.ofHours(24);

    public Duration getTokenTtl() { return tokenTtl; }
    public void setTokenTtl(Duration tokenTtl) { this.tokenTtl = tokenTtl; }
  }

  public static class Cors {
    private String allowedOrigins = "http://localhost:8080,http://localhost:8081";

    public String getAllowedOrigins() { return allowedOrigins; }
    public void setAllowedOrigins(String allowedOrigins) { this.allowedOrigins = allowedOrigins; }
  }

  public static class Bootstrap {
    private List<UserSeed> users = new ArrayList<>();

    public List<UserSeed> getUsers() { return users; }
    public void setUsers(List<UserSeed> users) { this.users = users; }

    public static class UserSeed {
      private String login;
      private String password;

      public String getLogin() { return login; }
      public void setLogin(String login) { this.login = login; }

      public String getPassword() { return password; }
      public void setPassword(String password) { this.password = password; }
    }
  }
}
