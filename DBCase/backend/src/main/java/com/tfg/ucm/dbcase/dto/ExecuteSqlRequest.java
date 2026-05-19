package com.tfg.ucm.dbcase.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteSqlRequest {
    private DatabaseUrl databaseUrl;
    private String username;
    private String password;
    private String sql;
}
