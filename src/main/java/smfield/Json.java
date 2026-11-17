package smfield;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parser JSON minimo (sem dependencias externas), suficiente para os arquivos de dados.
 * Retorna Map (objeto), List (array), String, Double, Boolean ou null.
 */
final class Json {
    private final String s;
    private int i;

    private Json(String s) {
        this.s = s;
    }

    static Object parse(String texto) {
        Json p = new Json(texto);
        p.espacos();
        Object v = p.valor();
        p.espacos();
        if (p.i != p.s.length()) {
            throw p.erro("conteudo inesperado apos o fim do JSON");
        }
        return v;
    }

    private IllegalArgumentException erro(String msg) {
        return new IllegalArgumentException("JSON invalido na posicao " + i + ": " + msg);
    }

    private void espacos() {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
            i++;
        }
    }

    private Object valor() {
        if (i >= s.length()) {
            throw erro("fim inesperado");
        }
        char c = s.charAt(i);
        switch (c) {
            case '{':
                return objeto();
            case '[':
                return array();
            case '"':
                return string();
            case 't':
                literal("true");
                return Boolean.TRUE;
            case 'f':
                literal("false");
                return Boolean.FALSE;
            case 'n':
                literal("null");
                return null;
            default:
                return numero();
        }
    }

    private void literal(String esperado) {
        if (!s.startsWith(esperado, i)) {
            throw erro("esperado '" + esperado + "'");
        }
        i += esperado.length();
    }

    private Map<String, Object> objeto() {
        Map<String, Object> m = new LinkedHashMap<>();
        i++; // {
        espacos();
        if (i < s.length() && s.charAt(i) == '}') {
            i++;
            return m;
        }
        while (true) {
            espacos();
            if (i >= s.length() || s.charAt(i) != '"') {
                throw erro("chave de objeto esperada");
            }
            String chave = string();
            espacos();
            if (i >= s.length() || s.charAt(i) != ':') {
                throw erro("':' esperado");
            }
            i++;
            espacos();
            m.put(chave, valor());
            espacos();
            if (i >= s.length()) {
                throw erro("fim inesperado no objeto");
            }
            char c = s.charAt(i++);
            if (c == '}') {
                return m;
            }
            if (c != ',') {
                throw erro("',' ou '}' esperado");
            }
        }
    }

    private List<Object> array() {
        List<Object> l = new ArrayList<>();
        i++; // [
        espacos();
        if (i < s.length() && s.charAt(i) == ']') {
            i++;
            return l;
        }
        while (true) {
            espacos();
            l.add(valor());
            espacos();
            if (i >= s.length()) {
                throw erro("fim inesperado no array");
            }
            char c = s.charAt(i++);
            if (c == ']') {
                return l;
            }
            if (c != ',') {
                throw erro("',' ou ']' esperado");
            }
        }
    }

    private String string() {
        StringBuilder sb = new StringBuilder();
        i++; // abre aspas
        while (i < s.length()) {
            char c = s.charAt(i++);
            if (c == '"') {
                return sb.toString();
            }
            if (c == '\\') {
                if (i >= s.length()) {
                    break;
                }
                char e = s.charAt(i++);
                switch (e) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'u':
                        if (i + 4 > s.length()) {
                            throw erro("escape \\u incompleto");
                        }
                        sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                        i += 4;
                        break;
                    default:
                        throw erro("escape invalido \\" + e);
                }
            } else {
                sb.append(c);
            }
        }
        throw erro("string sem fechamento");
    }

    private Double numero() {
        int ini = i;
        while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) {
            i++;
        }
        if (ini == i) {
            throw erro("valor inesperado '" + s.charAt(i) + "'");
        }
        try {
            return Double.valueOf(s.substring(ini, i));
        } catch (NumberFormatException e) {
            throw erro("numero invalido");
        }
    }
}
