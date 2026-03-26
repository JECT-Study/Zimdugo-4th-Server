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
    @DisplayName("레이어 의존성 규칙")
    class LayerDependencyRules {

        @Test
        @DisplayName("domain 레이어는 다른 레이어에 의존하지 않는다")
        void domain_should_not_depend_on_other_layers() {
            noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                    "..entrypoint..", "..application..", "..infrastructure.."
                )
                .allowEmptyShould(true)
                .check(importedClasses);
        }

        @Test
        @DisplayName("application 레이어는 domain만 의존할 수 있다")
        void application_should_only_depend_on_domain() {
            noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..entrypoint..", "..infrastructure..")
                .allowEmptyShould(true)
                .check(importedClasses);
        }

        @Test
        @DisplayName("entrypoint 레이어는 application만 의존할 수 있다 (계층 건너뛰기 금지)")
        void entrypoint_should_only_depend_on_application() {
            noClasses()
                .that().resideInAPackage("..entrypoint..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..domain..", "..infrastructure..")
                .allowEmptyShould(true)
                .check(importedClasses);
        }

        @Test
        @DisplayName("infrastructure 레이어는 domain만 의존할 수 있다")
        void infrastructure_should_only_depend_on_domain() {
            noClasses()
                .that().resideInAPackage("..infrastructure..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                    "..entrypoint..", "..application.."
                )
                .allowEmptyShould(true)
                .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("클래스 위치 규칙")
    class ClassLocationRules {

        @Test
        @DisplayName("@RestController는 entrypoint 패키지에만 위치해야 한다")
        void rest_controllers_should_reside_in_entrypoint() {
            classes()
                .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .should().resideInAPackage("..entrypoint..")
                .allowEmptyShould(true)
                .check(importedClasses);
        }

        @Test
        @DisplayName("@Entity는 domain 패키지에만 위치해야 한다")
        void entities_should_reside_in_domain() {
            classes()
                .that().areAnnotatedWith("jakarta.persistence.Entity")
                .should().resideInAPackage("..domain..")
                .allowEmptyShould(true)
                .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("도메인 간 의존성 규칙")
    class DomainSliceRules {

        @Test
        @DisplayName("도메인 간 순환 의존이 없어야 한다")
        void no_circular_dependencies_between_domains() {
            slices()
                .matching("com.zimdugo.(*)..")
                .should().beFreeOfCycles()
                .allowEmptyShould(true)
                .check(importedClasses);
        }
    }
}