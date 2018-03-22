package cern.ais.gridwars.web.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;


@Controller
public class GlobalErrorController implements ErrorController {

    private static final String PATH = "/error";

    @GetMapping(PATH)
    public ModelAndView renderErrorPage(HttpServletRequest httpRequest) {
        ModelAndView errorPage = new ModelAndView("pages/error");
        getErrorCode(httpRequest).ifPresent(errorCode -> populateErrorPageModel(errorPage, errorCode));
        return errorPage;
    }

    private Optional<Integer> getErrorCode(HttpServletRequest httpRequest) {
        return Optional.ofNullable((Integer) httpRequest.getAttribute("javax.servlet.error.status_code"));
    }

    private void populateErrorPageModel(ModelAndView errorPageModel, Integer errorCode) {
        String errorMsg;

        switch (errorCode) {
            case 400: {
                errorMsg = "Bad Request";
                break;
            }
            case 401: {
                errorMsg = "Unauthorized";
                break;
            }
            case 403: {
                errorMsg = "Forbidden";
                break;
            }
            case 404: {
                errorMsg = "Resource Not Found";
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

        errorPageModel.addObject("errorCode", errorCode);
        errorPageModel.addObject("errorMsg", errorMsg);
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }
}
