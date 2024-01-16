package org.gzu.adminbackend.config;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @description TODO
 * @classname PasswordEncoderConfigTest
 * @date 1/17/2024 3:28 AM
 * @created by LIONS7
 */
@SpringBootTest
public class PasswordEncoderConfigTest {

    @Resource
    private BCryptPasswordEncoder passwordEncoder;

    @Test
    public void test() {
        String encoded = passwordEncoder.encode("root1234");
        System.out.println(encoded);
    }
}
