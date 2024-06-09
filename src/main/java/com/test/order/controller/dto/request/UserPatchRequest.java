package com.test.order.controller.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
public class UserPatchRequest {

    // todo add customized validator to check at least one of the variable is required
    @Length(max = 30)
    private String nickname;

    @Length(max = 100)
    private String comment;
}
