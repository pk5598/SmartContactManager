package com.scm.SmartContactManager.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.aspectj.weaver.NewConstructorTypeMunger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.scm.SmartContactManager.dao.UserRepository;
import com.scm.SmartContactManager.entities.User;
import com.scm.SmartContactManager.helper.Message;

@Controller
public class HomeController {
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	
	@RequestMapping("/")
	public String home(Model model) {
		
		model.addAttribute("title", "Smart Contact Manager");
		return "home";
	}
	
	
	@RequestMapping("/about")
	public String about(Model model) {
		
		model.addAttribute("title", "About");
		return "about";
	}
	
	
	@RequestMapping("/signup")
	public String signup(Model model) {
		
		model.addAttribute("title", "Signup");
		model.addAttribute("user", new User());
		return "signup";
	}
	
	@PostMapping ("/do_register")
	public String name(@Valid@ModelAttribute("user") User user, 
			BindingResult result1,
			@RequestParam(value = "agreement",defaultValue = "false") Boolean agreement,
			Model model,
			HttpSession session ) {
		
		
		try { 
			if (!agreement) {
				System.out.println("you have not accepted terms and conditions");
				throw new Exception();
			}
			
			if(result1.hasErrors()) {
				model.addAttribute("user",user );
				return "signup";
			}
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			User result=this.userRepository.save(user);
			
			model.addAttribute("user", new User());
			
			System.out.println(user.getEmail()+" "+user.getName());
	        System.out.println(agreement);
	        session.setAttribute("message",new Message("Successfully Registered!!", "alert-success"));
	        return "signup";
	        
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message",new Message("something went wrong", "alert-danger"));
			return "signup";
		}
		
	}
	
	@GetMapping("/signin")
	private String customLogin(Model model) {
		model.addAttribute("title", "Login Page");
		return "login";
		
	}
	
	
	

	

}
