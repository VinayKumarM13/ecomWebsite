package com.ecom.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.UserdDtls;
import com.ecom.service.CategoryService;
import com.ecom.service.ProductService;
import com.ecom.service.UserService;
import com.ecom.util.CommonUtil;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.Session;

@Controller
public class HomeController {

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private ProductService productService;

	@Autowired
	private UserService userService;
	
	@Autowired
	private CommonUtil commonUtil;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@ModelAttribute
	public void getUserDetails(Principal p , Model m) {
		if(p!=null) {
			String email = p.getName();
			UserdDtls userdDtls = userService.getUserByEmail(email);
			m.addAttribute("user", userdDtls);
		}
		
		List<Category> allActiveCategory = categoryService.getAllActiveCategory();
		m.addAttribute("categorys", allActiveCategory);
	}

	@GetMapping("/")
	public String index() {
		return "index";
	}

	@GetMapping("/signin")
	public String login() {
		return "login";
	}

	@GetMapping("/register")
	public String register() {
		return "register";
	}

	@GetMapping("/products")
	public String products(Model m, @RequestParam(value = "category", defaultValue = "") String category) {
		System.out.println("category="+category);
		List<Category> categories = categoryService.getAllActiveCategory();
		List<Product> products = productService.getAllActiveProducts(category);
		m.addAttribute("categories", categories);
		m.addAttribute("products", products);
		m.addAttribute("paramValue",category);
		return "product";
	}

	@GetMapping("/product/{id}")
	public String product(@PathVariable int id, Model m) {
		Product productById = productService.getProductById(id);
		m.addAttribute("product", productById);
		return "view_product";
	}

	@PostMapping("/saveUser")
	private String saveUser(@ModelAttribute UserdDtls user, @RequestParam("img") MultipartFile file, HttpSession session) throws IOException
	{
		String imageName = file.isEmpty() ? "default.jsp" : file.getOriginalFilename();
		user.setProfileImage(imageName);
		UserdDtls saveUser = userService.saveUser(user);


		if(!ObjectUtils.isEmpty(saveUser)) {
			if(!file.isEmpty()) {
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+"profile_img"+File.separator+file.getOriginalFilename());

//				System.out.println(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
			session.setAttribute("succMsg",  "Register  Successfully");

		}else {
			session.setAttribute("errorMsg", "Something wrong on server");

		}
		return "redirect:/register";
	}
//		forget password code
		
		@GetMapping("/forgot-password")
		public String showForgotPassword() {
			
			return "forgot_password.html";
		}
		
		@PostMapping("/forgot-password")
		public String processForgotPassword(@RequestParam String email , HttpSession session, HttpServletRequest request) 
				throws UnsupportedEncodingException, MessagingException {
			
			UserdDtls userByEmail = userService.getUserByEmail(email);
			
			if(ObjectUtils.isEmpty(userByEmail)) {
				session.setAttribute("errorMsg", "invalid email");
			}else {
				
				String resetToken = UUID.randomUUID().toString();
				userService.updatedUserResetToken(email,resetToken);
				
				String url = CommonUtil.generateUrl(request)+"/reset-password?token="+resetToken;
				
				Boolean sendMail = commonUtil.sendMail(url, email);
				
				if(sendMail) {
					session.setAttribute("succMsg", "please check your email... Password Reset link sent");
				}else {
					session.setAttribute("errorMsg", "Something Wrong on Server !");
				}
				
			}
			return "redirect:/forgot-password";
		}
		
		@GetMapping("/reset-password")
		public String showRestPassword(@RequestParam String token,HttpSession session, Model m) {
			
			UserdDtls userByToken = userService.getUserByToken(token);
			if(userByToken==null) {
				m.addAttribute("msg", "your link is invalid or expired !!");
				return "message";
			}
			m.addAttribute("token", token);
			return "reset_password";
		}
	
		@PostMapping("/reset-password")
		public String restPassword(@RequestParam String token,@RequestParam String password,HttpSession session, Model m) {
			
			UserdDtls userByToken = userService.getUserByToken(token);
			if(userByToken==null) {
				m.addAttribute("errorMsg", "your link is invalid or expired");
				return "message";
			}else {
				userByToken.setPassword(passwordEncoder.encode(password));
				userByToken.setResetToken(null);
				userService.updateUser(userByToken);
				session.setAttribute("succMsg", "Password change Successfully");
				m.addAttribute("msg", "Password change Successfully");
				return "message";
			}
			
		}
		
}
