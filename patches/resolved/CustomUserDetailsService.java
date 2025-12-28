package vn.edu.smd.core.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.edu.smd.core.entity.User;
import vn.edu.smd.core.repository.UserRepository;
import vn.edu.smd.shared.enums.UserRole; // <--- Import Enum để dùng cho default role

import java.util.Collections;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Tìm user trong database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user với email: " + email));

        // 2. Lấy Role từ Database (Production Ready)
        // Cần kiểm tra NULL để tránh lỗi NullPointerException nếu DB bị thiếu dữ liệu
        UserRole role = user.getPrimaryRole();
        
        // Fallback: Nếu user chưa có quyền, mặc định coi là STUDENT (hoặc quyền thấp nhất)
        if (role == null) {
            role = UserRole.STUDENT; 
        }

        // 3. Tạo Authority chuẩn Spring Security ("ROLE_" + Tên quyền)
        String roleName = "ROLE_" + role.name();
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(roleName));

        // 4. Trả về UserDetails
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword() != null ? user.getPassword() : "",
                authorities // Truyền danh sách quyền thật vào đây
        );
    }
}

/*
Origin/main variant (kept as reference):

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.entity.User;
import vn.edu.smd.core.repository.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return UserPrincipal.create(user);
    }

    @Transactional
    public UserDetails loadUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        return UserPrincipal.create(user);
    }
}
*/
