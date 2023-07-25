package org.rmm.rsql;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Test VO for use in unit testing.
 *
 * @author  Rob McMurray
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TestObjectExtendTwoVO extends TestObjectVO{

    private String field5;

}
