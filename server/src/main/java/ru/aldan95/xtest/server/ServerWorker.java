package ru.aldan95.xtest.server;

import org.slf4j.Logger;
import org.slf4j.MDC;
import ru.aldan95.xtest.common.RemoteCallStatus;
import ru.aldan95.xtest.common.Request;
import ru.aldan95.xtest.common.Response;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class ServerWorker implements Runnable {

    private static final Logger logger = getLogger(ServerWorker.class);

    private Request request;
    private Object service;
    private Consumer<Response> responseWriter;

    ServerWorker(Request request, Object service, Consumer<Response> responseWriter) {
        this.request = request;
        this.service = service;
        this.responseWriter = responseWriter;
    }

    @Override
    public void run() {
        Response response = new Response(request.getId());
        MDC.put("req", "id=" + request.getId() + ":" + request.getService() + "." + request.getMethod() + ". ");
        logger.info("Executing with params {}", request.getParams());
        try {
            if (service == null) throw new ClassNotFoundException("No \"" + request.getService() + "\" service found");
            Object retVal = callMethod();
            response.setStatus(RemoteCallStatus.Success);
            response.setRetVal(retVal);
            logger.info("Execution result: {}", retVal);
        } catch (ClassNotFoundException e) {
            logger.error("NoSuchService: " + request.getService(), e);
            response.setStatus(RemoteCallStatus.NoSuchService);
            response.setErrDesc(e.getMessage());
        } catch (NoSuchMethodException e) {
            logger.error("NoSuchMethod: " + request.getService() + "." + request.getMethod(), e);
            response.setStatus(RemoteCallStatus.NoSuchMethod);
            response.setErrDesc(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("IllegalArgument: " + request.getService() + "." + request.getMethod() + "(" + Arrays.toString(request.getParams()) + ")", e);
            response.setStatus(RemoteCallStatus.IllegalArgument);
            response.setErrDesc(e.getMessage());
        } catch (InvocationTargetException e) {
            logger.error("ServiceError", e);
            response.setStatus(RemoteCallStatus.ServiceError);
            response.setErrDesc(e.getMessage());
        } catch (Throwable e) {
            logger.error("GeneralError", e);
            response.setStatus(RemoteCallStatus.Error);
            response.setErrDesc(e.getMessage());
        } finally {
            MDC.clear();
        }
        responseWriter.accept(response);
    }

    protected Object callMethod() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<Method> methods = Arrays.stream(service.getClass().getMethods()).filter(this::methodFilter).collect(Collectors.toList());
        if (methods.isEmpty()) {
            throw new NoSuchMethodException("No " + request.getService() + "." + request.getMethod() + " method with given parameters found");
        } else if (methods.size() > 1) {
            throw new IllegalArgumentException("Ambiguous parameters given for " + request.getService() + "." + request.getMethod());
        }
        Method method = methods.get(0);
        Object retVal = method.invoke(service, request.getParams());
        return method.getReturnType() == Void.TYPE? Void.TYPE : retVal;
    }

    private boolean methodFilter(Method m) {
        if (!m.getName().equals(request.getMethod())) return false;
        int paramsCount = request.getParams() == null? 0 : request.getParams().length;
        if (m.getParameterCount() != paramsCount ) return false;
        for (int i=0; i<paramsCount; i++) {
            if (request.getParams()[i] != null && !request.getParams()[i].getClass().equals(m.getParameterTypes()[i])) return false;
        }
        return true;
    }
}
