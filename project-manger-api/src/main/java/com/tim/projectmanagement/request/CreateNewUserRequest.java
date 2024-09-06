package com.tim.projectmanagement.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateNewUserRequest {
    @NotEmpty(message = "First Name is required.")
    private String firstName;

    @NotEmpty(message = "Last Name is required.")
    private String lastName;

    @NotEmpty(message = "Email is required.")
    @Email(message = "Invalid email.")
    private String email;

    @NotEmpty(message = "Password is required.")
    private String password;
    private String imageUrl;
}
