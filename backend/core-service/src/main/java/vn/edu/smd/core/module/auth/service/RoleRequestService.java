package vn.edu.smd.core.module.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.smd.core.module.auth.dto.RoleRequestDto;
import vn.edu.smd.core.module.auth.entity.RoleRequest;
import vn.edu.smd.core.module.auth.repository.RoleRequestRepository;
import vn.edu.smd.core.entity.User;
import vn.edu.smd.core.repository.UserRepository;
import vn.edu.smd.shared.enums.UserRole;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleRequestService {

    private final RoleRequestRepository roleRequestRepository;
    private final UserRepository userRepository;

    @Transactional
    public RoleRequestDto createRequest(UUID userId, String requestedRole, String comment) {
        RoleRequest r = RoleRequest.builder()
                .userId(userId)
                .requestedRole(requestedRole)
                .status("PENDING")
                .comment(comment)
                .build();
        RoleRequest saved = roleRequestRepository.save(r);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<RoleRequestDto> listPending() {
        return roleRequestRepository.findByStatus("PENDING").stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public Optional<RoleRequestDto> approve(UUID requestId, UUID handledBy) {
        RoleRequest req = roleRequestRepository.findById(requestId).orElse(null);
        if (req == null) return Optional.empty();

        // find user and set primaryRole if possible
        User user = userRepository.findById(req.getUserId()).orElse(null);
        if (user != null) {
            try {
                user.setPrimaryRole(UserRole.valueOf(req.getRequestedRole()));
                userRepository.save(user);
            } catch (Exception ignored) { }
        }

        req.setStatus("APPROVED");
        req.setHandledBy(handledBy);
        req.setHandledAt(LocalDateTime.now());
        roleRequestRepository.save(req);
        return Optional.of(toDto(req));
    }

    @Transactional
    public Optional<RoleRequestDto> deny(UUID requestId, UUID handledBy, String comment) {
        RoleRequest req = roleRequestRepository.findById(requestId).orElse(null);
        if (req == null) return Optional.empty();
        req.setStatus("DENIED");
        req.setHandledBy(handledBy);
        req.setHandledAt(LocalDateTime.now());
        if (comment != null) req.setComment(comment);
        roleRequestRepository.save(req);
        return Optional.of(toDto(req));
    }

    private RoleRequestDto toDto(RoleRequest r) {
        return new RoleRequestDto(r.getId(), r.getUserId(), r.getRequestedRole(), r.getStatus(), r.getComment());
    }
}
