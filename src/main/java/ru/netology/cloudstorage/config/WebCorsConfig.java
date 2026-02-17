package ru.netology.cloudstorage.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

  private final AppProperties props;

  public WebCorsConfig(AppProperties props) {
    this.props = props;
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    List<String> origins = Arrays.stream(props.getCors().getAllowedOrigins().split(","))
        .map(String::trim)
        .filter(s -> !s.isBlank())
        .toList();

    registry.addMapping("/**")
        .allowedOrigins(origins.toArray(String[]::new))
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .exposedHeaders("auth-token", "Content-Disposition")
        .allowCredentials(true);
  }
}
