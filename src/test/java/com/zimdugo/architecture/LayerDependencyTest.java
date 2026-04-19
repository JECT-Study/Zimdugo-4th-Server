package com.zimdugo.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LayerDependencyTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setUp() {
        importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.zimdugo");
    }

    @Nested
    @DisplayName("Layer dependency rules")
    class LayerDependencyRules {

        @Test
        @DisplayName("domain does not depend on entrypoint/application/infrastructure")
        void domain_should_not_depend_on_other_layers() {
            noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..entrypoint..", "..application..", "..infrastructure..")
                .allowEmptyShould(true)
                .check(importedClasses);
        }

        @Test
        @DisplayName("application does not depend on entrypoint")
        void application_should_not_depend_on_entrypoint() {
            noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..entrypoint..")
                .allowEmptyShould(true)
                .check(importedClasses);
        }

        @Test
        @DisplayName("entrypoint does not depend on infrastructure")
        void entrypoint_should_not_depend_on_infrastructure() {
            noClasses()
                .that().resideInAPackage("..entrypoint..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..infrastructure..")
                .allowEmptyShould(true)
                .check(importedClasses);
        }

        @Test
        @DisplayName("infrastructure does not depend on entrypoint/application")
        void infrastructure_should_not_depend_on_upper_layers() {
            noClasses()
                .that().resideInAPackage("..infrastructure..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..entrypoint..", "..application..")
                .allowEmptyShould(true)
                .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Class location rules")
    class ClassLocationRules {

        @Test
        @DisplayName("@RestController classes should be in entrypoint")
        void rest_controllers_should_reside_in_entrypoint() {
            classes()
                .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .should().resideInAPackage("..entrypoint..")
                .allowEmptyShould(true)
                .check(importedClasses);
        }

        @Test
        @DisplayName("@Entity classes should be in domain")
        void entities_should_reside_in_domain() {
            classes()
                .that().areAnnotatedWith("jakarta.persistence.Entity")
                .should().resideInAPackage("..domain..")
                .allowEmptyShould(true)
                .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Domain cycle rules")
    class DomainSliceRules {

        @Test
        @DisplayName("no cyclic dependencies between domains")
        void no_circular_dependencies_between_domains() {
            slices()
                .matching("com.zimdugo.(*)..")
                .should().beFreeOfCycles()
                .allowEmptyShould(true)
                .check(importedClasses);
        }
    }
}
