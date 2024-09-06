package com.tim.projectmanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String imageUrl;
    private boolean isEnabled;
    private boolean isNotLocked;
    private LocalDateTime createdAt;
    private String roleName;
    private String permissions;
}
