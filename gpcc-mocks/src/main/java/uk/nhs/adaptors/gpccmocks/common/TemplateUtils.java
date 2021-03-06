package uk.nhs.adaptors.gpccmocks.common;

import java.io.StringWriter;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TemplateUtils {
    private static final String TEMPLATES_RESOURCE_ROOT = "templates";
    private static final String TEMPLATES_EXTENSION = ".mustache";

    @SneakyThrows
    public static String fillTemplate(String templateName, Object content) {
        var template = loadTemplate(templateName + TEMPLATES_EXTENSION);
        StringWriter writer = new StringWriter();
        template.execute(writer, content).flush();
        var output = writer.toString();
        log.debug("Template {} with parameters {} produced output\n{}", template, content, output);
        return output;
    }

    private static Mustache loadTemplate(String templateName) {
        MustacheFactory mustacheFactory = new DefaultMustacheFactory(TEMPLATES_RESOURCE_ROOT);
        return mustacheFactory.compile(templateName);
    }
}
