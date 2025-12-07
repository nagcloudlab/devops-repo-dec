package com.upi.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.*;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Architecture Tests - Enforce code structure and dependencies
 * 
 * TEST PYRAMID: ARCHITECTURE TESTS
 * - Enforces layered architecture
 * - Prevents circular dependencies
 * - Ensures proper annotations
 */
@DisplayName("Architecture Tests - Code Structure Enforcement")
class ArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setUp() {
        importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.upi");
    }

    // ========================================================================
    // LAYERED ARCHITECTURE
    // ========================================================================

    @Nested
    @DisplayName("Layered Architecture Rules")
    class LayeredArchitectureTests {

        @Test
        @DisplayName("✅ Should follow layered architecture")
        void shouldFollowLayeredArchitecture() {
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
                .whereLayer("Entity").mayOnlyBeAccessedByLayers("Repository", "Service", "Controller")
                .whereLayer("DTO").mayOnlyBeAccessedByLayers("Controller", "Service");

            rule.check(importedClasses);
        }
    }

    // ========================================================================
    // NAMING CONVENTIONS
    // ========================================================================

    @Nested
    @DisplayName("Naming Convention Rules")
    class NamingConventionTests {

        @Test
        @DisplayName("✅ Controllers should have Controller suffix")
        void controllersShouldHaveCorrectSuffix() {
            ArchRule rule = classes()
                .that().resideInAPackage("..controller..")
                .and().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
                .should().haveSimpleNameEndingWith("Controller");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("✅ Services should have Service suffix")
        void servicesShouldHaveCorrectSuffix() {
            ArchRule rule = classes()
                .that().resideInAPackage("..service..")
                .and().areAnnotatedWith(org.springframework.stereotype.Service.class)
                .should().haveSimpleNameEndingWith("Service");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("✅ Repositories should have Repository suffix")
        void repositoriesShouldHaveCorrectSuffix() {
            ArchRule rule = classes()
                .that().resideInAPackage("..repository..")
                .should().haveSimpleNameEndingWith("Repository");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("✅ DTOs should have proper naming")
        void dtosShouldHaveProperNaming() {
            ArchRule rule = classes()
                .that().resideInAPackage("..dto..")
                .should().haveSimpleNameEndingWith("Request")
                .orShould().haveSimpleNameEndingWith("Response")
                .orShould().haveSimpleNameEndingWith("Error")
                .orShould().haveSimpleNameEndingWith("DTO");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("✅ Exceptions should have Exception suffix")
        void exceptionsShouldHaveCorrectSuffix() {
            ArchRule rule = classes()
                .that().resideInAPackage("..exception..")
                .and().areAssignableTo(Exception.class)
                .should().haveSimpleNameEndingWith("Exception");

            rule.check(importedClasses);
        }
    }

    // ========================================================================
    // ANNOTATION RULES
    // ========================================================================

    @Nested
    @DisplayName("Annotation Rules")
    class AnnotationRuleTests {

        @Test
        @DisplayName("✅ Controllers should be annotated with @RestController")
        void controllersShouldBeAnnotated() {
            ArchRule rule = classes()
                .that().resideInAPackage("..controller..")
                .and().haveSimpleNameEndingWith("Controller")
                .should().beAnnotatedWith(org.springframework.web.bind.annotation.RestController.class);

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("✅ Services should be annotated with @Service")
        void servicesShouldBeAnnotated() {
            ArchRule rule = classes()
                .that().resideInAPackage("..service..")
                .and().haveSimpleNameEndingWith("Service")
                .should().beAnnotatedWith(org.springframework.stereotype.Service.class);

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("✅ Entities should be annotated with @Entity")
        void entitiesShouldBeAnnotated() {
            ArchRule rule = classes()
                .that().resideInAPackage("..entity..")
                .and().haveSimpleNameNotEndingWith("Builder")
                .should().beAnnotatedWith(jakarta.persistence.Entity.class);

            rule.check(importedClasses);
        }
    }

    // ========================================================================
    // DEPENDENCY RULES
    // ========================================================================

    @Nested
    @DisplayName("Dependency Rules")
    class DependencyRuleTests {

        @Test
        @DisplayName("✅ No cyclic dependencies between packages")
        void noCyclicDependencies() {
            ArchRule rule = slices()
                .matching("com.upi.(*)..")
                .should().beFreeOfCycles();

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("✅ Controllers should not depend on repositories directly")
        void controllersNotDependOnRepositories() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..controller..")
                .should().dependOnClassesThat().resideInAPackage("..repository..");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("✅ Services should not depend on controllers")
        void servicesNotDependOnControllers() {
            ArchRule rule = noClasses()
                .that().resideInAPackage("..service..")
                .should().dependOnClassesThat().resideInAPackage("..controller..");

            rule.check(importedClasses);
        }
    }

    // ========================================================================
    // FIELD INJECTION RULES
    // ========================================================================

    @Nested
    @DisplayName("Dependency Injection Rules")
    class DependencyInjectionRuleTests {

        @Test
        @DisplayName("✅ No field injection - use constructor injection")
        void noFieldInjection() {
            ArchRule rule = noFields()
                .should().beAnnotatedWith(org.springframework.beans.factory.annotation.Autowired.class);

            rule.check(importedClasses);
        }
    }

    // ========================================================================
    // PACKAGE CONTAINMENT
    // ========================================================================

    @Nested
    @DisplayName("Package Containment Rules")
    class PackageContainmentTests {

        @Test
        @DisplayName("✅ Entities should only be in entity package")
        void entitiesInCorrectPackage() {
            ArchRule rule = classes()
                .that().areAnnotatedWith(jakarta.persistence.Entity.class)
                .should().resideInAPackage("..entity..");

            rule.check(importedClasses);
        }

        @Test
        @DisplayName("✅ Controllers should only be in controller package")
        void controllersInCorrectPackage() {
            ArchRule rule = classes()
                .that().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
                .should().resideInAPackage("..controller..");

            rule.check(importedClasses);
        }
    }
}
