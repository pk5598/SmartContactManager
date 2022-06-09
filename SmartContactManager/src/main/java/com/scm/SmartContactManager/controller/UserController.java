package com.scm.SmartContactManager.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.apache.catalina.filters.AddDefaultCharsetFilter;
import org.aspectj.weaver.NewConstructorTypeMunger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.scm.SmartContactManager.dao.ContactRepo;
import com.scm.SmartContactManager.dao.UserRepository;
import com.scm.SmartContactManager.entities.Contact;
import com.scm.SmartContactManager.entities.User;
import com.scm.SmartContactManager.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private ContactRepo contactRepo;
	
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {
		String userName=principal.getName();
		//System.out.println(userName);
		User user=this.userRepo.getUserByUserName(userName);
		//System.out.println("user"+user);
		
		model.addAttribute("user", user);
	}
	
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal) {
	
        return "normal/user_dashboard";		
		
	}
	
	@RequestMapping("/addContactForm")
	public String addContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		
		return "normal/addContactForm";
		
	}
	
	@PostMapping("/processContact")
	public String processContact(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file,
			Principal principal,
			HttpSession session) {
		
		try {
			String nameString=principal.getName();
			
			User user=this.userRepo.getUserByUserName(nameString);
			
			//processing and uplloading file
			if (file.isEmpty()) {
				contact.setImage("defaultImg.png");
				//throw new Exception();
								
			}
			else {
				
				//upload the file to folder and update the contact
				contact.setImage(file.getOriginalFilename());
				File savefile=new ClassPathResource("static/img").getFile();
				Path path=Paths.get(savefile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			    
			}
			
			contact.setUser(user); 
			user.getContacts().add(contact);
			this.userRepo.save(user); 
			
			//success msg
			session.setAttribute("message",new Message("Successfully Added Contact!!", "alert-success") );
			
		} catch (Exception e) {
			System.out.println("Error "+e.getMessage());
			e.printStackTrace();
			
			//error msg
			session.setAttribute("message",new Message("Something went wrong!!", "alert-danger") );
			
		}
		
		
		return "normal/addContactForm";
		
	}
	
	
	//show contacts hander
	//per page=5[n], n=no of contacts per page
	//current page=0[current page]
	@GetMapping("/showContacts/{page}")
	public String showContacts( @PathVariable("page") Integer page,
			Model model,
			Principal principal) {
		
		String userName=principal.getName();
		User user=userRepo.getUserByUserName(userName);
		Pageable pageable=PageRequest.of(page, 3);
		
		int userId=user.getId();
		Page<Contact> contacts=this.contactRepo.finContactsByUser(userId,pageable);
		
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages",contacts.getTotalPages());
		
		return "normal/showContacts";
		
	}
	
	@GetMapping("/contact/{cid}")
	public String showIndividualContact(@PathVariable("cid") Integer cid,
			Model model,Principal principal){
		Contact contact=this.contactRepo.findById(cid).get();
		String name=principal.getName();
		User user=this.userRepo.getUserByUserName(name);
		
		if (user.getId()==contact.getUser().getId()) {
			model.addAttribute("contact",contact);
		} 
		return "normal/showIndividualContact";
	}
	
	@GetMapping("/contact/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid,
			Model model,
			Principal principal,
			HttpSession session) {
	
		Contact contact=this.contactRepo.findById(cid).get();
		
		contact.setUser(null);

		contactRepo.delete(contact);
		
		
		session.setAttribute("message",new Message("Contact deleted successfully", "alert-success"));
		
		
		return "redirect:/user/showContacts/0";
	}
	
	// open update form handler
	@PostMapping("/updateContactForm/{cid}")
	public String openUpdateForm(@PathVariable("cid") Integer cid,Model model) {
		model.addAttribute("title", "Update form");
		Contact contact=contactRepo.findById(cid).get();
		model.addAttribute("contact", contact);
		return "normal/updateContactForm";
	}
	
	//process update form
	@PostMapping("/processUpdateContact")
	public String processUpdateForm(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file,
			Model model,HttpSession session,Principal principal) {
		
		try {
		
			User user=this.userRepo.getUserByUserName(principal.getName());
			contact.setUser(user);
			
			//processing and uplloading file
			if (file.isEmpty()) {
				contact.setImage("defaultImg.png");
				//throw new Exception();
								
			}
			else {
				
				//upload the file to folder and update the contact
				contact.setImage(file.getOriginalFilename());
				File savefile=new ClassPathResource("static/img").getFile();
				Path path=Paths.get(savefile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			    
			}
			
			this.contactRepo.save(contact);
			session.setAttribute("message",new Message("Contact updated successfully", "alert-success"));
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println(contact.getName());
		return "redirect:/user/contact/"+contact.getCid();
		
	}
	
	
	//user profile handler
	@GetMapping("/profilepage")
	public String yourProfile(Model model) {
		
		model.addAttribute("title", "Profile Page");
		
		return "normal/profilepage";
		
		
	}
	
	
	
	
	

}
