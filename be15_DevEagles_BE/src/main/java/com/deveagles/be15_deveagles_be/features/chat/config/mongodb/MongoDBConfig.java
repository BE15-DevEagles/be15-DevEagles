package com.deveagles.be15_deveagles_be.features.chat.config.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.deveagles.be15_deveagles_be.features.chat")
public class MongoDBConfig {

  @Value("${spring.data.mongodb.uri}")
  private String mongoUri;

  @Bean
  public MongoClient mongoClient() {
    ServerApi serverApi = ServerApi.builder().version(ServerApiVersion.V1).build();

    MongoClientSettings settings =
        MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(mongoUri))
            .serverApi(serverApi)
            .build();

    return MongoClients.create(settings);
  }

  @Bean
  public MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient) {
    return new SimpleMongoClientDatabaseFactory(mongoClient, "goody-chat");
  }

  @Bean
  public MongoTemplate mongoTemplate(
      MongoDatabaseFactory mongoDatabaseFactory, MongoMappingContext context) {
    MappingMongoConverter converter =
        new MappingMongoConverter(new DefaultDbRefResolver(mongoDatabaseFactory), context);
    converter.setTypeMapper(new DefaultMongoTypeMapper(null));

    converter.setCustomConversions(customConversions());
    converter.afterPropertiesSet();

    return new MongoTemplate(mongoDatabaseFactory, converter);
  }

  @Bean
  public MongoCustomConversions customConversions() {
    return new MongoCustomConversions(
        Arrays.asList(new LocalDateTimeToDateConverter(), new DateToLocalDateTimeConverter()));
  }

  static class LocalDateTimeToDateConverter implements Converter<LocalDateTime, Date> {
    @Override
    public Date convert(LocalDateTime source) {
      return source == null ? null : Date.from(source.atZone(ZoneId.systemDefault()).toInstant());
    }
  }

  static class DateToLocalDateTimeConverter implements Converter<Date, LocalDateTime> {
    @Override
    public LocalDateTime convert(Date source) {
      return source == null
          ? null
          : LocalDateTime.ofInstant(source.toInstant(), ZoneId.systemDefault());
    }
  }
}
