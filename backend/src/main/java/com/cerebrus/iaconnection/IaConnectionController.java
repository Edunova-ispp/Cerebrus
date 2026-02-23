package com.cerebrus.iaconnection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/iaconnection")
@CrossOrigin(origins = "*")
public class IaConnectionController {

    private final IaConnectionService iaConnectionService;

    @Autowired
    public IaConnectionController(IaConnectionService iaConnectionService) {
        this.iaConnectionService = iaConnectionService;
    } 
}
