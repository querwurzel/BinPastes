package com.github.binpastes.paste.api;

import com.github.binpastes.paste.domain.Paste;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Repository;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

class ArchitectureTest {

    private static final ImportOption DO_NOT_INCLUDE_MAVEN_TESTS = location -> !location.contains("/test-classes/");

    @Test
    void domainClassNeverRendered() {
        var importedClasses = new ClassFileImporter()
                .withImportOption(DO_NOT_INCLUDE_MAVEN_TESTS)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.github.binpastes.paste.api");

        var rule = noMethods()
                .that()
                .areDeclaredInClassesThat()
                .resideInAPackage("com.github.binpastes.paste.api")
                .should()
                .haveRawReturnType(Paste.class);

        rule.check(importedClasses);
    }

    @Test
    void injectionsMindArchitecturalBorders() {
        var importedClasses = new ClassFileImporter()
                .withImportOption(DO_NOT_INCLUDE_MAVEN_TESTS)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.github.binpastes.paste.api");

        var rule = noClasses()
                .that()
                .resideInAPackage("com.github.binpastes.paste.api")
                .should()
                .dependOnClassesThat().areAnnotatedWith(Repository.class);

        rule.check(importedClasses);
    }
}
