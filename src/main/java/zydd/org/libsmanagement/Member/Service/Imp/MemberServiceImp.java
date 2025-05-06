package zydd.org.libsmanagement.Member.Service.Imp;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import zydd.org.libsmanagement.Commons.DTO.ApiResponse;
import zydd.org.libsmanagement.Commons.Jwt.JwtUtils;
import zydd.org.libsmanagement.Member.DTO.*;
import zydd.org.libsmanagement.Member.Model.Member;
import zydd.org.libsmanagement.Member.Model.Role;
import zydd.org.libsmanagement.Member.Repository.MemberRepository;

@Service
public class MemberServiceImp implements zydd.org.libsmanagement.Member.Service.MemberService {

    private final MemberRepository memberRepository;
    private final JwtUtils jwtUtil;

    public MemberServiceImp(MemberRepository memberRepository, JwtUtils jwtUtil) {
        this.memberRepository = memberRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public ApiResponse<?> registerMember(MemberRegisterRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            return ApiResponse.error(400, "Email already registered", null);
        }

        Role role = request.getRole() == null ? Role.MEMBER : request.getRole();

        Member newMember = Member.builder()
                .email(request.getEmail())
                .name(request.getName())
                .password(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()))
                .role(role)
                .build();

        memberRepository.save(newMember);
        return ApiResponse.success(201, "Member registered successfully", null);
    }

    @Override
    public ApiResponse<TokenResponse> loginMember(MemberLoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        if (!BCrypt.checkpw(request.getPassword(), member.getPassword())) {
            return ApiResponse.error(401, "Invalid credentials", null);
        }

        String token = jwtUtil.generateToken(member);
        TokenResponse response = new TokenResponse(token);
        return ApiResponse.success("Login success.", response);
    }

    @Override
    public ApiResponse<MemberDetailResponse> getMemberDetail(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new UsernameNotFoundException("Member not found"));

        MemberDetailResponse response = MemberDetailResponse.builder()
                .email(member.getEmail())
                .name(member.getName())
                .build();
        return ApiResponse.success("Data fetched successfully", response);
    }

    @Override
    public ApiResponse<?> updateMemberDetail(Long memberId, MemberUpdateRequest request) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new UsernameNotFoundException("Member not found"));

        member.setName(request.getName());
        member.setEmail(request.getEmail());
        member.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
        memberRepository.save(member);
        return ApiResponse.success("Member updated successfully", null);
    }

    @Override
    public ApiResponse<Page<MemberDetailResponse>> getAllMembers(Pageable pageable) {
        Page<MemberDetailResponse> members =memberRepository.findAll(pageable)
                .map(member -> MemberDetailResponse.builder()
                        .email(member.getEmail())
                        .name(member.getName())
                        .build());

        return ApiResponse.success("Data fetched successfully", members);
    }

}
