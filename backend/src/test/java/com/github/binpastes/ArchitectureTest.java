package com.github.binpastes;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.ImportOption.Predefined;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {

    private static final ImportOption DO_NOT_INCLUDE_MAVEN_TESTS = location -> !location.contains("/test-classes/");

    private static final JavaClasses appClasses = new ClassFileImporter()
            .withImportOption(DO_NOT_INCLUDE_MAVEN_TESTS)
            .withImportOption(Predefined.DO_NOT_INCLUDE_JARS)
            .withImportOption(Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackagesOf(BinPastes.class);

    @Test
    void moduleVerification() {
        ApplicationModules applicationModules = ApplicationModules.of(BinPastes.class);
        applicationModules.verify();
    }

    @Test
    void applicationLayerIndependentOfDownstreamLayers() {
        var rule = noClasses()
                .that()
                .resideInAPackage("com.github.binpastes.paste.application..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("com.github.binpastes.paste.api..");

        rule.check(appClasses);
    }

    @Test
    void domainLayerIndependentOfDownstreamLayers() {
        var rule = noClasses()
                .that()
                .resideInAPackage("com.github.binpastes.paste.domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("com.github.binpastes.paste.api..", "com.github.binpastes.paste.application..");

        rule.check(appClasses);
    }
}
