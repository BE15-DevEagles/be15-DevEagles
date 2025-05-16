package com.deveagles.be15_deveagles_be;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
    classes = Be15DevEaglesBeApplicationTests.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

// 의존성 사용하게 되면 아래 코드 삭제
@EnableAutoConfiguration(
    exclude = {
      DataSourceAutoConfiguration.class,
      HibernateJpaAutoConfiguration.class,
      SecurityAutoConfiguration.class
    })
@TestPropertySource(
    properties = {
      "spring.cloud.aws.region.static=us-east-1",
      "spring.cloud.aws.s3.enabled=false",
      "spring.cloud.aws.dynamodb.enabled=false",
      "spring.cloud.aws.ses.enabled=false",
      "spring.cloud.aws.sqs.enabled=false",
      "spring.cloud.aws.sns.enabled=false",
      "spring.cloud.aws.credentials.access-key=test",
      "spring.cloud.aws.credentials.secret-key=test"
    })
class Be15DevEaglesBeApplicationTests {

  @Test
  void contextLoads() {}
}
