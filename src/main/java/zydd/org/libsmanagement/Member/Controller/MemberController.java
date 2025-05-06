package zydd.org.libsmanagement.Member.Controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zydd.org.libsmanagement.Commons.DTO.ApiResponse;
import zydd.org.libsmanagement.Member.DTO.*;
import zydd.org.libsmanagement.Member.Service.Imp.MemberServiceImp;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberServiceImp memberServiceImp;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@RequestBody @Valid MemberLoginRequest request) {
        return ResponseEntity.ok(memberServiceImp.loginMember(request));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@RequestBody MemberRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memberServiceImp.registerMember(request));
    }

    @RolesAllowed({"ADMIN", "MEMBER"})
    @GetMapping("/{memberId}")
    public ResponseEntity<ApiResponse<MemberDetailResponse>> getMemberDetail(
            @PathVariable Long memberId) {
        return ResponseEntity.ok(memberServiceImp.getMemberDetail(memberId));
    }

    @RolesAllowed({"ADMIN", "MEMBER"})
    @PutMapping("/{memberId}")
    public ResponseEntity<ApiResponse<?>> updateMemberDetail(
            @RequestBody MemberUpdateRequest request,
            @PathVariable Long memberId) {
        return ResponseEntity.ok(memberServiceImp.updateMemberDetail(memberId, request));
    }

    @RolesAllowed("ADMIN")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<MemberDetailResponse>>> getAllMembers(Pageable pageable) {
        return ResponseEntity.ok(memberServiceImp.getAllMembers(pageable));
    }
}
