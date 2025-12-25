package vn.edu.smd.core.module.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.entity.User;
import vn.edu.smd.core.repository.UserRepository;
import vn.edu.smd.shared.dto.auth.LoginRequest;
import vn.edu.smd.shared.dto.auth.LoginResponse;
import vn.edu.smd.shared.dto.user.UserDTO;
import vn.edu.smd.shared.enums.UserRole;
import vn.edu.smd.shared.enums.UserStatus;
import vn.edu.smd.core.common.util.JwtUtils;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            if (user.getPassword() != null && user.getPassword().equals(request.getPassword())) {
                
                LoginResponse response = new LoginResponse();
                // 1. Tạo Access Token
                String accessToken = jwtUtils.generateToken(user.getEmail());
                response.setAccessToken(accessToken);

                // 2. Map dữ liệu User sang DTO
                UserDTO userDto = new UserDTO();
                userDto.setId(user.getId().toString());
                userDto.setEmail(user.getEmail());
                userDto.setFullName(user.getFullName());
                userDto.setUsername(user.getUsername() != null ? user.getUsername() : user.getEmail());

                // --- XỬ LÝ KHOA / BỘ MÔN (Fix Null & Type Mismatch) ---
                if (user.getDepartment() != null) {
                    userDto.setDepartmentId(user.getDepartment().getId().toString());
                }

                if (user.getFaculty() != null) {
                    userDto.setFacultyId(user.getFaculty().getId().toString());
                }
                // -----------------------------------------------------

                // --- LOGIC PHÂN QUYỀN CHUẨN (Production Ready) ---
                // Thay vì gán cứng LECTURER, ta lấy từ DB
                UserRole role = user.getPrimaryRole();
                
                // Nếu role trong DB null, gán tạm STUDENT để tránh lỗi Frontend
                if (role == null) {
                    role = UserRole.STUDENT;
                }

                userDto.setPrimaryRole(role); 
                userDto.setRoles(List.of(role));
                // -------------------------------------------------

                userDto.setStatus(UserStatus.ACTIVE); 

                response.setUser(userDto);
                return ResponseEntity.ok(response);
            }
        }
        
        return ResponseEntity.status(401).body("Thông tin đăng nhập không chính xác");
    }
}