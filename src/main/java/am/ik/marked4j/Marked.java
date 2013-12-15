package am.ik.marked4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

public class Marked {
    private final Invocable invocableEngine;
    private final Object marked4j;

    public Marked(boolean gfm, boolean tables, boolean breaks,
                  boolean pedantic, boolean sanitize, boolean smartLists,
                  boolean smartypants) {
        this();
    }

    public Marked() {
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        this.invocableEngine = (Invocable) engine;
        try (InputStream strm = Marked.class.getClassLoader()
                .getResourceAsStream(
                        "META-INF/resources/assets/marked/lib/marked.js")) {
            String js = copyToString(strm, StandardCharsets.UTF_8);
            Bindings bindings = new SimpleBindings(); // todo
            this.marked4j = engine
                    .eval(js
                            + ";Marked4J = function(marked){this.marked = marked}; new Marked4J(marked);",
                            bindings);
        } catch (IOException e) {
            throw new IllegalStateException("marked.js is not found.", e);
        } catch (ScriptException e) {
            throw new IllegalStateException("invalid script!", e);
        }
    }

    public String marked(String markdownText) {
        try {
            Object result = this.invocableEngine.invokeMethod(marked4j,
                    "marked", markdownText);
            return result == null ? null : result.toString();
        } catch (NoSuchMethodException | ScriptException e) {
            throw new IllegalArgumentException(
                    "Cannot parse the given markdown text!", e);
        }
    }

    private static String copyToString(InputStream in, Charset charset)
            throws IOException {
        StringBuilder out = new StringBuilder();
        try (InputStreamReader reader = new InputStreamReader(in, charset);) {
            char[] buffer = new char[4096];
            int bytesRead = -1;
            while ((bytesRead = reader.read(buffer)) != -1) {
                out.append(buffer, 0, bytesRead);
            }
            return out.toString();
        }
    }

}