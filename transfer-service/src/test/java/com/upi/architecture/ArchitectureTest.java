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
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Architecture Tests using ArchUnit
 * 
 * These tests verify that the codebase follows the defined architectural rules:
 * - Layer dependencies (Controller → Service → Repository)
 * - Naming conventions
 * - Package structure
 * - No cyclic dependencies
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
    // LAYER DEPENDENCY TESTS
    // =========================================================================

    @Test
    @Story("Layer Dependencies")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Should enforce layered architecture")
    void shouldEnforceLayeredArchitecture() {
        ArchRule rule = layeredArchitecture()
                .consideringAllDependencies()
                .layer("Controller").definedBy("..controller..")
                .layer("Service").definedBy("..service..")
                .layer("Repository").definedBy("..repository..")
                .layer("Entity").definedBy("..entity..")
                .layer("DTO").definedBy("..dto..")
                .layer("Exception").definedBy("..exception..")
                
                .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
                .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller", "Service")
                .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service")
                .whereLayer("Entity").mayOnlyBeAccessedByLayers("Service", "Repository")
                .whereLayer("DTO").mayOnlyBeAccessedByLayers("Controller", "Service", "Exception");

        rule.check(importedClasses);
    }

    @Test
    @Story("Layer Dependencies")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Controllers should only depend on Services and DTOs")
    void controllersShouldOnlyDependOnServicesAndDtos() {
        ArchRule rule = classes()
                .that().resideInAPackage("..controller..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage(
                        "..controller..",
                        "..service..",
                        "..dto..",
                        "..exception..",
                        "java..",
                        "javax..",
                        "jakarta..",
                        "org.springframework..",
                        "io.swagger..",
                        "io.qameta.."
                );

        rule.check(importedClasses);
    }

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
    @DisplayName("Repositories should not depend on Controllers or Services")
    void repositoriesShouldNotDependOnUpperLayers() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..repository..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..controller..", "..service..");

        rule.check(importedClasses);
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

    @Test
    @Story("Naming Conventions")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("DTOs should be in dto package")
    void dtosShouldBeInDtoPackage() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Request")
                .or().haveSimpleNameEndingWith("Response")
                .should().resideInAPackage("..dto..");

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
    @DisplayName("Entities should only be accessed by repositories and services")
    void entitiesShouldOnlyBeAccessedByRepositoriesAndServices() {
        ArchRule rule = classes()
                .that().resideInAPackage("..entity..")
                .should().onlyBeAccessed().byAnyPackage(
                        "..entity..",
                        "..repository..",
                        "..service.."
                );

        rule.check(importedClasses);
    }

    @Test
    @Story("Package Structure")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Exceptions should be in exception package")
    void exceptionsShouldBeInExceptionPackage() {
        ArchRule rule = classes()
                .that().areAssignableTo(Exception.class)
                .and().doNotHaveFullyQualifiedName("java.lang.Exception")
                .should().resideInAPackage("..exception..");

        rule.check(importedClasses);
    }

    // =========================================================================
    // SPRING BEST PRACTICES
    // =========================================================================

    @Test
    @Story("Spring Best Practices")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Controllers should not have @Autowired fields")
    void controllersShouldNotHaveAutowiredFields() {
        ArchRule rule = noFields()
                .that().areDeclaredInClassesThat().resideInAPackage("..controller..")
                .should().beAnnotatedWith(org.springframework.beans.factory.annotation.Autowired.class)
                .because("Controllers should use constructor injection");

        rule.check(importedClasses);
    }

    @Test
    @Story("Spring Best Practices")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Services should not have @Autowired fields")
    void servicesShouldNotHaveAutowiredFields() {
        ArchRule rule = noFields()
                .that().areDeclaredInClassesThat().resideInAPackage("..service..")
                .should().beAnnotatedWith(org.springframework.beans.factory.annotation.Autowired.class)
                .because("Services should use constructor injection");

        rule.check(importedClasses);
    }
}