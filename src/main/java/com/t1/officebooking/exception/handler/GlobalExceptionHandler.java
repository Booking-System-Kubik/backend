package com.t1.officebooking.exception.handler;

import com.t1.officebooking.exception.*;
import jakarta.persistence.EntityExistsException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {
    @ExceptionHandler(SlotAlreadyBookedException.class)
    public ResponseEntity<?> handleSlotAlreadyBookedException(SlotAlreadyBookedException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<?> handleEntityExistsException(EntityExistsException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RefactoringForeignBookingsException.class)
    public ResponseEntity<?> handleRefactoringForeignBookingsException(RefactoringForeignBookingsException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(RoleAssignmentViolationException.class)
    public ResponseEntity<?> handleRoleAssignmentViolationException(RoleAssignmentViolationException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AdminAuthorityAbusingException.class)
    public ResponseEntity<?> handleAdminAuthorityAbusingException(AdminAuthorityAbusingException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(IncorrectBookingException.class)
    public ResponseEntity<?> handleIncorrectBookingException(IncorrectBookingException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
