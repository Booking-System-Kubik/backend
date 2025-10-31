package com.t1.officebooking.authorization.dto;

import com.t1.officebooking.model.Location;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequest {
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @NotBlank(message = "Full name cannot be blank")
    @Size(min = 5, max = 100, message = "Full name must be between 5 and 100 characters")
    private String fullName;

    @NotBlank(message = "Position cannot be blank")
    @Size(max = 100, message = "Position must be less than or equal to 100 characters")
    private String position;

    private Long location;

    // Organization selection: either choose existing organizationId or provide new organizationName
    private Long organizationId;
    private String organizationName;
}
