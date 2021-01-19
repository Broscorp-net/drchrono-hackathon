package com.remind.me.doc.controller;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

public abstract class BaseController {

        @InitBinder
        public void initBinder(final WebDataBinder binder) {

          final StringTrimmerEditor stringtrimmer =
                  new StringTrimmerEditor(true);
          binder.registerCustomEditor(String.class, stringtrimmer);
        }
}
