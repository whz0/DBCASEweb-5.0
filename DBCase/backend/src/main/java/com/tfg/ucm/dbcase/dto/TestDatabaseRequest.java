package com.tfg.ucm.dbcase.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestDatabaseRequest {

    String url;
    String username;
    String password;
}
