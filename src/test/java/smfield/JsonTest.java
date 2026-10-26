package smfield;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JsonTest {

    @Test
    void parseiaEstruturasBasicas() {
        Object v = Json.parse("{\"a\": [1, 2.5, true, null], \"b\": \"Feij\\u00e3o\"}");
        Map<?, ?> m = (Map<?, ?>) v;
        assertEquals(List.of(1.0, 2.5, true), ((List<?>) m.get("a")).subList(0, 3));
        assertEquals("Feij\u00e3o", m.get("b"));
    }

    @Test
    void jsonInvalidoLevantaErro() {
        assertThrows(IllegalArgumentException.class, () -> Json.parse("{\"a\": }"));
        assertThrows(IllegalArgumentException.class, () -> Json.parse("[1, 2"));
    }
}
