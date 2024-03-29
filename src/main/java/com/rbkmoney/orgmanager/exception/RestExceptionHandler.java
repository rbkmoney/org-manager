package com.rbkmoney.orgmanager.exception;

import com.rbkmoney.swag.organizations.model.InlineResponse422;
import com.rbkmoney.swag.organizations.model.InlineResponse4222;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {AccessDeniedException.class})
    protected ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .build();
    }

    @ExceptionHandler(value = {ResourceNotFoundException.class})
    protected ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .build();
    }

    @ExceptionHandler(value = {BouncerException.class})
    protected ResponseEntity<Object> handleBouncerException(BouncerException ex, WebRequest request) {
        log.error(ex.getMessage(), ex.getCause());
        return ResponseEntity
                .status(HttpStatus.FAILED_DEPENDENCY)
                .build();
    }

    @ExceptionHandler(InviteExpiredException.class)
    public ResponseEntity<?> handleInviteExpiredException(InviteExpiredException ex) {
        InlineResponse422 badResponse = new InlineResponse422()
                .code(InlineResponse422.CodeEnum.INVITATIONEXPIRED)
                .message(String.format("Invite expired at: %s", ex.getExpiredAt()));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(badResponse);
    }

    @ExceptionHandler(InviteRevokedException.class)
    public ResponseEntity<?> handleInviteRevokedException(InviteRevokedException ex) {
        InlineResponse422 badResponse = new InlineResponse422()
                .code(InlineResponse422.CodeEnum.INVITATIONEXPIRED)
                .message(String.format("Invite revoked: %s", ex.getReason()));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(badResponse);
    }

    @ExceptionHandler(InviteAlreadyAcceptedException.class)
    public ResponseEntity<?> handleInviteAlreadyAcceptedException(InviteAlreadyAcceptedException ex) {
        InlineResponse422 badResponse = new InlineResponse422()
                .code(InlineResponse422.CodeEnum.INVITATIONEXPIRED)
                .message(String.format("Invite accepted at: %s", ex.getAcceptedAt()));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(badResponse);
    }

    @ExceptionHandler(LastRoleException.class)
    public ResponseEntity<?> handleLastRoleException(LastRoleException ex) {
        InlineResponse4222 badResponse = new InlineResponse4222()
                .code(InlineResponse4222.CodeEnum.ONLYROLELEFT)
                .message("Member have only one role");
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(badResponse);
    }

}
