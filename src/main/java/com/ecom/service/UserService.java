package com.ecom.service;

import java.util.List;

import com.ecom.model.UserdDtls;

public interface UserService {

	public UserdDtls saveUser(UserdDtls user);
	
	public UserdDtls getUserByEmail(String email);
	
	public List<UserdDtls> getUsers(String role);

	public Boolean updateAccountStatus(Integer id, Boolean status);
	
	public void increaseFailedAttempt(UserdDtls user);
	
	public void userAccountLock(UserdDtls user);
	
	public boolean unlockAccountTimeExpired(UserdDtls user);

	public void resetAttempt(int userId);

	public void updatedUserResetToken(String email, String resetToken);
	
	public UserdDtls getUserByToken(String token);
	
	public UserdDtls updateUser(UserdDtls user);
}

