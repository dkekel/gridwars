package cern.ais.gridwars.web.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

public class ModelAndViewBuilder {

    private final ModelAndView modelAndView;

    public static ModelAndViewBuilder forPage(String pageName) {
        return forViewName("pages/" + pageName);
    }

    public static ModelAndViewBuilder forRedirect(String targetUrl) {
        return forViewName("redirect:" + targetUrl);
    }

    public static ModelAndViewBuilder forViewName(String viewName) {
        return new ModelAndViewBuilder().setViewName(viewName);
    }

    public ModelAndViewBuilder setViewName(String viewName) {
        modelAndView.setViewName(viewName);
        return this;
    }

    public ModelAndViewBuilder setStatus(HttpStatus httpStatus) {
        modelAndView.setStatus(httpStatus);
        return this;
    }

    public ModelAndViewBuilder addAttribute(String key, Object value) {
        modelAndView.addObject(key, value);
        return this;
    }

    public ModelAndView toModelAndView() {
        return modelAndView;
    }

    private ModelAndViewBuilder() {
        modelAndView = new ModelAndView();
    }
}
