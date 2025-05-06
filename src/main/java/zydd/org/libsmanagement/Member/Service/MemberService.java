package zydd.org.libsmanagement.Member.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import zydd.org.libsmanagement.Commons.DTO.ApiResponse;
import zydd.org.libsmanagement.Member.DTO.*;

public interface MemberService {
    ApiResponse<?> registerMember(MemberRegisterRequest request);
    ApiResponse<TokenResponse> loginMember(MemberLoginRequest request);

    ApiResponse<MemberDetailResponse> getMemberDetail(Long memberId);
    ApiResponse<?> updateMemberDetail(Long memberId, MemberUpdateRequest request);
    ApiResponse<Page<MemberDetailResponse>> getAllMembers(Pageable pageable);
}
