package com.nguyenthithuthao.example05.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.nguyenthithuthao.example05.entity.Cart;
import com.nguyenthithuthao.example05.entity.CartItem;
import com.nguyenthithuthao.example05.entity.Order;
import com.nguyenthithuthao.example05.entity.OrderItem;
import com.nguyenthithuthao.example05.entity.Payment;
import com.nguyenthithuthao.example05.entity.Product;
import com.nguyenthithuthao.example05.exceptions.APIException;
import com.nguyenthithuthao.example05.exceptions.ResourceNotFoundException;
import com.nguyenthithuthao.example05.payloads.OrderDTO;
import com.nguyenthithuthao.example05.payloads.OrderItemDTO;
import com.nguyenthithuthao.example05.payloads.OrderResponse;
import com.nguyenthithuthao.example05.repository.CartItemRepo;
import com.nguyenthithuthao.example05.repository.CartRepo;
import com.nguyenthithuthao.example05.repository.OrderItemRepo;
import com.nguyenthithuthao.example05.repository.OrderRepo;
import com.nguyenthithuthao.example05.repository.PaymentRepo;
import com.nguyenthithuthao.example05.repository.UserRepo;
import com.nguyenthithuthao.example05.service.CartService;
import com.nguyenthithuthao.example05.service.OrderService;
import com.nguyenthithuthao.example05.service.UserService;
import jakarta.transaction.Transactional;

@Transactional
@Service
public class OrderServiceImpl implements OrderService {
  @Autowired
  public UserRepo userRepo;
  @Autowired
  public CartRepo cartRepo;
  @Autowired
  public OrderRepo orderRepo;
  @Autowired
  private PaymentRepo paymentRepo;
  @Autowired
  public OrderItemRepo orderItemRepo;
  @Autowired
  public CartItemRepo cartItemRepo;
  @Autowired
  public UserService userService;
  @Autowired
  public CartService cartService;
  @Autowired
  public ModelMapper modelMapper;

  @Override
  public OrderDTO placeOrder(String emailId, Long cartId, String paymentMethod) {
      Cart cart = cartRepo.findCartByEmailAndCartId(emailId, cartId);
      if (cart == null) {
          throw new ResourceNotFoundException("Cart", "cartId", cartId);
      }
  
      if (cart.getCartItems().isEmpty()) {
          throw new APIException("Cart is empty");
      }
  
      // Tạo đối tượng Order
      Order order = new Order();
      order.setEmail(emailId);
      order.setOrderDate(LocalDate.now());
      order.setTotalAmount(cart.getTotalPrice());
      order.setOrderStatus("Order Accepted!");
  
      // Tạo đối tượng Payment
      Payment payment = new Payment();
      payment.setPaymentMethod(paymentMethod);
      payment.setOrder(order);
  
      order.setPayment(paymentRepo.save(payment));
  
      // Lưu Order và OrderItems
      Order savedOrder = orderRepo.save(order);
      List<OrderItem> orderItems = cart.getCartItems().stream().map(cartItem -> {
          OrderItem orderItem = new OrderItem();
          orderItem.setOrder(savedOrder);
          orderItem.setProduct(cartItem.getProduct());
          orderItem.setQuantity(cartItem.getQuantity());
          orderItem.setDiscount(cartItem.getDiscount());
          orderItem.setOrderedProductPrice(cartItem.getProductPrice());
  
          // Cập nhật số lượng tồn kho
          Product product = cartItem.getProduct();
          product.setQuantity(product.getQuantity() - cartItem.getQuantity());
          return orderItem;
      }).collect(Collectors.toList());
  
      orderItemRepo.saveAll(orderItems);
  
      // Xoá giỏ hàng sau khi đặt hàng
      cart.getCartItems().forEach(item -> 
          cartService.deleteProductFromCart(cartId, item.getProduct().getProductId())
      );
  
      // Mapping sang DTO
      OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);
      orderDTO.setOrderItems(orderItems.stream()
          .map(item -> modelMapper.map(item, OrderItemDTO.class))
          .collect(Collectors.toList())
      );
  
      return orderDTO;
  }
  

  @Override
  public List<OrderDTO> getOrdersByUser(String emailId) {
    List<Order> orders = orderRepo.findAllByEmail(emailId);
    List<OrderDTO> orderDTOs = orders.stream().map(order -> modelMapper.map(order, OrderDTO.class))
        .collect(Collectors.toList());
    if (orderDTOs.size() == 0) {
      throw new APIException("No orders placed yet by the user with email: " + emailId);
    }
    return orderDTOs;
  }

  @Override
  public OrderDTO getOrder(String emailld, Long orderId) {
    Order order = orderRepo.findOrderByEmailAndOrderId(emailld, orderId);
    if (order == null) {
      throw new ResourceNotFoundException("Order", "orderId", orderId);
    }
    return modelMapper.map(order, OrderDTO.class);
  }

  @Override
  public OrderResponse getAllOrders(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
    Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
        : Sort.by(sortBy).descending();
    Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
    Page<Order> pageOrders = orderRepo.findAll(pageDetails);
    List<Order> orders = pageOrders.getContent();
    List<OrderDTO> orderDTOs = orders.stream().map(order -> modelMapper.map(order, OrderDTO.class))
        .collect(Collectors.toList());
    if (orderDTOs.size() == 0) {
      throw new APIException("No orders placed yet by the users");
    }
    OrderResponse orderResponse = new OrderResponse();
    orderResponse.setContent(orderDTOs);
    orderResponse.setPageNumber(pageOrders.getNumber());
    orderResponse.setPageSize(pageOrders.getSize());
    orderResponse.setTotalElements(pageOrders.getTotalElements());
    orderResponse.setTotalPages(pageOrders.getTotalPages());
    orderResponse.setLastPage(pageOrders.isLast());
    return orderResponse;
  }

  @Override
  public OrderDTO updateOrder(String emailId, Long orderId, String orderStatus) {
    Order order = orderRepo.findOrderByEmailAndOrderId(emailId, orderId);
    if (order == null) {
      throw new ResourceNotFoundException("Order", "orderId", orderId);
    }
    order.setOrderStatus(orderStatus);
    return modelMapper.map(order, OrderDTO.class);
  }
}