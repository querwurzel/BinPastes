package com.github.binpastes.paste.api;

import com.github.binpastes.BinPastes;
import com.github.binpastes.paste.domain.Paste;
import com.github.binpastes.paste.domain.PasteRepository;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.ImportOption.Predefined;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

class ArchitectureTest {

    private static final ImportOption DO_NOT_INCLUDE_MAVEN_TESTS = location -> !location.contains("/test-classes/");

    private static final JavaClasses appClasses = new ClassFileImporter()
            .withImportOption(DO_NOT_INCLUDE_MAVEN_TESTS)
            .withImportOption(Predefined.DO_NOT_INCLUDE_JARS)
            .withImportOption(Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackagesOf(BinPastes.class);

    @Test
    void apiLayerDoesNotRenderDomain() {
        var rule = noMethods()
                .that()
                .areDeclaredInClassesThat()
                .areAnnotatedWith(RestController.class)
                .or()
                .areAnnotatedWith(Controller.class)
                .should()
                .haveRawReturnType(Paste.class);

        rule.check(appClasses);
    }

    @Test
    void apiLayerDoesNotPersistDomain() {
        var rule = noClasses()
                .that()
                .areAnnotatedWith(RestController.class)
                .or()
                .areAnnotatedWith(Controller.class)
                .should()
                .dependOnClassesThat()
                .areAnnotatedWith(Repository.class);

        rule.check(appClasses);
    }

    @Test
    void domainOnlyPersistedByDomainService() {
        var rule = theClass(PasteRepository.class)
                .should()
                .onlyBeAccessed()
                .byClassesThat()
                .resideInAPackage(PasteRepository.class.getPackageName());

        rule.check(appClasses);
    }
}
