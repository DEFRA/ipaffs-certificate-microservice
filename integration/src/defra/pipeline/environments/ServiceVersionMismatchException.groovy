package defra.pipeline.environments

import org.codehaus.groovy.GroovyException

class ServiceVersionMismatchException extends GroovyException {

    ServiceVersionMismatchException(String message) {
        super(message)
    }
}
