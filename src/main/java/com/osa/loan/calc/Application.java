package com.osa.loan.calc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.io.ResourceType;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderErrors;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@EnableConfigurationProperties
@ComponentScan(basePackageClasses = Application.class)
@PropertySource(value = "classpath:application.properties")
@SpringBootApplication
public class Application extends SpringBootServletInitializer {

    @Value("${drools.files}")
    private List<String> droolsFiles;

    @Bean
    public Gson gson() {
        return new GsonBuilder().setPrettyPrinting().create();
    }

    @Bean
    public Type questionsType() {
        return new TypeToken<Map<String, String>>() {}.getType();
    }

    @Bean
    public ConversionService conversionService() {
        return new DefaultConversionService();
    }

    public KnowledgeBase readKnowledgeBase() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        droolsFiles.stream()
                .map(name -> name.concat(".drl"))
                .map(ResourceFactory::newClassPathResource)
                .forEachOrdered(resource -> kbuilder.add(resource, ResourceType.DRL));
        KnowledgeBuilderErrors errors = kbuilder.getErrors();
        if (!errors.isEmpty()) {
            errors.forEach(err -> log.error("drools error: \n" + err));
            throw new IllegalArgumentException("Could not parse knowledge.");
        }
        KnowledgeBase kbase = kbuilder.newKnowledgeBase();
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        return kbase;
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    public static void main(String... args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
}
