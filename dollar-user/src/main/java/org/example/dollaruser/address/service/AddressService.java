package org.example.dollaruser.address.service;

import java.nio.file.AccessDeniedException;
import java.util.List;
import lombok.RequiredArgsConstructor;

import org.example.dollaruser.address.dto.AddressRequestDto;
import org.example.dollaruser.address.dto.AddressResponseDto;
import org.example.dollaruser.address.entity.Address;
import org.example.dollaruser.address.repository.AddressRepository;
import org.example.dollaruser.user.service.UserService;
import org.example.share.config.global.entity.user.User;
import org.example.share.config.global.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserService userService;

    public void createAddress(AddressRequestDto requestDto, User user) {
        User finduser = userService.findById(user.getId());
        Address address = new Address(requestDto, finduser.getId());
        addressRepository.save(address);
    }

    public List<AddressResponseDto> getUserAllAddress(User user) {
        return addressRepository.findAllByUserId(user.getId())
            .stream()
            .map(AddressResponseDto::new).toList();
    }

    public void updateAddress(Long addressId, AddressRequestDto requestDto, User user) throws AccessDeniedException {
        Address address = findOne(addressId);

        if(!address.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("해당 주소에 대한 권한이 없습니다.");
        }
        address.updateAddress(requestDto);
        addressRepository.save(address);
    }

    public void deleteAddress(Long addressId, User user) throws AccessDeniedException {
        Address address = findOne(addressId);

        if(!address.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("해당 주소에 대한 권한이 없습니다.");
        }
        addressRepository.delete(address);
    }

    public Address findOne(Long addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new NotFoundException("주소를 찾을 수 없습니다."));
    }
}
