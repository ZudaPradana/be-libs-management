package zydd.org.libsmanagement;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import zydd.org.libsmanagement.Commons.DTO.ApiResponse;
import zydd.org.libsmanagement.Commons.Jwt.JwtUtils;
import zydd.org.libsmanagement.Member.DTO.*;
import zydd.org.libsmanagement.Member.Model.Member;
import zydd.org.libsmanagement.Member.Repository.MemberRepository;
import zydd.org.libsmanagement.Member.Service.Imp.MemberServiceImp;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {
    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JwtUtils jwtUtil;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberServiceImp memberService;

    @Test
    void testRegisterMember_whenEmailAlreadyExists_thenReturnErrorResponse() {
        MemberRegisterRequest request = new MemberRegisterRequest();
        request.setEmail("test@example.com");

        when(memberRepository.existsByEmail("test@example.com")).thenReturn(true);

        ApiResponse<?> response = memberService.registerMember(request);

        assertEquals(400, response.getCode());
        assertEquals("Email already registered", response.getMessage());
    }

    @Test
    void testLoginMember_whenCredentialsAreValid_thenReturnToken() {
        MemberLoginRequest request = new MemberLoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("password");

        Member member = Member.builder()
                .email("user@example.com")
                .password(BCrypt.hashpw("password", BCrypt.gensalt()))
                .build();

        when(memberRepository.findByEmail("user@example.com")).thenReturn(Optional.of(member));
        when(jwtUtil.generateToken(any(Member.class))).thenReturn("dummy-token");

        ApiResponse<TokenResponse> response = memberService.loginMember(request);

        assertEquals(200, response.getCode());
        assertEquals("Login success.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("dummy-token", response.getData().getToken());
    }

    @Test
    void getMemberDetail_WhenMemberExists_ShouldReturnData() {
        Long memberId = 1L;
        Member mockMember = Member.builder()
                .id(memberId)
                .email("test@example.com")
                .name("Test User")
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(mockMember));

        ApiResponse<MemberDetailResponse> response = memberService.getMemberDetail(memberId);

        assertEquals("Data fetched successfully", response.getMessage());
        assertEquals("test@example.com", response.getData().getEmail());
        assertEquals("Test User", response.getData().getName());
        verify(memberRepository, times(1)).findById(memberId);
    }

    @Test
    void getMemberDetail_WhenMemberNotExists_ShouldThrowException() {
        Long memberId = 99L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            memberService.getMemberDetail(memberId);
        });
        verify(memberRepository, times(1)).findById(memberId);
    }

    @Test
    void updateMemberDetail_WhenValidRequest_ShouldUpdateMember() {
        Long memberId = 1L;
        Member mockMember = Member.builder()
                .id(memberId)
                .email("old@example.com")
                .name("Old Name")
                .password("oldPassword")
                .build();

        MemberUpdateRequest request = MemberUpdateRequest.builder()
                .name("New Name")
                .email("new@example.com")
                .password("newPassword")
                .build();

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(mockMember));
        when(memberRepository.save(memberCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        ApiResponse<?> response = memberService.updateMemberDetail(memberId, request);

        assertEquals("Member updated successfully", response.getMessage());

        verify(memberRepository, times(1)).findById(memberId);
        verify(memberRepository, times(1)).save(any(Member.class));

        Member savedMember = memberCaptor.getValue();

        assertEquals("new@example.com", savedMember.getEmail());
        assertEquals("New Name", savedMember.getName());

        assertTrue(BCrypt.checkpw("newPassword", savedMember.getPassword()));
    }

    @Test
    void getAllMembers_ShouldReturnPagedData() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Member> mockMembers = Arrays.asList(
                Member.builder().email("user1@test.com").name("User 1").build(),
                Member.builder().email("user2@test.com").name("User 2").build()
        );
        Page<Member> mockPage = new PageImpl<>(mockMembers, pageable, mockMembers.size());

        when(memberRepository.findAll(pageable)).thenReturn(mockPage);

        ApiResponse<Page<MemberDetailResponse>> response = memberService.getAllMembers(pageable);

        assertEquals("Data fetched successfully", response.getMessage());
        assertEquals(2, response.getData().getContent().size());
        assertEquals("user1@test.com", response.getData().getContent().get(0).getEmail());
        verify(memberRepository, times(1)).findAll(pageable);
    }

}
