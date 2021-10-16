package web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import web.model.Role;
import web.model.User;
import web.service.RoleService;
import web.service.UserService;
import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
public class UserController {
	private final UserService userService;
	private final RoleService roleService;
	private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserController(UserService userService, RoleService roleService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userService = userService;
        this.roleService = roleService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @GetMapping("/")
	public String startPage(){
		return "redirect:/login";
	}

	@GetMapping(value = "/user")
	public String userInfo(@AuthenticationPrincipal User user, Model model){
		model.addAttribute("user", user);
		model.addAttribute("roles", user.getRoles());
		return "userpage";
	}

	@GetMapping(value = "/admin")
	public String listUsers(Model model) {
		model.addAttribute("allUsers", userService.allUsers());
		return "adminpage";
	}

	@GetMapping(value = "/admin/new")
	public String newUser(Model model) {
		model.addAttribute("user", new User());
		model.addAttribute("roles", roleService.allRoles());
		return "new";
	}

	@PostMapping(value = "/admin/add-user")
	public String addUser(@ModelAttribute User user, @RequestParam(value = "checkBoxRoles") String[] checkBoxRoles) {
		Set<Role> roleSet = new HashSet<>();
		for (String role : checkBoxRoles) {
			roleSet.add(roleService.getRoleByName(role));
		}
		user.setRoles(roleSet);
		userService.saveUser(user);

		return "redirect:/admin";
	}


	@GetMapping(value = "/edit/{id}")
	public String editUserForm(@PathVariable("id") long id, Model model) {
		model.addAttribute("user", userService.getUser(id));
		model.addAttribute("roles", roleService.allRoles());
		return "edit";
	}

	@PostMapping(value = "/edit")
	public String editUser(@ModelAttribute User user, @RequestParam(value = "checkBoxRoles") String[] checkBoxRoles) {
		Set<Role> roleSet = new HashSet<>();
		for (String roles : checkBoxRoles) {
			roleSet.add(roleService.getRoleByName(roles));
		}
		user.setRoles(roleSet);
		userService.updateUser(user);
		return "redirect:/admin";
	}

	@GetMapping(value = "/remove/{id}")
	public String removeUser(@PathVariable("id") long id) {
		userService.deleteUser(id);
		return "redirect:/admin";
	}



	@PostConstruct
	public void addSomeUsers() {
		User user1 = new User();
		User user2 = new User();
		userService.saveUser(user1);
		userService.saveUser(user2);
		Role role1 = new Role();
		Role role2 = new Role();
		roleService.saveRole(role1);
		roleService.saveRole(role2);
		role1.setRole("ROLE_ADMIN");
		role2.setRole("ROLE_USER");
		roleService.updateRole(role1);
		roleService.updateRole(role2);
		Set<Role> roles = new HashSet<>();
		roles.add(role1);
		roles.add(role2);
		user1.setPassword("123");
		user1.setPassword(bCryptPasswordEncoder.encode(user1.getPassword()));
		user1.setName("Alexander");
		user1.setRoles(roles);
		userService.updateUser(user1);
		user2.setPassword("1234");
		user2.setPassword(bCryptPasswordEncoder.encode(user2.getPassword()));
		user2.setName("Al");
		user2.setRoles(roles.stream().skip(1).collect(Collectors.toSet()));
		userService.updateUser(user2);
	}

}