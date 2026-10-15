package com.xuecheng.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * @description 全局异常处理器
 * @author Mr.M
 * @date 2022/9/6 11:29
 * @version 1.0
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

   @ResponseBody
   @ExceptionHandler(XueChengPlusException.class)
   @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
   public RestErrorResponse customException(XueChengPlusException e) {
      log.error("【系统异常】{}",e.getErrMessage(),e);
      return new RestErrorResponse(e.getErrMessage());

   }

   @ResponseBody
   @ExceptionHandler(Exception.class)
   @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
   public RestErrorResponse exception(Exception e) {

      log.error("【系统异常】{}",e.getMessage(),e);

      return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());

   }
   @ResponseBody
   @ExceptionHandler(MethodArgumentNotValidException.class)
   @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
   public RestErrorResponse validException(MethodArgumentNotValidException e){
//      String msg = e.getBindingResult().getFieldError().getDefaultMessage();
      List<String> errorList = new ArrayList<>();
      e.getBindingResult().getFieldErrors().forEach(
              error -> errorList.add(error.getDefaultMessage())
      );
      String msg = String.join(",", errorList);
//      log.error("【参数验证失败】{}",msg);
      return new RestErrorResponse(msg);
   }

}