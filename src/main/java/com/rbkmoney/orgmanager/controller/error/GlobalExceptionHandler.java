package com.rbkmoney.orgmanager.controller.error;

import com.rbkmoney.swag.organizations.model.InlineResponse422;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(InviteExpiredException.class)
    public ResponseEntity<?> handle(InviteExpiredException ex) {
        InlineResponse422 badResponse = new InlineResponse422()
              .code(InlineResponse422.CodeEnum.INVITATIONEXPIRED)
              .message("Invite expired at: " + ex.getExpiredAt());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(badResponse);
    }

}
