package org.example.tentrilliondollars.address.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.tentrilliondollars.address.entity.Address;

@Getter
@AllArgsConstructor
public class AddressResponseDto {
    private Long id;
    private String city;
    private String village;
    private String province;

    public AddressResponseDto(Address address) {
        this.id = address.getId();
        this.city = address.getCity();
        this.village = address.getVillage();
        this.province = address.getProvince();
    }
}
