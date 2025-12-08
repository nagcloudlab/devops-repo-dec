package com.upi.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Architecture Tests using ArchUnit
 * 
 * These tests verify that the codebase follows basic architectural rules:
 * - Naming conventions
 * - No cyclic dependencies
 * - Basic layer separation
 * 
 * Note: Rules are relaxed for training purposes to allow common patterns
 * in simple Spring Boot applications.
 * 
 * @author NPCI Training Team
 */
@Epic("Architecture Tests")
@Feature("Code Structure Validation")
@DisplayName("🏛️ Architecture Tests")
public class ArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.upi");
    }

    // =========================================================================
    // NAMING CONVENTION TESTS
    // =========================================================================

    @Test
    @Story("Naming Conventions")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Controllers should have 'Controller' suffix")
    void controllersShouldHaveControllerSuffix() {
        ArchRule rule = classes()
                .that().resideInAPackage("..controller..")
                .and().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
                .should().haveSimpleNameEndingWith("Controller");

        rule.check(importedClasses);
    }

    @Test
    @Story("Naming Conventions")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Services should have 'Service' suffix")
    void servicesShouldHaveServiceSuffix() {
        ArchRule rule = classes()
                .that().resideInAPackage("..service..")
                .and().areAnnotatedWith(org.springframework.stereotype.Service.class)
                .should().haveSimpleNameEndingWith("Service");

        rule.check(importedClasses);
    }

    @Test
    @Story("Naming Conventions")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Repositories should have 'Repository' suffix")
    void repositoriesShouldHaveRepositorySuffix() {
        ArchRule rule = classes()
                .that().resideInAPackage("..repository..")
                .should().haveSimpleNameEndingWith("Repository");

        rule.check(importedClasses);
    }

    // =========================================================================
    // ANNOTATION TESTS
    // =========================================================================

    @Test
    @Story("Annotations")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Controllers should be annotated with @RestController")
    void controllersShouldBeAnnotatedWithRestController() {
        ArchRule rule = classes()
                .that().resideInAPackage("..controller..")
                .and().haveSimpleNameEndingWith("Controller")
                .and().areTopLevelClasses()
                .should().beAnnotatedWith(org.springframework.web.bind.annotation.RestController.class);

        rule.check(importedClasses);
    }

    @Test
    @Story("Annotations")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Services should be annotated with @Service")
    void servicesShouldBeAnnotatedWithService() {
        ArchRule rule = classes()
                .that().resideInAPackage("..service..")
                .and().haveSimpleNameEndingWith("Service")
                .should().beAnnotatedWith(org.springframework.stereotype.Service.class);

        rule.check(importedClasses);
    }

    // =========================================================================
    // LAYER DEPENDENCY TESTS
    // =========================================================================

    @Test
    @Story("Layer Dependencies")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Services should not depend on Controllers")
    void servicesShouldNotDependOnControllers() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..service..")
                .should().dependOnClassesThat()
                .resideInAPackage("..controller..");

        rule.check(importedClasses);
    }

    @Test
    @Story("Layer Dependencies")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Repositories should not depend on Controllers")
    void repositoriesShouldNotDependOnControllers() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..repository..")
                .should().dependOnClassesThat()
                .resideInAPackage("..controller..");

        rule.check(importedClasses);
    }

    @Test
    @Story("Layer Dependencies")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Repositories should not depend on Services")
    void repositoriesShouldNotDependOnServices() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..repository..")
                .should().dependOnClassesThat()
                .resideInAPackage("..service..");

        rule.check(importedClasses);
    }

    @Test
    @Story("Layer Dependencies")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Entities should not depend on Services")
    void entitiesShouldNotDependOnServices() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..entity..")
                .should().dependOnClassesThat()
                .resideInAPackage("..service..");

        rule.check(importedClasses);
    }

    @Test
    @Story("Layer Dependencies")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Entities should not depend on Controllers")
    void entitiesShouldNotDependOnControllers() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..entity..")
                .should().dependOnClassesThat()
                .resideInAPackage("..controller..");

        rule.check(importedClasses);
    }

    // =========================================================================
    // CYCLIC DEPENDENCY TESTS
    // =========================================================================

    @Test
    @Story("Cyclic Dependencies")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Should have no cyclic dependencies between packages")
    void shouldHaveNoCyclicDependencies() {
        ArchRule rule = slices()
                .matching("com.upi.(*)..")
                .should().beFreeOfCycles();

        rule.check(importedClasses);
    }

    // =========================================================================
    // PACKAGE STRUCTURE TESTS
    // =========================================================================

    @Test
    @Story("Package Structure")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Exception classes should extend Exception or RuntimeException")
    void exceptionClassesShouldExtendException() {
        ArchRule rule = classes()
                .that().resideInAPackage("..exception..")
                .and().haveSimpleNameEndingWith("Exception")
                .should().beAssignableTo(Exception.class);

        rule.check(importedClasses);
    }

    @Test
    @Story("Package Structure")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("DTOs in dto package should be public")
    void dtosInDtoPackageShouldBePublic() {
        ArchRule rule = classes()
                .that().resideInAPackage("..dto..")
                .should().bePublic();

        rule.check(importedClasses);
    }

    @Test
    @Story("Package Structure")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Entities should be in entity package")
    void entitiesShouldBeInEntityPackage() {
        ArchRule rule = classes()
                .that().areAnnotatedWith(jakarta.persistence.Entity.class)
                .should().resideInAPackage("..entity..");

        rule.check(importedClasses);
    }
}