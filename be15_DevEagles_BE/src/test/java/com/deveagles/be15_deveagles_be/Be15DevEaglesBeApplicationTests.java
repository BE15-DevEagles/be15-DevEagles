package com.deveagles.be15_deveagles_be;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
    classes = Be15DevEaglesBeApplicationTests.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

// 의존성 사용하게 되면 아래 코드 삭제
@EnableAutoConfiguration(
    exclude = {
      WebSocketServletAutoConfiguration.class, // WebSocket 자동설정 제외
      MongoAutoConfiguration.class, // MongoDB 자동설정 제외
      MongoDataAutoConfiguration.class, // MongoDB 데이터 자동설정 제외
      MongoRepositoriesAutoConfiguration.class // MongoDB 리포지토리 자동설정 제외
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
      "spring.cloud.aws.credentials.secret-key=test",
      "spring.main.allow-bean-definition-overriding=true",
      "spring.data.mongodb.auto-index-creation=false", // MongoDB 인덱스 생성 비활성화
      "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration" // MongoDB 설정 제외
    })
class Be15DevEaglesBeApplicationTests {}
