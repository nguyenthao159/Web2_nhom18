package com.nguyenthithuthao.example05.service.impl;
import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nguyenthithuthao.example05.config.AppConstants;
import com.nguyenthithuthao.example05.entity.Address;
import com.nguyenthithuthao.example05.entity.Cart;
import com.nguyenthithuthao.example05.entity.Role;
import com.nguyenthithuthao.example05.entity.User;
import com.nguyenthithuthao.example05.exceptions.APIException;
import com.nguyenthithuthao.example05.exceptions.ResourceNotFoundException;
import com.nguyenthithuthao.example05.payloads.AddressDTO;
import com.nguyenthithuthao.example05.payloads.CartDTO;
import com.nguyenthithuthao.example05.payloads.ProductDTO;
import com.nguyenthithuthao.example05.payloads.UserDTO;
import com.nguyenthithuthao.example05.payloads.UserResponse;
import com.nguyenthithuthao.example05.repository.AddressRepo;
import com.nguyenthithuthao.example05.repository.RoleRepo;
import com.nguyenthithuthao.example05.repository.UserRepo;
import com.nguyenthithuthao.example05.service.UserService;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private RoleRepo roleRepo;
    @Autowired
    private AddressRepo addressRepo;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ModelMapper modelMapper;
  
    @Override
    public UserDTO registerUser(UserDTO userDTO) {
      try {
        User user = modelMapper.map(userDTO, User.class);
        Cart cart = new Cart();
        cart.setUser(user);
        user.setCart(cart);
        Role role = roleRepo.findById(AppConstants.USER_ID).get();
        user.getRoles().add(role);
  
        String country = userDTO.getAddress().getCountry();
        String state = userDTO.getAddress().getState();
        String city = userDTO.getAddress().getCity();
        String pincode = userDTO.getAddress().getPincode();
        String street = userDTO.getAddress().getStreet();
        String buildingName = userDTO.getAddress().getBuildingName();
        Address address = addressRepo.findByCountryAndStateAndCityAndPincodeAndStreetAndBuildingName(country, state, city,
            pincode, street, buildingName);
        if (address == null) {
          address = new Address(country, state, city, pincode, street, buildingName);
          address = addressRepo.save(address);
        }
        user.setAddresses(List.of(address));
        User registeredUser = userRepo.save(user);
        cart.setUser(registeredUser);
        userDTO = modelMapper.map(registeredUser, UserDTO.class);
        userDTO.setAddress(modelMapper.map(user.getAddresses().stream().findFirst().get(), AddressDTO.class));
        return userDTO;
      } catch (DataIntegrityViolationException e) {
        throw new APIException("User already exists with emailId: " + userDTO.getEmail());
      }
    }
  
    @Override
    public UserResponse getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
      Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
          : Sort.by(sortBy).descending();
      Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
      Page<User> pageUsers = userRepo.findAll(pageDetails);
      List<User> users = pageUsers.getContent();
      if (users.size() == 0) {
        throw new APIException("No User exists !!!");
      }
      List<UserDTO> userDTOS = users.stream().map(user -> {
        UserDTO dto = modelMapper.map(user, UserDTO.class);
        if (user.getAddresses().size() != 0) {
          dto.setAddress(modelMapper.map(user.getAddresses().stream().findFirst().get(), AddressDTO.class));
        }
        return dto;
      }).collect(Collectors.toList());
      UserResponse userResponse = new UserResponse();
      userResponse.setContent(userDTOS);
      userResponse.setPageNumber(pageUsers.getNumber());
      userResponse.setPageSize(pageUsers.getSize());
      userResponse.setTotalElements(pageUsers.getTotalElements());
      userResponse.setTotalPages(pageUsers.getTotalPages());
      userResponse.setLastPage(pageUsers.isLast());
      return userResponse;
    }
  
    @Override
    public UserDTO getUserById(Long userId) {
      User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
      UserDTO userDTO = modelMapper.map(user, UserDTO.class);
      userDTO.setAddress(modelMapper.map(user.getAddresses().stream().findFirst().get(), AddressDTO.class));
      return userDTO;
    }
  
    @Override
    public UserDTO updateUser(Long userId, UserDTO userDTO) {
      User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
      String encodedPass = passwordEncoder.encode(userDTO.getPassword());
      user.setFirstName(userDTO.getFirstName());
      user.setLastName(userDTO.getLastName());
      user.setMobileNumber(userDTO.getMobileNumber());
      user.setEmail(userDTO.getEmail());
      user.setPassword(encodedPass);
      if (userDTO.getAddress() != null) {
        String country = userDTO.getAddress().getCountry();
        String state = userDTO.getAddress().getState();
        String city = userDTO.getAddress().getCity();
        String pincode = userDTO.getAddress().getPincode();
        String street = userDTO.getAddress().getStreet();
        String buildingName = userDTO.getAddress().getBuildingName();
        Address address = addressRepo.findByCountryAndStateAndCityAndPincodeAndStreetAndBuildingName(country, state, city,
            pincode, street, buildingName);
        if (address == null) {
          address = new Address(country, state, city, pincode, street, buildingName);
          address = addressRepo.save(address);
          user.setAddresses(List.of(address));
        }
      }
      userDTO = modelMapper.map(user, UserDTO.class);
      userDTO.setAddress(modelMapper.map(user.getAddresses().stream().findFirst().get(), AddressDTO.class));
      // CartDTO cart modelMapper.map(user.getCart(), CartDTO.class);
      // List<ProductDTO> products user.getCart().getCartItems().stream()
      // .map(item -> modelMapper.map(item.getProduct(),
      // ProductDTO.class)).collect(Collectors.toList());
      // userDTO.setCart(cart);
      // userDTO.getCart().setProducts (products);
      return userDTO;
    }
  
    @Override
    public String deleteUser(Long userId) {
      User user = userRepo.findById(userId)
          .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
      // List<CartItem> cartItems user.getCart().getCartItems();
      // Long cartId = user.getCart().getCartId();
      // cartItems.forEach(item -> {
      // Long productId = item.getProduct().getProductId();
      // cartService.deleteProductFromCart (cartId, productId);
      // });
      userRepo.delete(user);
      return "User with userId + userId" + "deletedsuccessfully!!!";
    }
  
    @Override
    public UserDTO getUserByEmail(String email) {
      User user = userRepo.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
      UserDTO userDTO = modelMapper.map(user, UserDTO.class);
      if (user.getAddresses() != null && !user.getAddresses().isEmpty()) {
        userDTO.setAddress(modelMapper.map(user.getAddresses().stream().findFirst().get(), AddressDTO.class));
      }
      if (user.getCart() != null) {
        CartDTO cart = modelMapper.map(user.getCart(), CartDTO.class);
        List<ProductDTO> products = user.getCart().getCartItems().stream()
            .map(item -> modelMapper.map(item.getProduct(), ProductDTO.class)).collect(Collectors.toList());
        cart.setProducts(products);
        userDTO.setCart(cart);
      }
      return userDTO;
    }
  }