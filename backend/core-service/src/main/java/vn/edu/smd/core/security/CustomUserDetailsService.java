package vn.edu.smd.core.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.entity.User;
import vn.edu.smd.core.repository.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Sử dụng phương thức của team để nạp kèm Roles, tránh lỗi Lazy loading
        User user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Logic HEAD: An toàn hơn (Check null trước khi lấy length để tránh lỗi 500)
        System.out.println("--- KIEM TRA MAT KHAU DB ---");
        System.out.println("Hash: " + user.getPasswordHash());
        System.out.println("Do dai: " + (user.getPasswordHash() != null ? user.getPasswordHash().length() : 0));

        return UserPrincipal.create(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(UUID id) {
        // Sử dụng phương thức của team để nạp kèm Roles bằng ID
        User user = userRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        return UserPrincipal.create(user);
    }
}