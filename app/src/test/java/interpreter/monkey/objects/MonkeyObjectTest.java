package interpreter.monkey.objects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class MonkeyObjectTest {

    @Test
    void testStringHashKey() {
        MonkeyString hello1 = new MonkeyString("Hello World");
        MonkeyString hello2 = new MonkeyString("Hello World");
        MonkeyString diff1 = new MonkeyString("My name is Johnny");
        MonkeyString diff2 = new MonkeyString("My name is Johnny");

        assertEquals(hello1.hashKey(), hello2.hashKey());
        assertEquals(diff1.hashKey(), diff2.hashKey());
        assertNotEquals(hello1.hashKey(), diff1.hashKey());
    }
}
