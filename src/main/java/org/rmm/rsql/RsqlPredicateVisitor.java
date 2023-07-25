package org.rmm.rsql;

import cz.jirutka.rsql.parser.ast.AndNode;
import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.OrNode;
import cz.jirutka.rsql.parser.ast.RSQLVisitor;

import java.util.function.Predicate;

/**
 * Rsql visitor class to constrict {@link Predicate} from parsed {@link cz.jirutka.rsql.parser.ast.Node}
 *
 * @author Rob McMurray
 */
public class RsqlPredicateVisitor<T> implements RSQLVisitor<Predicate<T>, Void> {

    /**
     * Predicate builder to be used by this visitor class.
     */
    private final SimpleRsqlPredicateBuilder<T> predicateBuilder;

    /**
     * Constructor
     */
    public RsqlPredicateVisitor() {
        predicateBuilder = new SimpleRsqlPredicateBuilder<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Predicate<T> visit(AndNode andNode, Void unused) {
        return predicateBuilder.build(andNode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Predicate<T> visit(OrNode orNode, Void unused) {
        return predicateBuilder.build(orNode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Predicate<T> visit(ComparisonNode comparisonNode, Void unused) {
        return predicateBuilder.build(comparisonNode);
    }
}
