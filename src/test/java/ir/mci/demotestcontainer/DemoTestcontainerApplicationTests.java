package ir.mci.demotestcontainer;

import java.time.Duration;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
class DemoTestcontainerApplicationTests {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("docker.xyz.dev/mysql:latest")
            .asCompatibleSubstituteFor("mysql"))
            .withNetwork(Network
                    .NetworkImpl
                    .builder()
                    .createNetworkCmdModifier(
                            cm -> cm.withOptions(Map.of("com.docker.network.bridge.host_binding_ipv4", "127.0.0.1")))
                    .build())
            .withExposedPorts(3306)
            .withStartupTimeout(Duration.ofMinutes(1))
            .withConnectTimeoutSeconds(60);
    @Autowired
    PersonRepo repo;
    @Autowired
    DataSource dataSource;

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> mysql.getJdbcUrl());
        registry.add("spring.datasource.username", () -> mysql.getUsername());
        registry.add("spring.datasource.password", () -> mysql.getPassword());
    }


    @Sql(scripts = "classpath:user-data.sql")
    @Test
    void contextLoads() {
        var persons = repo.findAll();
        System.out.println(persons);
        Assertions.assertEquals(2, persons.size());
    }
}
