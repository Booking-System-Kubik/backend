package com.t1.officebooking.authorization.dto;

import com.t1.officebooking.model.Location;
import com.t1.officebooking.model.User;
import com.t1.officebooking.model.UserRole;
import lombok.Data;

import java.util.Set;

@Data
public class UserDataResponse {
    private String email;
    private String fullName;
    private Long locationId;
    private String locationName;
    private Set<UserRole> roles;

    public UserDataResponse(User user) {
        this.email = user.getEmail();
        this.roles = user.getRoles();
        this.fullName = user.getFullName();
        this.locationName = (user.getLocation() != null)
                ? user.getLocation().getName()
                :null;
        this.locationId = (user.getLocation() != null)
                ? user.getLocation().getId()
                :null;
    }
}

