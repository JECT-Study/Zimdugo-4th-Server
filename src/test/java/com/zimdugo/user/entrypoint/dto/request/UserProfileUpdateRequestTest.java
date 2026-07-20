package com.zimdugo.user.entrypoint.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserProfileUpdateRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsBlankProfileImageUrl() {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest(" ");

        assertThat(validator.validate(request))
            .extracting(violation -> violation.getMessage())
            .contains("validation.profile_update_required");
    }

    @Test
    void rejectsNonHttpsProfileImageUrl() {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest("http://cdn.zimdugo.com/profile.png");

        assertThat(validator.validate(request))
            .extracting(violation -> violation.getMessage())
            .contains("validation.invalid_profile_image_url");
    }

    @Test
    void acceptsHttpsProfileImageUrl() {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest("https://cdn.zimdugo.com/profile.png");

        assertThat(validator.validate(request)).isEmpty();
    }
}
