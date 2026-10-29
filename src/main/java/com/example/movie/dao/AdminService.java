package com.example.movie.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.movie.exception.AdminNotFoundException;
import com.example.movie.Repository.AdminRepository;
import com.example.movie.model.Admin;

@Service
public class AdminService {

	@Autowired
	AdminRepository adminRepository;
	
	public List<Admin> findAll()
	{
		return adminRepository.findAll();
	}
	
	public Optional<Admin> findByName(String name)
	{
		return adminRepository.findByName(name);
	}
	
	public Optional<Admin> findByUsername(String username)
	{
		return adminRepository.findByUsername(username);
	}
	
	@Transactional(readOnly = true)
	public Admin findById(Integer id)
	{
		return adminRepository.findById(id)
				.orElseThrow(() -> new AdminNotFoundException(id));
	}
	
	public List<Admin> searchByKeyword(String keyword)
	{
		if(keyword == null || keyword.isBlank())
		{
			return adminRepository.findAll();
		}
		return adminRepository.searchByKeyword(keyword);
	}
	
	@Transactional
	public Admin create(Admin admin)
	{
		Optional<Admin> ad = adminRepository.findByUsername(admin.getUsername());
		if(ad.isPresent())
		{
			throw new IllegalArgumentException("帳號已存在：" + admin.getUsername());
		}
		return adminRepository.save(admin);
		
	}
	
	@Transactional
	public Optional<Admin> delete(String username)
	{		
		Optional<Admin> ad = adminRepository.findByUsername(username);
		if(ad.isPresent())
		{
			adminRepository.delete(ad.get());
		}
		
		return ad;
	}
	
	@Transactional
	public Admin update(Integer adminId, Admin admin)
	{
		Admin existing = adminRepository.findById(adminId)
				.orElseThrow(() -> new AdminNotFoundException(adminId));
		
		Optional<Admin> sameName = adminRepository.findByUsername(admin.getUsername());
		   if (sameName.isPresent() && !sameName.get().getAdminId().equals(admin.adminId)) {
		       throw new IllegalArgumentException("帳號已被使用：" + admin.getUsername());
		   }
		   
		existing.setUsername(admin.getUsername());
		existing.setName(admin.getName());
		
		if (admin.getPassword() != null && !admin.getPassword().isBlank()) {
	        existing.setPassword(admin.getPassword());
	    }

	    return adminRepository.save(existing);
		
	}
}
