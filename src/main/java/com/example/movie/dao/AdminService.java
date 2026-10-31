package com.example.movie.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.movie.exception.AdminNotFoundException;
import com.example.movie.Repository.AdminRepository;
import com.example.movie.model.Admin;
import com.example.movie.util.PasswordUtil;

@Service
public class AdminService {

	@Autowired
	AdminRepository adminRepository;
	
	public Page<Admin> findAll(int page, int size)
	{
		return adminRepository.findAll(buildPageable(page, size));
	}
	
	private Pageable buildPageable(int page, int size)
	{
		if (page < 1) page = 1;
		if (size < 1 || size > 100) size = 10;
		return PageRequest.of(page - 1, size, Sort.by("adminId").ascending());
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
	
	public Page<Admin> searchByKeyword(String keyword, int page, int size)
	{
		if(keyword == null || keyword.isBlank())
		{
			return adminRepository.findAll(buildPageable(page, size)); //page 目前頁碼，size 目前筆數
		}
		return adminRepository.searchByKeyword(keyword, buildPageable(page, size));
	}
	
	@Transactional
	public Admin create(Admin admin)
	{
		Optional<Admin> ad = adminRepository.findByUsername(admin.getUsername());
		if(ad.isPresent())
		{
			throw new IllegalArgumentException("帳號已存在：" + admin.getUsername());
		}
		admin.setPassword(PasswordUtil.sha256(admin.getPassword()));
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
		   if (sameName.isPresent() && !sameName.get().getAdminId().equals(adminId))
		   {
		       throw new IllegalArgumentException("帳號已被使用：" + admin.getUsername());
		   }
		   
		existing.setUsername(admin.getUsername());
		existing.setName(admin.getName());
		
		if (admin.getPassword() != null && !admin.getPassword().isBlank()) 
		{
	        existing.setPassword(PasswordUtil.sha256(admin.getPassword())); // 密碼加密後再存
	    }

	    return adminRepository.save(existing);
		
	}
}
