package com.ecom.config;

import java.io.IOException;

import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.ecom.model.UserdDtls;
import com.ecom.repository.UserRepository;
import com.ecom.service.UserService;
import com.ecom.util.AppConstant;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthFailureHandlerImpl extends SimpleUrlAuthenticationFailureHandler
{

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserService userService;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {

		String email = request.getParameter("username");

		UserdDtls userdDtls = userRepository.findByEmail(email);

		if(userdDtls.getIsEnable()) {

			if(userdDtls.getAccountNonLocked()) {

				if(userdDtls.getFailedAttempt() < AppConstant.ATTEMPTED_TIME)
				{	
					userService.increaseFailedAttempt(userdDtls);
				}else {
					userService.userAccountLock(userdDtls);
					exception = new LockedException("your account is Locked || failed attempt 3");
				}

			}else {
				if(userService.unlockAccountTimeExpired(userdDtls)) {
					exception = new LockedException("your account is unLocked || please try to login");
				}else {

					exception = new LockedException("your account is Locked || please try after sometimes");
				}
			}

		} else {

			exception = new LockedException("your account is inactive");

		}
		
		super.setDefaultFailureUrl("/signin?error");
		super.onAuthenticationFailure(request, response, exception);
	}

}
