package com.nguyenthithuthao.example05.service;

import java.util.List;

import com.nguyenthithuthao.example05.entity.Address;
import com.nguyenthithuthao.example05.payloads.AddressDTO;

public interface AddressService {
  AddressDTO createAddress(AddressDTO addressDTO);

  List<AddressDTO> getAddresses();

  AddressDTO getAddress(Long addressId);

  AddressDTO updateAddress(Long addressId, Address address);

  String deleteAddress(Long addressId);
}