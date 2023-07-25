package org.rmm.rsql;

import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.RSQLOperators;
import lombok.Getter;

/**
 * Enum representation of filter operators for use in switch statements and for easier comparison.
 * Logic taken from <a href="https://www.baeldung.com/rest-api-search-language-rsql-fiql">Baeldung</a>.
 * Then extended with additional operators.
 *
 * @author  Rob McM
 */
@Getter
public enum RsqlFilterOperators {
    EQUAL(RSQLOperators.EQUAL),
    NOT_EQUAL(RSQLOperators.NOT_EQUAL),
    GREATER_THAN(RSQLOperators.GREATER_THAN),
    GREATER_THAN_OR_EQUAL(RSQLOperators.GREATER_THAN_OR_EQUAL),
    LESS_THAN(RSQLOperators.LESS_THAN),
    LESS_THAN_OR_EQUAL(RSQLOperators.LESS_THAN_OR_EQUAL),
    IN(RSQLOperators.IN),
    NOT_IN(RSQLOperators.NOT_IN),
    LIKE(RsqlCustomOperators.LIKE);

    private final ComparisonOperator operator;

    /**
     * Constructor
     */
    RsqlFilterOperators(ComparisonOperator operator) {
        this.operator = operator;
    }

    /**
     * Gets the enum value for the provided {@link ComparisonOperator}
     *
     * @param operator  The operator we are getting the enum for.
     * @return  The enum value.
     */
    public static RsqlFilterOperators getSimpleOperator(ComparisonOperator operator) {
        for (RsqlFilterOperators operation : values()) {
            if (operation.getOperator() == operator) {
                return operation;
            }
        }
        return null;
    }
}
