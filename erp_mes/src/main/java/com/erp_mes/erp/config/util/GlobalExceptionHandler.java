package com.erp_mes.erp.config.util;

import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public String handleMaxSizeException(MaxUploadSizeExceededException e,
	                                     HttpServletRequest request,
	                                     RedirectAttributes redirectAttributes) {
	    
	    String path = request.getRequestURI();
	    redirectAttributes.addFlashAttribute("error", "업로드 가능한 파일 용량을 초과했습니다.");

	 // 사용자가 요청한 URL 그대로 redirect
        String redirectUrl = "redirect:" + request.getRequestURI();

        // 쿼리스트링 포함될 경우 처리
        if (request.getQueryString() != null) {
            redirectUrl += "?" + request.getQueryString();
        }

        return redirectUrl;
	}
}
