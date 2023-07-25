package org.rmm.rsql;

import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.LogicalNode;
import cz.jirutka.rsql.parser.ast.LogicalOperator;
import cz.jirutka.rsql.parser.ast.Node;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.rmm.rsql.exceotion.RsqlException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Predicate;

/**
 * Builder for {@link Predicate} from a rsql-parser {@link Node}.
 * Logic adapted from <a href="https://www.baeldung.com/rest-api-search-language-rsql-fiql">Baeldung</a>.
 *
 * @author Rob McMurray
 */
@Slf4j
public class SimpleRsqlPredicateBuilder<T> {

    /**
     * Constructs a predicate of type T for the given parsed rsql {@link Node}
     *
     * @param rsqlParsedNode    A parsed rsql {@link Node} we will construct the predicate from.
     * @return  A {@link Predicate} encapsulating the rsql logic from the provided {@link Node}
     */
    public Predicate<T> build(@NonNull Node rsqlParsedNode) {
        if (rsqlParsedNode instanceof ComparisonNode cNode) {
            return build(cNode);
        }
        if (rsqlParsedNode instanceof LogicalNode lNode) {
            return build(lNode);
        }
        log.warn("Unable to construct predicate from node [{}]. Returning null.", rsqlParsedNode);
        return null;
    }

    /**
     * Constructs a predicate from a {@link LogicalNode}.
     * This is a node containing one or more {@link Node} children, all of which will be seperated by either an AND operator or an OR operator.
     *
     * @param logicalNode   The {@link LogicalNode} we are constructing the {@link Predicate} from.
     * @return  A {@link Predicate} encapsulating the logic from the provided {@link LogicalNode}
     */
    private Predicate<T> build(@NonNull LogicalNode logicalNode) {

        List<Predicate<T>> predicateList = logicalNode
                .getChildren()
                .stream()
                .map(this::build)
                .toList();

        if (logicalNode.getOperator() == LogicalOperator.AND) {
            return andList(predicateList);
        } else if (logicalNode.getOperator() == LogicalOperator.OR) {
            return orList(predicateList);
        }
        log.warn("Logical operator [{}] is invalid or not provided. Returning null for predicate.", logicalNode.getOperator());
        return null;
    }

    /**
     * Constructs a predicate from a {@link ComparisonNode}.
     * This is a node containing a comparison of a field in the comparing object to an expected value.
     *
     * @param comparisonNode    The {@link ComparisonNode} we are constructing the {@link Predicate} from.
     * @return  A {@link Predicate} encapsulating the comparison logic present in the provided {@link ComparisonNode}
     */
    private Predicate<T> build(@NonNull ComparisonNode comparisonNode) {
        String fieldName = comparisonNode.getSelector();
        List<String> args = comparisonNode.getArguments();

        RsqlFilterOperators operator = RsqlFilterOperators.getSimpleOperator(comparisonNode.getOperator());

        if (operator != null) {
            return switch (operator) {
                case EQUAL -> equalsPredicate(fieldName, args);
                case NOT_EQUAL -> notEqualsPredicate(fieldName, args);
                case IN -> inPredicate(fieldName, args);
                case NOT_IN -> notInPredicate(fieldName, args);
                case GREATER_THAN -> throw new UnsupportedOperationException("Greater than logic not yet implemented");
                case GREATER_THAN_OR_EQUAL -> throw new UnsupportedOperationException("Greater than or equal logic not yet implemented");
                case LESS_THAN -> throw new UnsupportedOperationException("Less than logic not yet implemented");
                case LESS_THAN_OR_EQUAL -> throw new UnsupportedOperationException("Less than or equal logic not yet implemented");
                case LIKE -> throw new UnsupportedOperationException("Like logic not yet implemented");
            };
        }
        return null;

    }

    /*
     *   Predicate methods
     */

    /**
     * Creates an equality predicate for the given field name equaling the first element of the provided comparison node arguments.
     *
     * @param fieldName The field we are comparing.
     * @param nodeArguments The {@link ComparisonNode} arguments.
     * @return  A predicate on the field equality.
     */
    private Predicate<T> equalsPredicate(String fieldName, List<String> nodeArguments) {
        return e -> {
            Object fieldValue = getFieldValue(e, fieldName);
            if (fieldValue == null) {
                return nodeArguments == null || nodeArguments.isEmpty();
            }
            else if (nodeArguments == null || nodeArguments.isEmpty()) {
                return false;
            }

            List<Object> args = castNodeStringArgsToFieldType((Class<T>) e.getClass(), fieldName, nodeArguments);

            return fieldValue.equals(args.get(0));
        };
    }

    /**
     * Creates an in predicate for the given field name equaling the first element of the provided comparison node arguments.
     *
     * @param fieldName The field we are comparing.
     * @param nodeArguments The {@link ComparisonNode} arguments.
     * @return  A predicate on the field being in a set of values..
     */
    private Predicate<T> inPredicate(String fieldName, List<String> nodeArguments) {
        return e -> {
            Object fieldValue = getFieldValue(e, fieldName);
            if (fieldValue == null || nodeArguments == null || nodeArguments.isEmpty()) {
                return false;
            }

            List<Object> args = castNodeStringArgsToFieldType((Class<T>) e.getClass(), fieldName, nodeArguments);

            for(Object o : args) {
                if (fieldValue.equals(o))
                    return true;
            }
            return false;
        };
    }

