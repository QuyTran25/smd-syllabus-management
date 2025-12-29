package vn.edu.smd.core.module.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.edu.smd.core.module.auth.dto.RoleRequestDto;
import vn.edu.smd.core.module.auth.service.RoleRequestService;
import vn.edu.smd.core.security.UserPrincipal;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/role-requests")
@RequiredArgsConstructor
public class RoleRequestController {

    private final RoleRequestService roleRequestService;

    @PostMapping
    public ResponseEntity<RoleRequestDto> create(@RequestBody RoleRequestDto req) {
        // user must be the authenticated user (or admin can create)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = req.getUserId();
        if (userId == null && auth != null && auth.getPrincipal() instanceof UserPrincipal) {
            userId = ((UserPrincipal) auth.getPrincipal()).getId();
        }
        if (userId == null) return ResponseEntity.badRequest().build();
        RoleRequestDto created = roleRequestService.createRequest(userId, req.getRequestedRole(), req.getComment());
        return ResponseEntity.ok(created);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<RoleRequestDto>> listPending() {
        List<RoleRequestDto> list = roleRequestService.listPending();
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<RoleRequestDto> approve(@PathVariable UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID handledBy = null;
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) handledBy = ((UserPrincipal) auth.getPrincipal()).getId();
        return roleRequestService.approve(id, handledBy).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/deny")
    public ResponseEntity<RoleRequestDto> deny(@PathVariable UUID id, @RequestParam(required = false) String comment) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID handledBy = null;
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) handledBy = ((UserPrincipal) auth.getPrincipal()).getId();
        return roleRequestService.deny(id, handledBy, comment).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
