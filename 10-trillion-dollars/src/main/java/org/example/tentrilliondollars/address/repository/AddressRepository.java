package org.example.tentrilliondollars.address.repository;

import org.example.tentrilliondollars.address.entity.Address;
import org.example.tentrilliondollars.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findAllByUserId(Long userId);
}
