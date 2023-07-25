package org.rmm.rsql;

import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.RSQLOperators;

/**
 * Additional {@link ComparisonOperator} extension of {@link RSQLOperators}
 *
 * @author Rob McMurray
 */
public class RsqlCustomOperators extends RSQLOperators {

    public static final ComparisonOperator LIKE = new ComparisonOperator("=like=", true);

}
