package com.scm.SmartContactManager.dao;

import java.util.List;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.scm.SmartContactManager.entities.Contact;

public interface ContactRepo  extends JpaRepository<Contact, Integer>{
	//pagination
	
	//Pageable contains two information  curr page,contacts per page
	@Query("from Contact as c where c.user.id =:userId")
	public Page<Contact> finContactsByUser(@Param ("userId")int userId,
			 Pageable pageable);

}
