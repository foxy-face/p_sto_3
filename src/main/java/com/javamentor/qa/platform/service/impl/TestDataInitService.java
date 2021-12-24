package com.javamentor.qa.platform.service.impl;

import com.javamentor.qa.platform.models.entity.user.Role;
import com.javamentor.qa.platform.models.entity.user.User;
import com.javamentor.qa.platform.service.abstracts.model.RoleService;
import com.javamentor.qa.platform.service.abstracts.model.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TestDataInitService {

    private RoleService roleService;

    private UserService userService;


    public TestDataInitService() {
    }

    @Autowired
    public TestDataInitService(@Lazy RoleService roleService, @Lazy UserService userService) {
        this.roleService = roleService;
        this.userService = userService;
    }

    public void createRole() {
        Role role = new Role("ROLE_USER");
        roleService.persist(role);
    }

    public void createUser(int count) {
        for (int i = 0; i <= count; i++) {
            User user = new User();
            user.setEmail((i + "@mail.com"));
            user.setPassword(("pass" + i));
            user.setFullName(("fullName" + i));
            user.setPersistDateTime(LocalDateTime.of(2021, 12, 3, 0, 0));
            user.setIsEnabled(true);
            user.setIsDeleted(false);
            user.setCity(("City" + i));
            user.setLinkSite(("linkSite" + i));
            user.setLinkGitHub(("linkGitHub" + i));
            user.setLinkVk(("linkVk" + i));
            user.setAbout(("About" + i));
            user.setImageLink(("imageLink" + i));
            user.setLastUpdateDateTime(LocalDateTime.of(2021, 12, 4, 0, 0));
            user.setNickname(("nickName" + i));
            user.setRole(roleService.getAll().get(0));
            userService.persist(user);
        }
    }

    public void init() {
        createRole();
        createUser(50);
    }


}
