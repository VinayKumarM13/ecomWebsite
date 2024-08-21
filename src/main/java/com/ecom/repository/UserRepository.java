package com.ecom.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecom.model.UserdDtls;

public interface UserRepository extends JpaRepository<UserdDtls, Integer> {
	
	public UserdDtls findByEmail(String email);

	public List<UserdDtls> findByrole(String role);
	
	public UserdDtls findByResetToken(String token);
}
