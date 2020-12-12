/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.gui.css;

import cz.vutbr.web.css.CSSException;
import org.junit.Test;
import pixel.gui.style.StyleFactory;
import pixel.gui.style.StyleProperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CssParserTest {

    public String loadCss(String filename) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream resourceStream = loader.getResourceAsStream(filename)) {
            return new BufferedReader(new InputStreamReader(Objects.requireNonNull(resourceStream)))
                    .lines().collect(Collectors.joining("\n"));

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return "";
    }

    @Test
    public void parseCss() throws IOException, CSSException {
        long now = System.currentTimeMillis();

        String customStyle = "color: red; background-color: #fff;";
        List<StyleProperty> props = StyleFactory.getProperties(customStyle);
        for (int i = 0; i < 0; i++) {
            StyleFactory.getProperties(customStyle);
        }

        System.out.println("TOOK " + (System.currentTimeMillis() - now) + "ms");

        //String style = "abcedf";
        //System.out.println( String.join(".", style.split(" ")));

        //String src = loadCss("css/base.css");
        /*CascadingStyleSheet css = CSSReader.readFromString(src, StandardCharsets.UTF_8, ECSSVersion.CSS30);
        Assert.assertNotNull(css);

        for (CSSStyleRule rule : css.getAllStyleRules()) {
                System.out.println(rule);

        }*/
// ((DeclarationImpl) css.get(0).get(0)).get(0)
        /*StyleSheet css = CSSFactory.parseString(src, null);
        Iterator<RuleBlock<?>> it = css.iterator();
        while (it.hasNext()) {
            var rule = it.next();
            System.out.println(rule);
        }*/
    }
}
