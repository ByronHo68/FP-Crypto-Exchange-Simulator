package com.Ron.tradingApps.service;

import com.Ron.tradingApps.dto.request.SignUpRequestDTO;
import com.Ron.tradingApps.model.Instructor;
import com.Ron.tradingApps.model.Trader;
import com.Ron.tradingApps.repository.InstructorRepository;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InstructorService {
    @Autowired
    private InstructorRepository instructorRepository;
    public Instructor findByUsername(String username) throws ResourceNotFoundException{
        return instructorRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found by id " + username
                ));
    }
    @Transactional
    public Instructor save(Instructor instructor){
        return instructorRepository.save(instructor);
    }
    @Transactional
    public Instructor createInstructor(String uid, SignUpRequestDTO requestDTO) {
        if (requestDTO.getDisplayName() == null || requestDTO.getEmail() == null || requestDTO.getPassword() == null) {
            throw new IllegalArgumentException("Username, email, and password must not be null");
        }
        Instructor instructor = Instructor.builder()
                .username(requestDTO.getDisplayName())
                .email(requestDTO.getEmail())
                .password(requestDTO.getPassword())
                .userId(uid)
                .firstName("unknown admin")
                .lastName("unknown admin")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .instructorNumber("unknown admin")
                .build();

        return instructorRepository.save(instructor);
    }
    @Transactional
    public Instructor update(String username, Instructor instructor) throws ResourceNotFoundException {
        Instructor existingInstructor = findByUsername(username);
        existingInstructor.setEmail(instructor.getEmail());
        existingInstructor.setPassword(instructor.getPassword());
        existingInstructor.setFirstName(instructor.getFirstName());
        existingInstructor.setLastName(instructor.getLastName());
        existingInstructor.setCreatedAt(LocalDateTime.now());
        existingInstructor.setUpdatedAt(LocalDateTime.now());
        existingInstructor.setUserId(instructor.getUserId());
        existingInstructor.setInstructorNumber(instructor.getInstructorNumber());
        return instructorRepository.save(existingInstructor);
    }

}
