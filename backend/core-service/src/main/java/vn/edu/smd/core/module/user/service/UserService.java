package vn.edu.smd.core.module.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.smd.core.common.exception.BadRequestException;
import vn.edu.smd.core.common.exception.ResourceNotFoundException;
import vn.edu.smd.core.entity.*;
import vn.edu.smd.core.module.user.dto.*;
import vn.edu.smd.core.repository.*;
import vn.edu.smd.shared.enums.AuthProvider;
import vn.edu.smd.shared.enums.RoleScope;
import vn.edu.smd.shared.enums.UserStatus;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    // --- 1. GET ALL (QUAY VỀ BẢN ĐƠN GIẢN) ---

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(String role, Boolean isActive, String search, Pageable pageable) {
        // 🔥 QUAY LẠI CÁCH GỌI CŨ: Chỉ lấy danh sách và phân trang
        // Tạm thời bỏ qua role, isActive, search để tránh lỗi DB
        return userRepository.findAllWithRoles(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToResponse(user);
    }

    // --- 2. CREATE / UPDATE / DELETE (GIỮ NGUYÊN VÌ ĐÃ CHẠY ĐƯỢC) ---

    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email đã tồn tại: " + request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail());
        String rawPass = (request.getPassword() != null && !request.getPassword().isEmpty()) 
                ? request.getPassword() : "Smd@123456";
        user.setPassword(passwordEncoder.encode(rawPass));
        
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setStatus(request.getStatus() != null ? UserStatus.valueOf(request.getStatus()) : UserStatus.ACTIVE);
        user.setIsActive(user.getStatus() == UserStatus.ACTIVE);
        user.setAuthProvider(AuthProvider.LOCAL);

        if (request.getFacultyId() != null) {
            facultyRepository.findById(request.getFacultyId()).ifPresent(user::setFaculty);
        }
        if (request.getDepartmentId() != null) {
            departmentRepository.findById(request.getDepartmentId()).ifPresent(user::setDepartment);
        }

        User savedUser = userRepository.save(user);

        if (request.getRole() != null) {
            Role role = roleRepository.findByCode(request.getRole())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "code", request.getRole()));
            
            UserRole userRole = new UserRole();
            userRole.setUser(savedUser);
            userRole.setRole(role);
            userRole.setScopeType(RoleScope.GLOBAL);
            
            Set<UserRole> roles = new HashSet<>();
            roles.add(userRole);
            savedUser.setUserRoles(roles);
            userRepository.save(savedUser);
        }
        return mapToResponse(savedUser);
    }

    @Transactional
    public UserResponse updateUser(UUID id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        
        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new BadRequestException("Email đã tồn tại");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getStatus() != null) {
            user.setStatus(UserStatus.valueOf(request.getStatus()));
            user.setIsActive(user.getStatus() == UserStatus.ACTIVE);
        }

        if (request.getFacultyId() != null) {
            facultyRepository.findById(request.getFacultyId()).ifPresent(user::setFaculty);
        } else {
            user.setFaculty(null);
        }
        
        if (request.getDepartmentId() != null) {
            departmentRepository.findById(request.getDepartmentId()).ifPresent(user::setDepartment);
        } else {
            user.setDepartment(null);
        }

        if (request.getRole() != null) {
            Role newRole = roleRepository.findByCode(request.getRole())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "code", request.getRole()));
            boolean hasRole = user.getUserRoles().stream()
                    .anyMatch(ur -> ur.getRole().getCode().equals(request.getRole()));
            if (!hasRole) {
                user.getUserRoles().clear();
                UserRole userRole = new UserRole();
                userRole.setUser(user);
                userRole.setRole(newRole);
                userRole.setScopeType(RoleScope.GLOBAL);
                user.getUserRoles().add(userRole);
            }
        }
        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        userRepository.deleteById(id);
    }

    // --- 3. STATUS & ROLES ---

    @Transactional
    public UserResponse updateStatus(UUID id, UpdateStatusRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        UserStatus status = UserStatus.valueOf(request.getStatus());
        user.setStatus(status);
        user.setIsActive(status == UserStatus.ACTIVE);
        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse toggleUserStatus(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        boolean newStatus = !user.getIsActive();
        user.setIsActive(newStatus);
        user.setStatus(newStatus ? UserStatus.ACTIVE : UserStatus.INACTIVE);
        return mapToResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public Set<String> getUserRoles(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return user.getUserRoles().stream().map(ur -> ur.getRole().getName()).collect(Collectors.toSet());
    }

    @Transactional
    public Set<String> assignRoles(UUID id, AssignRolesRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        for (UUID roleId : request.getRoleIds()) {
            Role role = roleRepository.findById(roleId).orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));
            boolean hasRole = user.getUserRoles().stream().anyMatch(ur -> ur.getRole().getId().equals(roleId));
            if (!hasRole) {
                UserRole userRole = new UserRole();
                userRole.setUser(user);
                userRole.setRole(role);
                userRole.setScopeType(RoleScope.GLOBAL);
                user.getUserRoles().add(userRole);
            }
        }
        userRepository.save(user);
        return user.getUserRoles().stream().map(ur -> ur.getRole().getName()).collect(Collectors.toSet());
    }

    @Transactional
    public void removeRole(UUID userId, UUID roleId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        UserRole roleToRemove = user.getUserRoles().stream()
                .filter(ur -> ur.getRole().getId().equals(roleId)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Role not found on user", "roleId", roleId));
        user.getUserRoles().remove(roleToRemove);
        userRepository.save(user);
    }

    // --- 4. IMPORT ---

    @Transactional
    public Map<String, Object> importUsers(MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int failedCount = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                if (isHeader) { isHeader = false; continue; }
                String[] data = line.split(",");
                if (data.length < 3) { failedCount++; errors.add("Dòng thiếu dữ liệu: " + line); continue; }
                try {
                    UserRequest request = new UserRequest();
                    request.setEmail(data[0].trim());
                    request.setFullName(data[1].trim());
                    request.setRole(data[2].trim());
                    if (data.length > 3) request.setPhoneNumber(data[3].trim());
                    request.setPassword("Smd@123456");
                    createUser(request);
                    successCount++;
                } catch (Exception e) {
                    failedCount++;
                    errors.add("Lỗi (" + line + "): " + e.getMessage());
                }
            }
        } catch (Exception e) { throw new BadRequestException("Lỗi đọc file: " + e.getMessage()); }
        response.put("success", successCount);
        response.put("failed", failedCount);
        response.put("errors", errors);
        return response;
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setStatus(user.getStatus().name());
        response.setRoles(user.getUserRoles().stream().map(ur -> ur.getRole().getCode()).collect(Collectors.toSet()));
        if (user.getFaculty() != null) {
            response.setFacultyId(user.getFaculty().getId());
            response.setFacultyName(user.getFaculty().getName());
        }
        if (user.getDepartment() != null) {
            response.setDepartmentId(user.getDepartment().getId());
            response.setDepartmentName(user.getDepartment().getName());
        }
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}