package net.jmb19905.messenger.messages.exception;

public class UnsupportedSideException extends Exception{

    public UnsupportedSideException(){
        super();
    }

    public UnsupportedSideException(String message){
        super(message);
    }

    public UnsupportedSideException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedSideException(Throwable cause) {
        super(cause);
    }

}
