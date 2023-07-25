package org.rmm.rsql.exceotion;


/**
 * Exception to stop Sonar complaining. Replace with proper dedicated exception later.
 *
 * @author Rob McMurray
 */
public class RsqlException extends RuntimeException{

    /**
     * Http status code.
     */
    final int status;

    /**
     * Constructor
     */
    public RsqlException(int status, String message) {
        super(message);
        this.status = status;
    }

}
