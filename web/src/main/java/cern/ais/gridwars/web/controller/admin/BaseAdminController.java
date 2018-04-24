package cern.ais.gridwars.web.controller.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/admin")
public abstract class BaseAdminController {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());
}
