package com.ecom.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecom.model.UserdDtls;
import com.ecom.repository.UserRepository;
import com.ecom.service.UserService;
import com.ecom.util.AppConstant;

@Service
public class UserServiceImpl implements UserService{

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Override
	public UserdDtls saveUser(UserdDtls user) {
		user.setRole("ROLE_USER");
		user.setIsEnable(true);
		user.setAccountNonLocked(true);
		user.setFailedAttempt(0);
		String encodePasseord = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodePasseord);
		UserdDtls saveUser = userRepository.save(user);
		return saveUser;
	}

	@Override
	public UserdDtls getUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public List<UserdDtls> getUsers(String role) {

		return userRepository.findByrole(role);
	}

	@Override
	public Boolean updateAccountStatus(Integer id, Boolean status) {
		Optional<UserdDtls> findByuser = userRepository.findById(id);
		if(findByuser.isPresent()) {
			UserdDtls userdDtls = findByuser.get();
			userdDtls.setIsEnable(status);
			userRepository.save(userdDtls);
			return true;
		}
		return false;
	}

	@Override
	public void increaseFailedAttempt(UserdDtls user) {
		int attempt = user.getFailedAttempt() + 1;
		user.setFailedAttempt(attempt);
		userRepository.save(user);
		
		
	}

	@Override
	public void userAccountLock(UserdDtls user) {
		user.setAccountNonLocked(false);
		user.setLockTime(new Date());
		userRepository.save(user);
		
	}

	@Override
	public boolean unlockAccountTimeExpired(UserdDtls user) {
		long lockTime = user.getLockTime().getTime();
		long unLockTime = lockTime + AppConstant.UNLOCK_DURATION_TIME;
		
		long currentTime = System.currentTimeMillis();
		
		if(unLockTime < currentTime) {
			user.setAccountNonLocked(true);
			user.setFailedAttempt(0);
			user.setLockTime(null);
			userRepository.save(user);
			return true;
		}
		return false;
	}

	@Override
	public void resetAttempt(int userId) {
		
		
	}

	@Override
	public void updatedUserResetToken(String email, String resetToken) {
		UserdDtls findbyEmail = userRepository.findByEmail(email);
		findbyEmail.setResetToken(resetToken);
		userRepository.save(findbyEmail);
		
	}

	@Override
	public UserdDtls getUserByToken(String token) {
		return userRepository.findByResetToken(token);
	
	}

	@Override
	public UserdDtls updateUser(UserdDtls user) {
		return userRepository.save(user);
		
	}

}