    /**
     * Creates an inequality predicate for the given field name not equaling the first element of the provided comparison node arguments.
     *
     * @param fieldName The field we are comparing.
     * @param nodeArguments The {@link ComparisonNode} arguments.
     * @return  A predicate on the field inequality.
     */
    private Predicate<T> notEqualsPredicate(String fieldName, List<String> nodeArguments) {
        return e -> {
            Object fieldValue = getFieldValue(e, fieldName);
            if (fieldValue == null) {
                return !(nodeArguments == null || nodeArguments.isEmpty());
            }
            else if (nodeArguments == null || nodeArguments.isEmpty()) {
                return true;
            }

            List<Object> args = castNodeStringArgsToFieldType((Class<T>) e.getClass(), fieldName, nodeArguments);

            return !fieldValue.equals(args.get(0));
        };
    }

    /**
     * Creates a not in predicate for the given field name equaling the first element of the provided comparison node arguments.
     *
     * @param fieldName The field we are comparing.
     * @param nodeArguments The {@link ComparisonNode} arguments.
     * @return  A predicate on the field not being in a set of values..
     */
    private Predicate<T> notInPredicate(String fieldName, List<String> nodeArguments) {
        return e -> {
            Object fieldValue = getFieldValue(e, fieldName);
            if (fieldValue == null || nodeArguments == null || nodeArguments.isEmpty()) {
                return true; //If the field value is null it is by default "not in a provided set". If the arguments are null or empty a value cannot be in them.
            }

            List<Object> args = castNodeStringArgsToFieldType((Class<T>) e.getClass(), fieldName, nodeArguments);

            for(Object o : args) {
                if (fieldValue.equals(o))
                    return false;
            }
            return true;
        };
    }

    /*
     *   Helper methods
     */

    /**
     * Casts the {@link ComparisonNode} arguments provided into the type of the field name we wish to check against.
     * The cast will only occur if the field value is {@link Integer}, {@link Long} or {@link Boolean}.
     *
     * @param objectClass   The class of the object we are checking in the predicate.
     * @param fieldName The name of the field we are checking against.
     * @param args  The list of string arguments from {@link ComparisonNode}
     * @return  A cast list of the provided arguments.
     */
    private List<Object> castNodeStringArgsToFieldType(Class<T> objectClass, String fieldName, List<String> args) {
        Field field = ensureFieldPresent(objectClass, fieldName);
        Class<?> fieldClass = field.getType();
        return args.stream()
                .map(
                        a -> {
                            Object result = a;
                            if (fieldClass == Integer.class || fieldClass == int.class)
                                result = Integer.parseInt(a);
                            else if (fieldClass == Long.class || fieldClass == long.class)
                                result =  Long.parseLong(a);
                            else if (fieldClass == Boolean.class || fieldClass == boolean.class)
                                result = Boolean.valueOf(a);
                            return result;
                        }
                )
                .toList();
    }

    /**
     * Creates an AND linked single {@link Predicate} from the provided predicate list.
     *
     * @param predicateList The {@link List} of {@link Predicate} we are joining via logical AND.
     * @return  A single {@link Predicate} of the provided list joined by logical AND.
     */
    private Predicate<T> andList(List<Predicate<T>> predicateList) {
        if (predicateList == null || predicateList.isEmpty())
            return null;

        Predicate<T> result = predicateList.get(0);

        if (predicateList.size() > 1) {
            for (int i = 1; i < predicateList.size(); i++) {
                result = result.and(predicateList.get(i));
            }
        }

        return result;
    }

    /**
     * Creates an OR linked single {@link Predicate} from the provided predicate list.
     *
     * @param predicateList The {@link List} of {@link Predicate} we are joining via logical OR.
     * @return  A single {@link Predicate} of the provided list joined by logical OR.
     */
    private Predicate<T> orList(List<Predicate<T>> predicateList) {
        if (predicateList == null || predicateList.isEmpty())
            return null;

        Predicate<T> result = predicateList.get(0);

        if (predicateList.size() > 1) {
            for (int i = 1; i < predicateList.size(); i++) {
                result = result.or(predicateList.get(i));
            }
        }

        return result;
    }

    /**
     * Ensures a field is present in the provided class and returns it.
     * As the class getField only returns public fields it is useless to use.
     * Similarly getDeclaredField does not include inherited fields, thus we must recursively check down until with hit the Object class for the field.
     *
     * @param objectClass   The class we are checking the field exists in.
     * @param fieldName The field name we are checking for.
     * @return  The field if present.
     */
    private Field ensureFieldPresent(@NonNull Class<?> objectClass, @NonNull String fieldName) {
        try {
            return objectClass.getDeclaredField(fieldName);
        }
        catch (NoSuchFieldException ex) {
            if (objectClass.getSuperclass() == Object.class) {
                log.warn("Field [{}] is not valid for class [{}]", fieldName, objectClass.getSimpleName());
                throw new RsqlException(400, String.format("Field [%s] is not valid for the given filter.", fieldName));
            }
            return ensureFieldPresent(objectClass.getSuperclass(), fieldName);
        }
    }

    /**
     * Get the value of the specified field in the provided object.
     *
     * @param object    The object we are getting the field from.
     * @param fieldName The name of the field we want to get.
     * @return  The field value.
     */
    private Object getFieldValue(@NonNull T object, @NonNull String fieldName) {
        ensureFieldPresent(object.getClass(), fieldName);
        Class<T> objectClass = (Class<T>)object.getClass();
        String firstLetterUpperFieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        String methodName = String.format("get%s", firstLetterUpperFieldName);

        try {
            Method getter = objectClass.getMethod(methodName);
            return getter.invoke(object);
        }
        catch (NoSuchMethodException ex) {
            log.warn("Getter method for field [{}] not found in class [{}]", fieldName, objectClass.getSimpleName());
            throw new RsqlException(400, String.format("Getter method for field [%s] not found.", fieldName));
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            log.warn("Exception [{}] thrown calling getter method [{}]", ex.getClass().getSimpleName(), methodName);
            throw new RsqlException(500, String.format("Exception occurred when calling getter for field [%s]", fieldName));
        }
    }

}
