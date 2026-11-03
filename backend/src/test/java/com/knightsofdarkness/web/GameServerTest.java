package com.knightsofdarkness.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;

/**
 * the whole server on a database of its own, driven through MockMvc
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(properties = {
    "app.jwt.secret=dependency-upgrade-test-only-key-0123456789012345678901234567890123456789",
    "app.jwt.expiration.minutes=5"
})
@AutoConfigureMockMvc
@Import(TestDatabase.class)
public @interface GameServerTest {
}
