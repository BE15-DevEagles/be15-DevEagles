package com.deveagles.be15_deveagles_be.features.chat.config.mongodb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.deveagles.be15_deveagles_be.features.chat")
public class MongoDBConfig {

  @Bean
  public MongoTemplate mongoTemplate(
      MongoDatabaseFactory mongoDatabaseFactory, MongoMappingContext context) {
    MappingMongoConverter converter =
        new MappingMongoConverter(new DefaultDbRefResolver(mongoDatabaseFactory), context);
    converter.setTypeMapper(new DefaultMongoTypeMapper(null));
    return new MongoTemplate(mongoDatabaseFactory, converter);
  }
}
