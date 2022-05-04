package site.metacoding.blogv3.web.dto.user;

import javax.validation.constraints.NotBlank;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateReqDto {

    // 유효성 검사 필요
    @NotBlank
    private String password;

    private MultipartFile profileFile;

    @NotBlank
    private String email;

}
