package org.jdbc_cursor.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestTableEntity {

    private long id;
    private String textField;
    private LocalDate dateField;
}
