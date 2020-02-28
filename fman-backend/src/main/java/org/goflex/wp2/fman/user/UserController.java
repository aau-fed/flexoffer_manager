package org.goflex.wp2.fman.user;

/*-
 * #%L
 * GOFLEX::WP2::FlexOfferManager Backend
 * %%
 * Copyright (C) 2017 - 2020 The GOFLEX Consortium
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.goflex.wp2.fman.billing.UserBill;
import org.goflex.wp2.fman.billing.BillingService;
import org.goflex.wp2.fman.common.exception.CustomException;
import org.goflex.wp2.fman.user.usercontract.UserContract;
import org.goflex.wp2.fman.user.usercontract.UserContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	UserService userService;

	@Autowired
    UserContactService userContactService;

	@Autowired
    BillingService billingService;


    @PostMapping(value = "/login", produces = "application/json")
    @ApiOperation(value = "${UserController.login}")
    @ApiResponses(value = {//
            @ApiResponse(code = 400, message = "Something went wrong"), //
            @ApiResponse(code = 422, message = "Invalid username/password supplied")})
    public Map login(@ApiParam("User login") @RequestBody UserT login) throws Exception {
        String token = userService.signin(login.getUserName(), login.getPassword());
        UserT user = userService.getUserByUserName(login.getUserName());
        if (user.getRole() != UserRole.ROLE_ADMIN && user.getRole() != UserRole.ROLE_BROKER) {
            throw new Exception("Only admin or broker users are currently allowed to signin");
        }
        return ImmutableMap.of("token", token,
                               "user", user );
    }


    @PostMapping(value = "/refreshToken", produces = "application/json")
    @ApiOperation(value = "${UserController.refreshToken}")
    @ApiResponses(value = {//
            @ApiResponse(code = 400, message = "Something went wrong"), //
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public Map refreshToken(@ApiParam("User Refresh Token") @RequestBody UserT userT) throws Exception {
        String token = userService.refreshToken(userT.getUsername());
        UserT user = userService.getUserByUserName(userT.getUserName());
        if (user.getRole() != UserRole.ROLE_ADMIN && user.getRole() != UserRole.ROLE_BROKER) {
            throw new Exception("Only admin or broker users are currently allowed to refresh tokens");
        }
        return ImmutableMap.of("token", token,
                               "user", user );
    }

    @PostMapping(value="/register", produces = "application/json")
    //@PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_BROKER')")      // Added, as only admins should be able to register users
    @ApiOperation(value = "${UserController.register}")
    @ApiResponses(value = {//
            @ApiResponse(code = 400, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 422, message = "Username is already in use"), //
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public Map register(@ApiParam("Register User") @RequestBody UserT user) {
        return ImmutableMap.of("token", userService.signup(user),
                               "user", userService.getUserByUserName(user.getUserName()));
    }


    @GetMapping(value = "/me")
    @ApiOperation(value = "${UserController.whoami}", response = UserT.class)
    @ApiResponses(value = {//
            @ApiResponse(code = 400, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public UserT whoami(HttpServletRequest req) {
        return userService.whoami(req);
    }



    @GetMapping(value = "/roles")
    @ApiOperation(value = "${UserController.roles}", response = UserT.class)
    @ApiResponses(value = {//
            @ApiResponse(code = 400, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public List<String> roles(Authentication auth) {
        return auth.getAuthorities().stream()
                                    .map( a -> a.getAuthority())
                                    .collect(Collectors.toList());
    }

    @DeleteMapping(value = "/{username}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "${UserController.delete}")
    @ApiResponses(value = {//
            @ApiResponse(code = 400, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "The user doesn't exist"), //
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public String delete(@ApiParam("Username") @PathVariable String username) {
        userService.delete(username);
        return username;
    }

    @PutMapping(value = "/{username}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "${UserController.save}")
    @ApiResponses(value = {//
            @ApiResponse(code = 400, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 422, message = "Username is already in use"), //
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public UserT save(@ApiParam("User being updated") @RequestBody UserT user) {
        return userService.update(user);
    }

    @GetMapping(value = "/contract/{userName}")
    @ApiOperation(value = "${UserController.contract}", response = UserContract.class)
    @ApiResponses(value = {//
            @ApiResponse(code = 400, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public Map contract(@PathVariable String userName) {
        UserT user = userService.getUserByUserName(userName);
        if(user == null) {
            throw new CustomException("User not found", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return ImmutableMap.of("status", HttpStatus.OK.value(), "message", "success",
                "data", userContactService.getContract(user.getUserId()));
    }

    @PutMapping(value = "/contract")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "${UserController.updatecontract}")
    @ApiResponses(value = {//
            @ApiResponse(code = 400, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 422, message = "Username is already in use"), //
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public UserContract updateContract(@ApiParam("User contract being updated") @RequestBody UserContract userContract) {
        return userContactService.update(userContract);
    }

    @GetMapping(value = "/getbill/{userName}/{year}/{month}")
    @ApiOperation(value = "${UserController.getBill}", response = List.class)
    @ApiResponses(value = {//
            @ApiResponse(code = 400, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public Map getBill(@PathVariable(value = "userName") String userName,
                            @PathVariable(value = "year") String year,
                            @PathVariable(value = "month") String month) throws Exception {

        int yr = Integer.parseInt(year);
        int mo = Integer.parseInt(month);
        UserBill bill = this.billingService.generateUserBillForMonth(userName, yr, mo);//cal.get(Calendar.MONTH));
        return ImmutableMap.of("status", HttpStatus.OK.value(), "message", "success",
                "data", bill);
    }



    @GetMapping(value = "/{userName}")
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_BROKER')")
    @ApiOperation(value = "${UserController.getUserByUserName}")
	public UserT getUserByUserName(@PathVariable String userName) {
        UserT user = userService.getUserByUserName(userName);
		return userService.getUserByUserName(userName);
	}

    @GetMapping(value = "/")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "${UserController.getAllUser}")
	public @ResponseBody List<UserT> getAllUser() {
		List<UserT> users = userService.getAllUsers()
                .stream().filter(u -> !u.getUserName().equals("sysadmin"))
                .collect(Collectors.toList());
		return users;
	}

    @GetMapping(value = "/userNames")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Gets all user names only")
	public @ResponseBody List<Map<String, String>> getAllUserNames() {
		List<Map<String, String>> users = new ArrayList<>();
        userService.getAllUsers()
                .stream().filter(u -> !u.getUserName().equals("sysadmin")).collect(Collectors.toList())
                .forEach(user -> users.add(ImmutableMap.of("userName", user.getUserName())));
		return users;
	}


}
