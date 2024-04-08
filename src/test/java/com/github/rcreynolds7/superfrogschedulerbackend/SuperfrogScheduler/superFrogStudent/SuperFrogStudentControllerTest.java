package com.github.rcreynolds7.superfrogschedulerbackend.SuperfrogScheduler.superFrogStudent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rcreynolds7.superfrogschedulerbackend.SuperfrogScheduler.superFrogStudent.dto.SuperFrogStudentDto;
import com.github.rcreynolds7.superfrogschedulerbackend.SuperfrogScheduler.system.StatusCode;
import org.aspectj.lang.annotation.After;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SuperFrogStudentControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    SuperFrogStudentService superFrogStudentService;

    @Autowired
    ObjectMapper objectMapper;

    List<SuperFrogStudent> superFrogStudents;

    @BeforeEach
    void setUp() {
        this.superFrogStudents = new ArrayList<>();

        SuperFrogStudent s1 = new SuperFrogStudent();
        s1.setId(1);
        s1.setFirstName("tom");
        s1.setLastName("lee");
        s1.setActive(true);
        s1.setPhone("123-456-7901");
        s1.setAddress("2901 Stadium Dr");
        s1.setEmail("tomlee@tcu.edu");
        this.superFrogStudents.add(s1);

        SuperFrogStudent s2 = new SuperFrogStudent();
        s2.setId(2);
        s2.setFirstName("andre");
        s2.setLastName("gomez");
        s2.setActive(false);
        s2.setPhone("263-456-7891");
        s2.setAddress("3000 McCart Ave");
        s2.setEmail("andregomez@tcu.edu");
        this.superFrogStudents.add(s2);
    }

    @AfterEach()
    void tearDown() {
    }

    @Test
    void testDeactivateSuperFrogStudentSuccess() throws Exception {
        // Given
        SuperFrogStudent existingStudent = new SuperFrogStudent();
        existingStudent.setId(1);
        existingStudent.setActive(true);

        given(superFrogStudentService.findById(eq(1))).willReturn(existingStudent);
        given(superFrogStudentService.update(eq(1), any(SuperFrogStudent.class))).willReturn(existingStudent);

        // When & Then
        mockMvc.perform(put("/api/v1/superfrog-students/{superFrogStudentId}/deactivate", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("SuperFrog Student deactivated successfully"));
    }
}
