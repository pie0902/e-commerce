package org.example.dollaruser.address.repository;


import java.util.List;
import org.example.dollaruser.address.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findAllByUserId(Long userId);
}
