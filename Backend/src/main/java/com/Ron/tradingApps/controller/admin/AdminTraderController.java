package com.Ron.tradingApps.controller.admin;

import com.Ron.tradingApps.dto.request.InstructorRequestDTO;
import com.Ron.tradingApps.dto.response.InstructorResponseDTO;
import com.Ron.tradingApps.mapper.InstructorMapper;
import com.Ron.tradingApps.mapper.TraderMapper;
import com.Ron.tradingApps.model.Instructor;
import com.Ron.tradingApps.service.user.InstructorService;
import com.Ron.tradingApps.service.user.TraderService;
import jakarta.validation.Valid;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/traders")
public class AdminTraderController {
    @Autowired
    private TraderMapper traderMapper;

    @Autowired
    private TraderService traderService;
    @Autowired
    private InstructorService instructorService;
    @Autowired
    private InstructorMapper instructorMapper;

    @PostMapping
    public InstructorResponseDTO createTrader(@Valid @RequestBody InstructorRequestDTO requestDTO){
        Instructor instructor = instructorService.save(instructorMapper.toEntity(requestDTO));
        return instructorMapper.toDTO(instructor);
    }

    @PutMapping("/{username}")
    public InstructorResponseDTO updateInstructor(@PathVariable("username") String username,
                                              @Valid @RequestBody InstructorRequestDTO requestDTO) throws ResourceNotFoundException{
        Instructor updateInstructor = instructorService.update(username, instructorMapper.toEntity(requestDTO));
        return instructorMapper.toDTO(updateInstructor);
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteTrader(@PathVariable("username") String username) {
        try {
            traderService.deleteByUsername(username);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
