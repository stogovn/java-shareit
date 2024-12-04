package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ShareItServerTest {
    @Test
    void mainMethodTest() {
        ShareItServer.main(new String[]{}); // Явный вызов метода main
    }
}