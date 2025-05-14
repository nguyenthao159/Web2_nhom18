package com.nguyenthithuthao.example05.service;

import java.util.List;
import com.nguyenthithuthao.example05.payloads.OrderDTO;
import com.nguyenthithuthao.example05.payloads.OrderResponse;

public interface OrderService {
  OrderDTO placeOrder(String emailid, Long cartId, String paymentMethod);

  OrderDTO getOrder(String emailid, Long orderId);

  List<OrderDTO> getOrdersByUser(String emailId);

  OrderResponse getAllOrders(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

  OrderDTO updateOrder(String emailld, Long orderId, String orderStatus);
}