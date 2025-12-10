package org.example.tentrilliondollars.address.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.tentrilliondollars.address.dto.AddressRequestDto;
import org.example.tentrilliondollars.user.entity.User;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //시,도
    @Column(nullable = false)
    private String city;

    //구
    @Column(nullable = false)
    private String village;

    //동
    @Column(nullable = false)
    private String province;

    @Column
    private Long userId;

    public Address(AddressRequestDto requestDto, Long userId) {
        this.city = requestDto.getCity();
        this.village = requestDto.getVillage();
        this.province = requestDto.getProvince();
        this.userId = userId;
    }

    public void updateAddress(AddressRequestDto requestDto) {
        this.city = requestDto.getCity();
        this.village = requestDto.getVillage();
        this.province = requestDto.getProvince();
    }
}
