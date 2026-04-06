package com.zimdugo.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

class LayerDependencyTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setUp() {
        importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.zimdugo");
    }

    @Nested
    @DisplayName("Layer Dependency Rules")
    class LayerDependencyRules {

        @Test
        @DisplayName("domain should not depend on other layers")
        void domain_should_not_depend_on_other_layers() {
            noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..entrypoint..", "..application..", "..infrastructure..")
                .allowEmptyShould(true)
                .check(importedClasses);
        }

        @Test
        @DisplayName("application should not depend on entrypoint or infrastructure")
        void application_should_only_depend_on_domain() {
            noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..entrypoint..", "..infrastructure..")
                .allowEmptyShould(true)
                .check(importedClasses);
        }

        @Test
        @DisplayName("entrypoint should not depend on domain or infrastructure")
        void entrypoint_should_only_depend_on_application() {
            noClasses()
                .that().resideInAPackage("..entrypoint..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..domain..", "..infrastructure..")
                .allowEmptyShould(true)
                .check(importedClasses);
        }

        @Test
        @DisplayName("infrastructure should not depend on entrypoint or application")
        void infrastructure_should_only_depend_on_domain() {
            noClasses()
                .that().resideInAPackage("..infrastructure..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..entrypoint..", "..application..")
                .allowEmptyShould(true)
                .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Class Location Rules")
    class ClassLocationRules {

        @Test
        @DisplayName("@RestController should reside in entrypoint package")
        void rest_controllers_should_reside_in_entrypoint() {
            classes()
                .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .should().resideInAPackage("..entrypoint..")
                .allowEmptyShould(true)
                .check(importedClasses);
        }

        @Test
        @DisplayName("@Entity should reside in infrastructure package")
        void entities_should_reside_in_infrastructure() {
            classes()
                .that().areAnnotatedWith("jakarta.persistence.Entity")
                .should().resideInAPackage("..infrastructure..")
                .allowEmptyShould(true)
                .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Slice Rules")
    class DomainSliceRules {

        @Test
        @DisplayName("slices should be free of cycles")
        void no_circular_dependencies_between_domains() {
            slices()
                .matching("com.zimdugo.(*)..")
                .should().beFreeOfCycles()
                .allowEmptyShould(true)
                .check(importedClasses);
        }
    }
}
