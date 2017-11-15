package jlox;

public class Return extends RuntimeException {
    /* We use java exceptions to return from a function.
     * Exceptions let us quickly go back up the call stack.
     * Since we're using exceptions for control flow this class suppresses
     * the error handling/stack tracey things provided by RuntimeException.
     */
    final Object value;

    Return(Object value) {
        // disable stack trace, provide no error message info
        super(null, null, false, false);
        this.value = value;
    }
}
