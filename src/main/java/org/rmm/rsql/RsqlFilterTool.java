package org.rmm.rsql;


import com.google.common.annotations.Beta;
import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import cz.jirutka.rsql.parser.ast.Node;
import cz.jirutka.rsql.parser.ast.RSQLOperators;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Tool for filtering a list of objects based on an RSQL query string.
 *
 * @author Rob McMurray
 */
@Slf4j
@Beta // Proof of concept partial implementation.
public class RsqlFilterTool {

    /**
     * The RSQL parser for interpreting the query strings.
     */
    private final RSQLParser parser;

    /**
     * Constructor
     */
    public RsqlFilterTool() {
        Set<ComparisonOperator> supportedOperators = RSQLOperators.defaultOperators();
        supportedOperators.add(RsqlCustomOperators.LIKE);
        parser = new RSQLParser(supportedOperators);
    }

    /**
     * Filters the provided collection against the provided RSQL filter string.
     *
     * @param allValues The collection being filtered.
     * @param rsqlFilter    The RSQL filter string.
     * @return  A filtered list of elements that met the filters requirements.
     * @param <T>   The type of objects stored in the collection and returned by the filter.
     */
    public <T> List<T> filter(Collection<T> allValues, String rsqlFilter) {
        Node parsedNode = parseRsql(rsqlFilter); //Possibly try-catch this and just log.warn and return full list on error?

        Predicate<T> filterPredicate = interpretToPredicate(parsedNode);

        return allValues.stream()
                .filter(filterPredicate)
                .toList();
    }

    /**
     * Parses the RSQL string into a {@link Node}
     *
     * @param rsqlFilter    The RSQL filter string.
     * @return  The parsed {@link Node}
     */
    private Node parseRsql(String rsqlFilter) {
        Node node = parser.parse(rsqlFilter);
        log.info("Node: {}", node.toString());
        return node;
    }

    /**
     * Interpretes the provided {@link Node} to a {@link Predicate}, for use in {@code steam().filter()}
     *
     * @param parsedNode    The {@link Node} to be interpreted.
     * @return  The interpreted {@link Predicate}
     * @param <T>   The type of object the predicate will be used to filter.
     */
    private <T> Predicate<T> interpretToPredicate(Node parsedNode) {
        RsqlPredicateVisitor<T> predicateVisitor = new RsqlPredicateVisitor<>();
        return parsedNode.accept(predicateVisitor);
    }

}
