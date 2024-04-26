package com.github.binpastes.paste.api;

import com.github.binpastes.paste.domain.Paste;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.ImportOption.Predefined;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

class ArchitectureTest {

    private static final ImportOption DO_NOT_INCLUDE_MAVEN_TESTS = location -> !location.contains("/test-classes/");

    @Test
    void domainClassNeverRendered() {
        var importedClasses = new ClassFileImporter()
                .withImportOption(DO_NOT_INCLUDE_MAVEN_TESTS)
                .withImportOption(Predefined.DO_NOT_INCLUDE_JARS)
                .withImportOption(Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackagesOf(PasteController.class);

        var rule = noMethods()
                .that()
                .areDeclaredInClassesThat()
                .areAnnotatedWith(RestController.class)
                .or()
                .areAnnotatedWith(Controller.class)
                .should()
                .haveRawReturnType(Paste.class);

        rule.check(importedClasses);
    }

    @Test
    void injectionsMindArchitecturalBorders() {
        var importedClasses = new ClassFileImporter()
                .withImportOption(DO_NOT_INCLUDE_MAVEN_TESTS)
                .withImportOption(Predefined.DO_NOT_INCLUDE_JARS)
                .withImportOption(Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackagesOf(PasteController.class);

        var rule = noClasses()
                .that()
                .areAnnotatedWith(RestController.class)
                .or()
                .areAnnotatedWith(Controller.class)
                .should()
                .dependOnClassesThat()
                .areAnnotatedWith(Repository.class);

        rule.check(importedClasses);
    }
}
