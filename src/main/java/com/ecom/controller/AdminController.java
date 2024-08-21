package com.ecom.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.UserdDtls;
import com.ecom.service.CategoryService;
import com.ecom.service.ProductService;
import com.ecom.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private ProductService productService;
	
	@Autowired
	private UserService userService;
	
	@ModelAttribute
	public void getUserDetails(Principal p , Model m) {
		if(p!=null) {
			String email = p.getName();
			UserdDtls userdDtls = userService.getUserByEmail(email);
			m.addAttribute("user", userdDtls);
			
			List<Category> allActiveCategory = categoryService.getAllActiveCategory();
			m.addAttribute("categorys", allActiveCategory);
		}
	}

	@GetMapping("/")
	public String index() {

		return "admin/index";
	}

	@GetMapping("/loadAddProduct")
	public String loadAddProduct(Model m) {
		List<Category> categories = categoryService.getAllCategory();
		m.addAttribute("categories", categories);
		return "admin/add_product";
	}

	@GetMapping("/category")
	public String category(Model m)
	{
		m.addAttribute("categorys", categoryService.getAllCategory());
		return "admin/category";
	}

	@PostMapping("/saveCategory")
	public String saveCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file, 
			HttpSession session) throws IOException
	{

		String imageName = file != null ? file.getOriginalFilename(): "default.jpg";
		category.setImageName(imageName);

		Boolean existCategory = categoryService.existCategory(category.getName());

		if(existCategory) {
			session.setAttribute("errorMsg", "Category Name already exists");
		}else {
			Category saveCategory = categoryService.saveCategory(category);

			if(ObjectUtils.isEmpty(saveCategory)) {
				session.setAttribute("errorMsg", "Not saved ! internal server error");
			}else {

				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+"category_img"+File.separator+file.getOriginalFilename());

				//System.out.println(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				session.setAttribute("succMsg",  "Saved  Successfully");
			}
		}


		return "redirect:/admin/category";
	}

	@GetMapping("deleteCategory/{id}")
	public String deleteCategory(@PathVariable int id, HttpSession session) {

		Boolean deleteCategory = categoryService.deleteCategory(id);

		if(deleteCategory) {
			session.setAttribute("succMsg", "Category delete Message");
		}else {
			session.setAttribute("errorMsg", "Something wrong on server");
		}
		return "redirect:/admin/category";

	}

	@GetMapping("/loadEditcategory/{id}")
	public String loadEditcategory(@PathVariable int id, Model m) {

		m.addAttribute("category", categoryService.getCategoryById(id));

		return "admin/edit_Category";

	}

	@PostMapping("/updateCategory")
	public String updateCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
			HttpSession session) throws IOException {

		Category oldcategory = categoryService.getCategoryById(category.getId());
		String imageName = file.isEmpty() ? oldcategory.getImageName() : file.getOriginalFilename();


		if(!ObjectUtils.isEmpty(category)) {

			oldcategory.setName(category.getName());
			oldcategory.setIsActive(category.getIsActive());
			oldcategory.setImageName(imageName);	
		}

		Category updateCategory = categoryService.saveCategory(oldcategory);

		if(!ObjectUtils.isEmpty(updateCategory)) 
		{


			if(!file.isEmpty()) {

				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+"category_img"+File.separator
						+file.getOriginalFilename());

				//System.out.println(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);


			}
			session.setAttribute("succMsg", "Category Update Successfully");
		}else {
			session.setAttribute("errorMsg", "Something wrong on server");

		}

		return "redirect:/admin/loadEditcategory/"+ category.getId(); 

	}

	@PostMapping("/saveProduct")
	public String saveProduct(@ModelAttribute Product product,@RequestParam("file") MultipartFile image,
			HttpSession session ) throws IOException {

		String imageName=image.isEmpty() ? "default.jsp" : image.getOriginalFilename();

		product.setImage(imageName);		
		product.setDiscount(0);
		product.setDiscountPrice(product.getPrice());
		Product saveProduct = productService.saveProduct(product);

		if(!ObjectUtils.isEmpty(saveProduct)) {

			File saveFile = new ClassPathResource("static/img").getFile();

			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+"product_img"+File.separator
					+image.getOriginalFilename());

			//			System.out.println(path);
			Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);



			session.setAttribute("succMsg", "Product Saved Success");
		}else {
			session.setAttribute("errorMsg", "Something Wrong On Server");
		}

		return "redirect:/admin/loadAddProduct";
	}

	@GetMapping("/products")
	public String loadViewProduct(Model m) {
		m.addAttribute("products", productService.getAllProducts());
		return "admin/products";
	}

	@GetMapping("/deleteProduct/{id}")
	public String deleteProduct(@PathVariable int id, HttpSession session) {
		Boolean deleteProduct = productService.deleteProduct(id);
		if(deleteProduct) {
			session.setAttribute("succMsg", "Product deleted Succesfully");
		}else {
			session.setAttribute("errorMsg", "Something Wrong on Server");
		}
		return "redirect:/admin/products";
	}

	@GetMapping("/editProduct/{id}")
	public String editProduct(@PathVariable int id, Model  m) {
		m.addAttribute("product", productService.getProductById(id));
		m.addAttribute("category", categoryService.getAllCategory());
		return "admin/edit_product";
	}

	@PostMapping("/updateProduct")
	public String updateProduct(@ModelAttribute Product product , @RequestParam("file") MultipartFile image,
			HttpSession session , Model m) {

		if(product.getDiscount()<0 || product.getDiscount()>100)
		{
			session.setAttribute("errorMsg", "Invalid Discount");

		}else {
			Product updateProduct = productService.updateProduct(product, image);
			if(!ObjectUtils.isEmpty(updateProduct))
			{
				session.setAttribute("succMsg", "Product Update Succesfully");
			}else {
				session.setAttribute("errorMsg", "Something Wrong on Server");

			}
		}

		return "redirect:/admin/editProduct/" + product.getId();
	}
	
	@GetMapping("/users")
	public String getAllUsers(Model m) {
		List<UserdDtls> users = userService.getUsers("ROLE_USER");
		m.addAttribute("users", users);
		return "/admin/users";
	}
	
	@GetMapping("/updateSts")
	public String updateUserAccountStatus(@RequestParam Boolean status, @RequestParam Integer id, HttpSession session) {
		
		Boolean f = userService.updateAccountStatus(id, status);
		if(f) {
			session.setAttribute("succMsg", "Account Status Updated");
		}else {
			session.setAttribute("errorMsg", "Something Wrong on Server");
		}
		return "redirect:/admin/users";
	}
}
