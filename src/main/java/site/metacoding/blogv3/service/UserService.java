package site.metacoding.blogv3.service;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import site.metacoding.blogv3.domain.user.User;
import site.metacoding.blogv3.domain.user.UserRepository;
import site.metacoding.blogv3.domain.visit.Visit;
import site.metacoding.blogv3.domain.visit.VisitRepository;
import site.metacoding.blogv3.handler.ex.CustomApiException;
import site.metacoding.blogv3.handler.ex.CustomException;
import site.metacoding.blogv3.util.UtilFileUpload;
import site.metacoding.blogv3.util.email.EmailUtil;
import site.metacoding.blogv3.web.dto.user.PasswordResetReqDto;
import site.metacoding.blogv3.web.dto.user.UpdateReqDto;

@RequiredArgsConstructor
@Service // IoC 등록
public class UserService {

    @Value("${file.path}")
    String uploadFolder;

    // DI
    private final VisitRepository visitRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EmailUtil emailUtil;

    @Transactional
    public void 패스워드초기화(PasswordResetReqDto passwordResetReqDto) {
        // 1. username, email 이 같은 것이 있는지 체크 (DB)
        Optional<User> userOp = userRepository.findByUsernameAndEmail(
                passwordResetReqDto.getUsername(),
                passwordResetReqDto.getEmail());

        // 2. 같은 것이 있다면 DB password 초기화 - BCrypt 해시 - update 하기 (DB)
        if (userOp.isPresent()) {
            User userEntity = userOp.get(); // 영속화
            String encPassword = bCryptPasswordEncoder.encode("9999");
            userEntity.setPassword(encPassword);
        } else {
            throw new CustomException("해당 이메일이 존재하지 않습니다.");
        }

        // 3. 초기화된 비밀번호 이메일로 전송
        emailUtil.sendEmail("getinthere@naver.com", "비밀번호 초기화", "초기화된 비밀번호 : 9999");
    } // 더티체킹 (update)

    @Transactional
    public void 회원정보수정(User principal, Integer id, UpdateReqDto updateReqDto) {
        // 1. 권한 확인
        if (principal.getId() != id) {
            throw new CustomApiException("해당 회원의 정보수정 권한이 없습니다.");
        }

        // 2. 회원 프로필이미지 경로로 변경
        if (updateReqDto.getProfileFile() != null) {
            String profileImg = UtilFileUpload.write(uploadFolder, updateReqDto.getProfileFile());
            principal.setProfileImg(profileImg);
        }

        // 3. 비밀번호 암호화
        String encPassword = 비밀번호암호화(updateReqDto.getPassword());
        principal.setPassword(encPassword);

        // 4. 정보 update
        principal.setEmail(updateReqDto.getEmail());

        userRepository.save(principal);
    }

    @Transactional
    public void 회원가입(User user) {
        // 1. save 한번
        String encPassword = 비밀번호암호화(user.getPassword());
        user.setPassword(encPassword);

        User userEntity = userRepository.save(user);

        // 2. save 두번
        Visit visit = new Visit();
        visit.setTotalCount(0L);
        visit.setUser(userEntity); // 터트리고 테스트 해보기
        visitRepository.save(visit);
    }

    public boolean 유저네임중복체크(String username) {
        Optional<User> userOp = userRepository.findByUsername(username);

        if (userOp.isPresent()) {
            return false;
        } else {
            return true;
        }
    }

    private String 비밀번호암호화(String pasword) {
        String encPassword = bCryptPasswordEncoder.encode(pasword); // 해쉬 알고리즘
        return encPassword;
    }
}
