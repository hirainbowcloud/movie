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
	
	@Transactional
	public Admin create(Admin admin)
	{
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
}
