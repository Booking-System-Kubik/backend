package com.t1.officebooking.dto.request;

import com.t1.officebooking.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangingRoleRequest {
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    String email;

    @NotNull
    UserRole role;
}
