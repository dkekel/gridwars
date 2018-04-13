package cern.ais.gridwars.web.controller.error;

import cern.ais.gridwars.web.util.ModelAndViewBuilder;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;


@Controller
public class GlobalErrorController implements ErrorController {

    private static final String PATH = "/error";

//    @Value("${spring.servlet.multipart.max-file-size}")
//    private String maxUploadSize;

    @RequestMapping(PATH)
    public ModelAndView renderErrorPage(HttpServletRequest httpRequest) {
        ModelAndViewBuilder mav = ModelAndViewBuilder.forPage("error/index");

        getErrorCode(httpRequest).ifPresent(errorCode -> populateErrorPageModel(mav, errorCode));

        return mav.toModelAndView();
    }

    private Optional<Integer> getErrorCode(HttpServletRequest httpRequest) {
        return Optional.ofNullable((Integer) httpRequest.getAttribute("javax.servlet.error.status_code"));
    }

    private Optional<Exception> getException(HttpServletRequest httpRequest) {
        return Optional.ofNullable((Exception) httpRequest.getAttribute("javax.servlet.error.exception"));
    }

    private void populateErrorPageModel(ModelAndViewBuilder mav, Integer errorCode) {
        String errorMsg;

//        if (getException())

        switch (errorCode) {
            case 400: {
                errorMsg = "Invalid Request";
                break;
            }
            case 401: {
                errorMsg = "Unauthorized";
                break;
            }
            case 403: {
                errorMsg = "Access Denied";
                break;
            }
            case 404: {
                errorMsg = "Page Not Found";
                break;
            }
            case 500: {
                errorMsg = "Internal Server Error";
                break;
            }
            default: {
                errorMsg = "Unknown Error";
            }
        }

        mav.addAttribute("errorCode", errorCode).addAttribute("errorMsg", errorMsg);
    }

    private boolean isMaxUploadSizeExceededException(Exception exception) {
        Throwable currentException = exception;

        while (currentException != null) {
            if (MaxUploadSizeExceededException.class.isInstance(currentException)) {
                return true;
            }

            currentException = exception.getCause();
        }

        return false;
    }
    @Override
    public String getErrorPath() {
        return PATH;
    }
}
