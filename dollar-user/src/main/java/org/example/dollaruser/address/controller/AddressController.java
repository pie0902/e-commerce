package org.example.dollaruser.address.controller;

import java.nio.file.AccessDeniedException;
import java.util.List;
import lombok.RequiredArgsConstructor;

import org.example.dollaruser.address.dto.AddressRequestDto;
import org.example.dollaruser.address.dto.AddressResponseDto;
import org.example.dollaruser.address.service.AddressService;
import org.example.share.config.global.security.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/address")
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<String> createAddress(
            @RequestBody AddressRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        addressService.createAddress(requestDto, userDetails.getUser());

        return ResponseEntity.status(201).body("주소 생성 완료");
    }

    @GetMapping
    public ResponseEntity<List<AddressResponseDto>> getUserAllAddress(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<AddressResponseDto> addressList = addressService.getUserAllAddress(userDetails.getUser());

        return ResponseEntity.status(200).body(addressList);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<String> updateAddress(
            @PathVariable Long addressId,
            @RequestBody AddressRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) throws AccessDeniedException {
        addressService.updateAddress(addressId, requestDto, userDetails.getUser());

        return ResponseEntity.status(200).body("주소 수정 완료");
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<String> deleteAddress(
            @PathVariable Long addressId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) throws AccessDeniedException {
        addressService.deleteAddress(addressId, userDetails.getUser());

        return ResponseEntity.status(200).body("주소 삭제 완료");
    }
}
