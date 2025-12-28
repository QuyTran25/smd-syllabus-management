package vn.edu.smd.core.module.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
<<<<<<< HEAD
import vn.edu.smd.core.module.auth.dto.UserInfoResponse;
=======
>>>>>>> origin/main

@Data
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
<<<<<<< HEAD
    private UserInfoResponse user;
=======
>>>>>>> origin/main

    public AuthResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
<<<<<<< HEAD

    public AuthResponse(String accessToken, String refreshToken, UserInfoResponse user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }
=======
>>>>>>> origin/main
}
